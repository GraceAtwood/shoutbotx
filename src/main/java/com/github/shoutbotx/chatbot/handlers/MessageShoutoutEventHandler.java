package com.github.shoutbotx.chatbot.handlers;

import com.github.shoutbotx.chatbot.repositories.ShoutoutRepository;
import com.github.shoutbotx.chatbot.utils.Utils;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.shoutbotx.chatbot.config.TwitchConfigModule.TWITCH_AUTH_TOKEN_KEY;

public class MessageShoutoutEventHandler {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final String authToken;

    @Inject
    public MessageShoutoutEventHandler(
            ShoutoutRepository shoutoutRepository,
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.authToken = authToken;
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {
        var user = event.getUser().getName();
        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            // We have no custom shout out info for this person
            return;
        }

        var shoutout = Utils.selectRandomWithWeight(shoutouts);

        if (!shoutout.isReadyForShoutout()) {
            logger.info("Determined we aren't ready to send a shoutout for {}.", user);
            return;
        }

        logger.info("Determined we need to send a periodic shoutout to {}", user);

        var message = Utils.formatShoutoutMessage(event.getServiceMediator().getService(TwitchClient.class, "twitch4j"), authToken, shoutout);

        logger.info("Sending shoutout: {}", message);

        event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

        // Now we need to update DynamoDB with this shoutout time
        logger.info("Updating shoutout time");
        shoutout.setLastShoutoutTime(Instant.now());
        shoutoutRepository.updateShoutout(shoutout);
    }
}