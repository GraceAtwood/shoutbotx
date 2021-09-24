package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.freeolympus.notyourfathersbot.chatbot.bot.CachingHelixProvider;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSetting;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSettingRepository;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.RaidEvent;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

public class RaidShoutoutEventHandler {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final CachingHelixProvider cachingHelixProvider;

    @Inject
    public RaidShoutoutEventHandler(
            ShoutoutRepository shoutoutRepository,
            ShoutoutSettingRepository shoutoutSettingRepository,
            CachingHelixProvider cachingHelixProvider
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.shoutoutSettingRepository = shoutoutSettingRepository;
        this.cachingHelixProvider = cachingHelixProvider;
    }

    @EventSubscriber
    public void handle(RaidEvent event) {
        logger.info("[{}] raiding!", event.getRaider().getName());

        var user = event.getRaider().getName();

        var shoutout = shoutoutRepository.getRandomShoutout(user);
        var shoutoutSettings = shoutoutSettingRepository.getShoutoutSettingForUser(user);

        if (shoutout == null) {
            logger.info("No custom shoutout message found for {}", user);
            return;
        }

        if (shoutoutSettings.isEmpty()) {
            throw new IllegalStateException(format("Failed to find shoutout settings for a user with shoutouts!! User: %s", user));
        }

        logger.info("Selected custom shoutout message for {}", user);

        var message = ChatUtils.formatShoutoutMessage(cachingHelixProvider, shoutout, event.getRaider());

        logger.info("Sending shoutout: {}", message);

        event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

        // Now we need to update DynamoDB with this shoutout time
        logger.info("Updating dynamodb shoutout time");
        reportShoutout(user, shoutoutSettings.get());
        setFutureShoutout(shoutoutSettings.get());
    }

    public void reportShoutout(String user, ShoutoutSetting shoutoutSetting) {
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
