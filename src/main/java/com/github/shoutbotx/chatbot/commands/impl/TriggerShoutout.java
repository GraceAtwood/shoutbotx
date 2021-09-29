package com.github.shoutbotx.chatbot.commands.impl;

import com.github.shoutbotx.chatbot.commands.Command;
import com.github.shoutbotx.chatbot.dynamodb.Shoutout;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutRepository;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutSetting;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutSettingRepository;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.shoutbotx.chatbot.utils.ChatUtils;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.shoutbotx.chatbot.bot.TwitchConfigModule.TWITCH_AUTH_TOKEN_KEY;
import static com.github.shoutbotx.chatbot.utils.ChatUtils.respondToMessage;
import static com.github.shoutbotx.chatbot.utils.ChatUtils.sendMessage;

public class TriggerShoutout extends Command {
    private static final Logger logger = LogManager.getLogger();

    private final ShoutoutRepository shoutoutRepository;
    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final String authToken;

    @Inject
    public TriggerShoutout(
            ShoutoutRepository shoutoutRepository,
            ShoutoutSettingRepository shoutoutSettingRepository,
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken
    ) {
        super(Pattern.compile("^(?!.*add|.*del|.*set-int|.*list|.*get-int).*$"), Set.of(CommandPermission.BROADCASTER, CommandPermission.MODERATOR));

        this.shoutoutRepository = shoutoutRepository;
        this.shoutoutSettingRepository = shoutoutSettingRepository;
        this.authToken = authToken;
    }

    @Override
    protected void executeInternal(ChannelMessageEvent event, String commandText, String arguments) {

        if (StringUtils.isBlank(commandText)) {
            logger.info("Malformed command! commandText: {} | args: {}", commandText, arguments);
            return;
        }

        var user = StringUtils.substringBefore(commandText, " ");

        try {
            user = ChatUtils.stripUsername(user).toLowerCase();
        } catch (InvalidUsernameException e) {
            sendMessage(event, e.getMessage());
            return;
        }

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            sendMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        Shoutout shoutout;
        // If no index was provided, choose a random shoutout
        if ("".equals(arguments)) {
            shoutout = shoutouts.get(ThreadLocalRandom.current().nextInt(shoutouts.size()) % shoutouts.size());
        } else {

            // else, choose a specific one if the index parses
            //noinspection UnstableApiUsage
            var index = Optional.of(arguments)
                    .map(Ints::tryParse);

            if (index.isEmpty()) {
                respondToMessage(event, "Index must be a number!");
                return;
            }

            if (index.get() < 1 || index.get() > shoutouts.size()) {
                respondToMessage(event, "Index is out of range! There are %s shoutouts for %s.", shoutouts.size(), user);
                return;
            }

            shoutout = shoutouts.stream().sorted(Comparator.comparing(Shoutout::getTimeAdded)).collect(Collectors.toList()).get(index.get() - 1);
        }

        var settings = shoutoutSettingRepository.getShoutoutSettingForUser(user);
        sendMessage(event, ChatUtils.formatShoutoutMessage(event.getServiceMediator().getService(TwitchClient.class, "twitch4j"), authToken, shoutout));
        reportShoutout(settings);
    }

    public void reportShoutout(ShoutoutSetting shoutoutSetting) {
        if (shoutoutSetting.getForceShoutoutAfter() != null && Instant.now().isAfter(shoutoutSetting.getForceShoutoutAfter())) {
            shoutoutSetting.setForceShoutoutAfter(null);
        }

        shoutoutSetting.setLastShoutoutTime(Instant.now());

        shoutoutSettingRepository.saveShoutoutSetting(shoutoutSetting);
    }
}
