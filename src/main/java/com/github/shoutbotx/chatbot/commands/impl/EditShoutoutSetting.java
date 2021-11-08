package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.config.ConfigModule;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import static com.github.shoutbotx.chatbot.utils.Utils.sendMessage;

public class EditShoutoutSetting extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final String channel;

    @Inject
    public EditShoutoutSetting(
            @Named(ConfigModule.CHANNEL) String channel
    ) {
        super("edit", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.channel = channel;
    }

    // !xso edit xanniette 2 something=value

    @Override
    protected void executeInternal(ChannelMessageEvent event, String commandText, String arguments) {

        sendMessage(event, "Not implemented yet!");

/*
        if (StringUtils.isBlank(arguments)) {
            logger.info("Malformed command! commandText: {} | args: {}", commandText, arguments);
            return;
        }

        var argElements = arguments.split(Pattern.quote(" "));

        if (argElements.length < 3) {
            sendMessage(event, "Malformed command!");
            return;
        }

        var user = argElements[0];

        try {
            user = ChatUtils.stripUsername(user).toLowerCase();
        } catch (InvalidUsernameException e) {
            sendMessage(event, e.getMessage());
            return;
        }

        var indexArg = argElements[1];
        var indices = new ArrayList<Integer>();

        shoutoutSettingRepository.getShoutoutSettingForUser(user);

        if (indexArg.equalsIgnoreCase("all")) {

        }*/
    }
}
