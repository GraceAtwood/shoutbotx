package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.dynamodb.Shoutout;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutRepository;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.shoutbotx.chatbot.utils.ChatUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DeleteShoutout extends Command {
    private static final Logger logger = LogManager.getLogger();

    private ShoutoutRepository shoutoutRepository;

    @Inject
    public DeleteShoutout(
            ShoutoutRepository shoutoutRepository
    ) {
        super("del", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

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

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            ChatUtils.sendMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        var indexArg = StringUtils.substringAfter(arguments, " ");
        // If no index was provided, delete all shoutouts for user
        if ("".equals(indexArg)) {
            shoutouts.forEach(shoutoutRepository::deleteShoutout);
            ChatUtils.sendMessage(event, "Deleted %s shoutout(s) for %s", shoutouts.size(), user);
            return;
        }

        // Ok we want to delete a specific index - let's try to parse it to an int
        //noinspection UnstableApiUsage
        var index = Optional.of(indexArg)
                .map(Ints::tryParse);

        if (index.isEmpty()) {
            ChatUtils.sendMessage(event, "Index must be a number!");
            return;
        }

        if (index.get() < 1 || index.get() > shoutouts.size()) {
            ChatUtils.sendMessage(event, "Index is out of range! There are %s shoutouts for %s.", shoutouts.size(), user);
            return;
        }

        var shoutoutToDelete = shoutouts.stream().sorted(Comparator.comparing(Shoutout::getTimeAdded)).collect(Collectors.toList()).get(index.get() - 1);
        shoutoutRepository.deleteShoutout(shoutoutToDelete);
        ChatUtils.sendMessage(event, "Deleted 1 shoutout for %s", user);
    }
}
