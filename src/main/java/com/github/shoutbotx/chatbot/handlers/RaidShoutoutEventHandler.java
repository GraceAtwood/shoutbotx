package com.github.shoutbotx.chatbot.handlers;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.shoutbotx.chatbot.repositories.ShoutoutRepository;
import com.github.shoutbotx.chatbot.utils.Utils;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.shoutbotx.chatbot.config.TwitchConfigModule.TWITCH_AUTH_TOKEN_KEY;

public class RaidShoutoutEventHandler {

    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final String authToken;

    @Inject
    public RaidShoutoutEventHandler(
            ShoutoutRepository shoutoutRepository,
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.authToken = authToken;
    }

    @EventSubscriber
    public void handle(RaidEvent event) {
        logger.info("[{}] raiding!", event.getRaider().getName());

        var user = event.getRaider().getName();

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            logger.info("No custom shoutout message found for {}", user);
            return;
        }

        var shoutout = Utils.selectRandomWithWeight(shoutouts);

        logger.info("Selected custom shoutout message for {}", user);

        var message = Utils.formatShoutoutMessage(event.getServiceMediator().getService(TwitchClient.class, "twitch4j"), authToken, shoutout);

        logger.info("Sending shoutout: {}", message);

        Utils.sendMessage(event, message);

        // Now we need to update DynamoDB with this shoutout time
        logger.info("Updating shoutout time");
        shoutout.setLastShoutoutTime(Instant.now());
        shoutoutRepository.updateShoutout(shoutout);
    }
}
