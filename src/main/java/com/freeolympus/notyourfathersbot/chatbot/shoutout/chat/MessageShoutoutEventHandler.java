package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb.ShoutoutRecordRepository;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static java.lang.String.format;

public class MessageShoutoutEventHandler {
    private static final Logger logger = LogManager.getLogger(MessageShoutoutEventHandler.class);

    private final ShoutoutRecordRepository shoutoutRecordRepository;
    private final CustomShoutouts customShoutouts;

    @Inject
    public MessageShoutoutEventHandler(
            ShoutoutRecordRepository shoutoutRecordRepository,
            CustomShoutouts customShoutouts
    ) {
        this.shoutoutRecordRepository = shoutoutRecordRepository;
        this.customShoutouts = customShoutouts;

        shoutoutRecordRepository.initialize();
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {
        var viewerName = event.getUser().getName();
        var shoutoutInfo = customShoutouts.forStreamer(viewerName);

        if (shoutoutInfo == null) {
            // We have no custom shout out info for this person
            return;
        }

        var shoutoutTime = shoutoutRecordRepository.getShoutoutTime(event.getChannel().getName(), viewerName);

        logger.info("Received last shoutout time from DDB for {} : {}", viewerName, shoutoutTime);

        if (shoutoutTime == null || shoutoutTime.plus(shoutoutInfo.getShoutoutIntervalMins(), ChronoUnit.MINUTES).isBefore(Instant.now())) {
            // we need to give them a new shoutout!

            logger.info("Determined we need to send a periodic shoutout to {}", viewerName);

            var twitchClient = event.getServiceMediator().getService(TwitchClient.class, "twitch4j");

            var channelInformation = twitchClient.getHelix().getChannelInformation(null, Collections.singletonList(event.getUser().getId())).execute().getChannels().get(0);

            var message = shoutoutInfo.produceShoutoutMessage(channelInformation);

            logger.info("Sending shoutout: {}", message);

            event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

            // Now we need to update DynamoDB with this shoutout time
            logger.info("Updating dynamodb shoutout time");
            shoutoutRecordRepository.updateShoutoutTimeToNow(event.getChannel().getName(), viewerName);
        } else {
            logger.info("Skipping shoutout message for {}", viewerName);
        }
    }
}