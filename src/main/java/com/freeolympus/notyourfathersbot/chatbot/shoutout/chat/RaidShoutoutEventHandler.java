package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeolympus.notyourfathersbot.chatbot.handlers.MessageLoggerEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.CustomShoutouts;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb.ShoutoutRecordRepository;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Objects;

import static java.lang.String.format;

public class RaidShoutoutEventHandler {
    private static final Logger logger = LogManager.getLogger(MessageLoggerEventHandler.class);

    private final CustomShoutouts customShoutouts;
    private final ShoutoutRecordRepository shoutoutRecordRepository;

    @Inject
    public RaidShoutoutEventHandler(
            CustomShoutouts customShoutouts,
            ShoutoutRecordRepository shoutoutRecordRepository
    ) {
        this.customShoutouts = customShoutouts;
        this.shoutoutRecordRepository = shoutoutRecordRepository;

        shoutoutRecordRepository.initialize();
    }

    @EventSubscriber
    public void handle(RaidEvent event) {
        logger.info("[{}] raiding!", event.getRaider().getName());

        var shoutoutInfo = customShoutouts.forStreamer(event.getRaider().getName());
        if (shoutoutInfo != null) {
            logger.info("Found custom shoutout message for {}", event.getRaider().getName());

            var twitchClient = event.getServiceMediator().getService(TwitchClient.class, "twitch4j");

            var channelInformation = twitchClient.getHelix().getChannelInformation(null, Collections.singletonList(event.getRaider().getId())).execute().getChannels().get(0);

            var message = shoutoutInfo.produceShoutoutMessage(channelInformation);

            logger.info("Sending shoutout: {}", message);

            event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

            logger.info("Updating dynamodb shoutout time");

            shoutoutRecordRepository.updateShoutoutTimeToNow(event.getChannel().getName(), event.getRaider().getName());
        } else {
            logger.info("No custom shoutout message found for {}", event.getRaider().getName());
        }
    }
}
