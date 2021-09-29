package com.github.shoutbotx.chatbot.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class Command {

    @Getter
    private final Pattern commandRegex;

    @Getter
    private final Set<CommandPermission> permittedPermissions;

    public Command(String command, Set<CommandPermission> permittedPermissions) {
        this.commandRegex = Pattern.compile(Pattern.quote(command));
        this.permittedPermissions = permittedPermissions;
    }

    public Command(Pattern pattern, Set<CommandPermission> permissions) {
        commandRegex = pattern;
        this.permittedPermissions = permissions;
    }

    public final void execute(ChannelMessageEvent event, String commandText, String arguments) {
        executeInternal(event, commandText, arguments);
    }

    protected abstract void executeInternal(ChannelMessageEvent event, String commandText, String arguments);

    public String getHelpText() {
        return "No help provided for command!";
    }
}
