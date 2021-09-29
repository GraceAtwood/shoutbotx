package com.freeolympus.notyourfathersbot.chatbot.commands.impl;

import com.freeolympus.notyourfathersbot.chatbot.commands.Command;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.Shoutout;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSetting;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSettingRepository;
import com.freeolympus.notyourfathersbot.chatbot.exceptions.InvalidUsernameException;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;
import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.respondToMessage;
import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.sendMessage;

public class SetInterval extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final String channel;

    @Inject
    public SetInterval(
            ShoutoutSettingRepository shoutoutSettingRepository,
            @Named(CHANNEL) String channel
    ) {
        super("set-int", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutSettingRepository = shoutoutSettingRepository;
        this.channel = channel;
    }

    @Override
    protected void executeInternal(ChannelMessageEvent event, String commandText, String arguments) {

        if (StringUtils.isBlank(arguments)) {
            logger.info("Malformed command! commandText: {} | args: {}", commandText, arguments);
            return;
        }

        var user = StringUtils.substringBefore(arguments, " ");

        try {
            user = ChatUtils.stripUsername(user).toLowerCase();
        } catch (InvalidUsernameException e) {
            sendMessage(event, e.getMessage());
            return;
        }

        var intervalArg = StringUtils.substringAfter(arguments, " ");

        Integer newInterval;

        // If the interval arg is blank, then set it back to default
        if ("".equals(intervalArg)) {
            newInterval = ShoutoutSetting.createDefaultSettings(channel, user).getShoutoutIntervalMins();
        } else {
            // If the arg is set, then let's try to parse it and use it
            //noinspection UnstableApiUsage
            var newIntervalOptional = Optional.of(intervalArg)
                    .map(Ints::tryParse);

            if (newIntervalOptional.isEmpty()) {
                respondToMessage(event, "New interval must be a number!");
                return;
            }

            if (newIntervalOptional.get() <= 0) {
                respondToMessage(event, "New interval must be greater than 0!");
                return;
            }

            newInterval = newIntervalOptional.get();
        }

        var settings = shoutoutSettingRepository.getShoutoutSettingForUser(user);
        settings.setShoutoutIntervalMins(newInterval);
        shoutoutSettingRepository.saveShoutoutSetting(settings);

        sendMessage(event, "Shoutout interval for %s set to %s mins", user, newInterval);
    }
}
