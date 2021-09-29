package com.github.shoutbotx.chatbot.handlers;

import com.github.shoutbotx.chatbot.dynamodb.ShoutoutRepository;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutSetting;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutSettingRepository;
import com.github.shoutbotx.chatbot.utils.ChatUtils;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.shoutbotx.chatbot.bot.TwitchConfigModule.TWITCH_AUTH_TOKEN_KEY;
import static java.lang.String.format;

public class RaidShoutoutEventHandler {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final String authToken;

    @Inject
    public RaidShoutoutEventHandler(
            ShoutoutRepository shoutoutRepository,
            ShoutoutSettingRepository shoutoutSettingRepository,
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.shoutoutSettingRepository = shoutoutSettingRepository;
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

        var shoutout = shoutouts.get(ThreadLocalRandom.current().nextInt(shoutouts.size()) % shoutouts.size());
        var shoutoutSettings = shoutoutSettingRepository.getShoutoutSettingForUser(user);

        logger.info("Selected custom shoutout message for {}", user);

        var message = ChatUtils.formatShoutoutMessage(event.getServiceMediator().getService(TwitchClient.class, "twitch4j"), authToken, shoutout);

        logger.info("Sending shoutout: {}", message);

        ChatUtils.sendMessage(event, message);

        // Now we need to update DynamoDB with this shoutout time
        logger.info("Updating dynamodb shoutout time");
        reportShoutout(shoutoutSettings);
        setFutureShoutout(shoutoutSettings);
    }

    public void reportShoutout(ShoutoutSetting shoutoutSetting) {
        if (shoutoutSetting.getForceShoutoutAfter() != null && Instant.now().isAfter(shoutoutSetting.getForceShoutoutAfter())) {
            shoutoutSetting.setForceShoutoutAfter(null);
        }

        shoutoutSetting.setLastShoutoutTime(Instant.now());

        shoutoutSettingRepository.saveShoutoutSetting(shoutoutSetting);
    }

    public void setFutureShoutout(ShoutoutSetting shoutoutSetting) {
        shoutoutSetting.setForceShoutoutAfter(Instant.now().plus(5, ChronoUnit.MINUTES));

        shoutoutSettingRepository.saveShoutoutSetting(shoutoutSetting);
    }
}
