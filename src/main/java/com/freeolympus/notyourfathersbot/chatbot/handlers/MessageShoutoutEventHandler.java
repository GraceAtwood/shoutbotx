package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.freeolympus.notyourfathersbot.chatbot.bot.CachingHelixProvider;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSetting;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSettingRepository;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
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

    private final ShoutoutRepository shoutoutRepository;
    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final CachingHelixProvider cachingHelixProvider;

    @Inject
    public MessageShoutoutEventHandler(
            ShoutoutRepository shoutoutRepository,
            ShoutoutSettingRepository shoutoutSettingRepository,
            CachingHelixProvider cachingHelixProvider
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.shoutoutSettingRepository = shoutoutSettingRepository;
        this.cachingHelixProvider = cachingHelixProvider;
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {
        var user = event.getUser().getName();
        var shoutout = shoutoutRepository.getRandomShoutout(user);

        if (shoutout == null) {
            // We have no custom shout out info for this person
            return;
        }

        var shoutoutSettings = shoutoutSettingRepository.getShoutoutSettingForUser(user);

        if (shoutoutSettings.isEmpty()) {
            throw new IllegalStateException(format("Failed to find shoutout settings for a user with shoutouts!! User: %s", user));
        }

        if (!isReadyForShoutout(shoutoutSettings.get())) {
            logger.info("Determined we aren't ready to send a shoutout for {}", user);
            return;
        }

        logger.info("Determined we need to send a periodic shoutout to {}", user);

        var message = ChatUtils.formatShoutoutMessage(cachingHelixProvider, shoutout, event.getUser());

        logger.info("Sending shoutout: {}", message);

        event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

        // Now we need to update DynamoDB with this shoutout time
        logger.info("Updating dynamodb shoutout time");
        reportShoutout(user, shoutoutSettings.get());
    }

    public static Boolean isReadyForShoutout(ShoutoutSetting setting) {

        if (setting.getForceShoutoutAfter() != null && Instant.now().isAfter(setting.getForceShoutoutAfter())) {
            return true;
        }

        if (setting.getLastShoutoutTime() == null)
            return true;

        return Instant.now().isAfter(setting.getLastShoutoutTime().plus(setting.getShoutoutIntervalMins(), ChronoUnit.MINUTES));
    }

    public void reportShoutout(String user, ShoutoutSetting shoutoutSetting) {
        if (shoutoutSetting.getForceShoutoutAfter() != null && Instant.now().isAfter(shoutoutSetting.getForceShoutoutAfter())) {
            shoutoutSetting.setForceShoutoutAfter(null);
        }

        shoutoutSetting.setLastShoutoutTime(Instant.now());

        shoutoutSettingRepository.saveShoutoutSetting(shoutoutSetting);
    }
}