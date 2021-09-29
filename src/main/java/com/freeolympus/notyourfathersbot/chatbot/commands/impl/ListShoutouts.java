package com.freeolympus.notyourfathersbot.chatbot.commands.impl;

import com.freeolympus.notyourfathersbot.chatbot.commands.Command;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.Shoutout;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.exceptions.InvalidUsernameException;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.collect.Streams;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.respondToMessage;
import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.sendMessage;
import static java.lang.String.format;

public class ListShoutouts extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;

    @Inject
    public ListShoutouts(
            ShoutoutRepository shoutoutRepository
    ) {
        super("list", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutRepository = shoutoutRepository;
    }

    @Override
    protected void executeInternal(ChannelMessageEvent event, String commandText, String arguments) {

        if (StringUtils.isBlank(arguments) || arguments.contains(" ")) {
            logger.info("Malformed command! commandText: {} | args: {}", commandText, arguments);
            return;
        }

        String user;
        try {
            user = ChatUtils.stripUsername(arguments).toLowerCase();
        } catch (InvalidUsernameException e) {
            sendMessage(event, e.getMessage());
            return;
        }

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            sendMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        //noinspection UnstableApiUsage
        var message = format("Shoutouts for %s: ", user).concat(
                Streams.mapWithIndex(
                        shoutouts.stream().sorted(Comparator.comparing(Shoutout::getTimeAdded)),
                        (shoutout, x) -> format("(%s) %s", x + 1, shoutout.getMessage())).collect(Collectors.joining(" ||| ")));

        sendMessage(event, message);
    }
}
