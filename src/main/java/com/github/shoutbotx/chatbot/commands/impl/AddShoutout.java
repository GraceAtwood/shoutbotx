package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutRepository;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.shoutbotx.chatbot.utils.ChatUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class AddShoutout extends Command {
    private static final Logger logger = LogManager.getLogger();

    public static final int MAX_SHOUTOUTS = 100;

    private final ShoutoutRepository shoutoutRepository;

    @Inject
    public AddShoutout(
            ShoutoutRepository shoutoutRepository
    ) {
        super("add", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutRepository = shoutoutRepository;
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
            ChatUtils.sendMessage(event, e.getMessage());
            return;
        }

        var message = StringUtils.substringAfter(arguments, " ");
        if ("".equals(message)) {
            ChatUtils.respondToMessage(event, "Malformed message contents!  Please try '!xso help add'.");
            return;
        }

        var totalShoutouts = shoutoutRepository.getAllShoutouts().size();

        if (totalShoutouts >= MAX_SHOUTOUTS) {
            ChatUtils.respondToMessage(event, "This channel has reached the maximum allowed shoutouts!");
            return;
        }

        shoutoutRepository.saveShoutout(user, message);
        ChatUtils.sendMessage(event, "Added shoutout for %s!", user);
    }
}
