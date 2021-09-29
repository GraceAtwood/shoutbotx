package com.freeolympus.notyourfathersbot.chatbot.commands.impl;

import com.freeolympus.notyourfathersbot.chatbot.commands.Command;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.util.ChatReply;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.respondToMessage;

public class AddShoutout extends Command {

    public static final int MAX_SHOUTOUTS = 100;

    private ShoutoutRepository shoutoutRepository;

    @Inject
    public AddShoutout(
            ShoutoutRepository shoutoutRepository
    ) {
        super("add", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutRepository = shoutoutRepository;
    }

    @Override
    protected void executeInternal(ChannelMessageEvent event, String arguments) {
        var user = StringUtils.substringBefore(arguments, " ");
        var message = StringUtils.substringAfter(arguments, " ");
        if (message.equals("")) {
            respondToMessage(event, "Malformed message contents!  Please try '!xso help add'.");
            return;
        }

        var totalShoutouts = shoutoutRepository.getAllShoutouts().size();

        if (totalShoutouts >= MAX_SHOUTOUTS) {
            respondToMessage(event, "This channel has reached the maximum allowed shoutouts!");
            return;
        }

        shoutoutRepository.saveShoutout(user, message);
        shoutout


    }
}
