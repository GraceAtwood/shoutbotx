package com.freeolympus.notyourfathersbot.chatbot.commands.impl;

import com.freeolympus.notyourfathersbot.chatbot.commands.Command;
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

import java.util.Optional;
import java.util.Set;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;
import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.respondToMessage;
import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.sendMessage;

public class GetInterval extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutSettingRepository shoutoutSettingRepository;

    @Inject
    public GetInterval(
            ShoutoutSettingRepository shoutoutSettingRepository
    ) {
        super("get-int", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutSettingRepository = shoutoutSettingRepository;
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

        var shoutoutIntervalMins = shoutoutSettingRepository.getShoutoutSettingForUser(user).getShoutoutIntervalMins();

        sendMessage(event, "Shoutout interval for %s is %s mins", user, shoutoutIntervalMins);
    }
}
