package com.freeolympus.notyourfathersbot.chatbot.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

public abstract class Command {

    @Getter
    private final String command;

    @Getter
    private final Set<CommandPermission> permittedPermissions;

    @Getter
    private final Integer coolOffMinutes;

    public Command(String command, Set<CommandPermission> permittedPermissions) {
        this(command, permittedPermissions, null);
    }

    public Command(String command) {
        this(command, Collections.singleton(CommandPermission.EVERYONE), null);
    }

    public Command(String command, Set<CommandPermission> permittedPermissions, Integer coolOffMinutes) {
        this.command = command;
        this.permittedPermissions = permittedPermissions;
        this.coolOffMinutes = coolOffMinutes;
    }

    public final void execute(ChannelMessageEvent event, String arguments) {
        executeInternal(event, arguments);
    }

    protected abstract void executeInternal(ChannelMessageEvent event, String arguments);

    public String getHelpText() {
        return "No help provided for command!";
    }
}
