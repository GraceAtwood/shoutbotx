package com.github.shoutbotx.chatbot.handlers;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class MessageCommandEventHandler {
    private static final Logger logger = LogManager.getLogger();

    private final List<Command> commands;

    @Inject
    public MessageCommandEventHandler(
            List<Command> commands
    ) {
        this.commands = commands;
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {

        // !xso add @tashalej some message
        if (event.getMessage().startsWith("!xso")) {

            var withoutEntry = StringUtils.substringAfter(event.getMessage(), " ");
            var commandText = StringUtils.substringBefore(withoutEntry, " ");
            var args = StringUtils.substringAfter(withoutEntry, " ");

            if ("".equals(commandText)) {
                // TODO Handle this case a little better?
                logger.info("malformed command?");
                return;
            }

            var matchingCommands = commands.stream()
                    .filter(command -> command.getCommandRegex().matcher(commandText).find())
                    .collect(Collectors.toList());

            if (matchingCommands.isEmpty()) {
                logger.info("Failed to find any matching commands!");
                return;
            }

            if (matchingCommands.size() > 1) {
                var classNames = matchingCommands.stream().map(x -> x.getClass().getName()).collect(Collectors.joining(", "));
                logger.error("Found more than one matching path for command {}.  Matching classes: {}", commandText, classNames);
                return;
            }

            var commandToExecute = matchingCommands.get(0);
            logger.info("Selected command {}", commandToExecute.getClass().getSimpleName());

            var commandPermitted = event.getPermissions().stream().anyMatch(commandPermission -> commandToExecute.getPermittedPermissions().contains(commandPermission));

            if (!commandPermitted) {
                logger.info("Command not permitted for use by {}", event.getUser().getName());
                return;
            }

            commandToExecute.execute(event, commandText, args);
        }
    }
}