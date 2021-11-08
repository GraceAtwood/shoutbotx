package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.entities.ShoutoutEntity;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.shoutbotx.chatbot.repositories.ChannelRepository;
import com.github.shoutbotx.chatbot.repositories.ShoutoutRepository;
import com.github.shoutbotx.chatbot.utils.Utils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Set;

import static com.github.shoutbotx.chatbot.config.ConfigModule.CHANNEL;

public class AddShoutout extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final ChannelRepository channelRepository;
    private final String channel;

    @Inject
    public AddShoutout(
            ShoutoutRepository shoutoutRepository,
            ChannelRepository channelRepository,
            @Named(CHANNEL) String channel
    ) {
        super("add", Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));
        this.shoutoutRepository = shoutoutRepository;
        this.channelRepository = channelRepository;
        this.channel = channel;
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

        var message = StringUtils.substringAfter(arguments, " ");
        if ("".equals(message)) {
            Utils.respondToMessage(event, "Malformed message contents!  Please try '!xso help add'.");
            return;
        }

        var totalShoutouts = shoutoutRepository.countShoutouts();
        var maxShoutouts = channelRepository.getChannel().getMaxShoutouts();

        if (totalShoutouts >= maxShoutouts) {
            Utils.respondToMessage(event, "This channel has reached the maximum allowed shoutouts!");
            return;
        }

        shoutoutRepository.addShoutout(ShoutoutEntity.builder()
                .channel(channel)
                .user(user)
                .message(message)
                .timeAdded(Instant.now())
                .shoutoutIntervalMins(60)
                .weight(100)
                .build());

        Utils.sendMessage(event, "Added shoutout for %s!", user);
    }
}
