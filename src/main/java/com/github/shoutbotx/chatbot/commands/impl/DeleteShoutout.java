package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.shoutbotx.chatbot.repositories.ShoutoutRepository;
import com.github.shoutbotx.chatbot.utils.Utils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

public class DeleteShoutout extends Command {
    private static final Logger logger = LogManager.getLogger();
    private final ShoutoutRepository shoutoutRepository;

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
            user = Utils.stripUsername(user).toLowerCase();
        } catch (InvalidUsernameException e) {
            Utils.sendMessage(event, e.getMessage());
            return;
        }

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            Utils.sendMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        var indexArg = StringUtils.substringAfter(arguments, " ");
        // If no index was provided, delete all shoutouts for user
        if ("".equals(indexArg)) {
            shoutouts.forEach(shoutoutRepository::deleteShoutout);
            Utils.sendMessage(event, "Deleted %s shoutout(s) for %s", shoutouts.size(), user);
            return;
        }

        // Ok we want to delete a specific index - let's try to parse it to an int
        //noinspection UnstableApiUsage
        var index = Optional.of(indexArg)
                .map(Ints::tryParse);

        if (index.isEmpty()) {
            Utils.sendMessage(event, "Index must be a number!");
            return;
        }

        if (index.get() < 1 || index.get() > shoutouts.size()) {
            Utils.sendMessage(event, "Index is out of range! There are %s shoutouts for %s.", shoutouts.size(), user);
            return;
        }

        var shoutoutToDelete = shoutouts.get(index.get() - 1);
        shoutoutRepository.deleteShoutout(shoutoutToDelete);
        Utils.sendMessage(event, "Deleted 1 shoutout for %s", user);
    }
}
