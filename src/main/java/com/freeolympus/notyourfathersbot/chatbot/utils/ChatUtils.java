package com.freeolympus.notyourfathersbot.chatbot.utils;

import com.freeolympus.notyourfathersbot.chatbot.bot.CachingHelixProvider;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.Shoutout;
import com.freeolympus.notyourfathersbot.chatbot.exceptions.InvalidUsernameException;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.String.format;

public class ChatUtils {
    private static final Logger logger = LogManager.getLogger();

    private ChatUtils() {
    }

    public static void respondToMessage(ChannelMessageEvent event, String message, Object... formatParams) {
        event.getTwitchChat().sendMessage(event.getChannel().getName(), format(message, formatParams), null, event.getEventId());
    }

    public static String formatShoutoutMessage(CachingHelixProvider cachingHelixProvider, Shoutout shoutout, EventUser user) {

        var message = shoutout.getMessage();

        if (message.contains("%url%") || message.contains("%lastplayed%")) {
            var channelInformation = cachingHelixProvider.getChannelInformation(user.getId());

            message = StringUtils.replace(message, "%url%", format("https://twitch.tv/%s", channelInformation.getBroadcasterName()));

            var gameName = channelInformation.getGameName();

            if (StringUtils.isBlank(gameName)) {
                gameName = "nothing";
            }

            message = StringUtils.replace(message, "%lastplayed%", gameName);
        }

        logger.info("Formatted shoutout from {} to {}", shoutout.getMessage(), message);

        return message;
    }

    public static String stripUsername(String input) throws InvalidUsernameException {
        if (input.startsWith("@")) {
            if (input.length() == 1) {
                throw new InvalidUsernameException(input);
            }
            return input.substring(1);
        }

        return input;
    }

}
