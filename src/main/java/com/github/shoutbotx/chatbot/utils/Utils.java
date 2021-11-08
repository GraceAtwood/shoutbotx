package com.github.shoutbotx.chatbot.utils;

import com.github.shoutbotx.chatbot.entities.ShoutoutEntity;
import com.github.shoutbotx.chatbot.exceptions.InvalidUsernameException;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.common.collect.DiscreteDomain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Utils {
    private static final Logger logger = LogManager.getLogger();

    private Utils() {
    }

    public static void respondToMessage(ChannelMessageEvent event, String message, Object... formatParams) {
        event.getTwitchChat().sendMessage(event.getChannel().getName(), formatParams.length == 0 ? message : format(message, formatParams), null, event.getEventId());
    }

    public static void sendMessage(AbstractChannelEvent event, String message, Object... formatParams) {
        event.getTwitchChat().sendMessage(event.getChannel().getName(), formatParams.length == 0 ? message : format(message, formatParams));
    }

    public static String formatShoutoutMessage(TwitchClient twitchClient, String authToken, ShoutoutEntity shoutout) {

        var message = shoutout.getMessage();

        if (message.contains("%url%") || message.contains("%lastplayed%")) {
            var user = twitchClient.getHelix()
                    .getUsers(authToken, null, Collections.singletonList(shoutout.getUser()))
                    .execute()
                    .getUsers()
                    .get(0);

            var channelInformation = twitchClient.getHelix()
                    .getChannelInformation(authToken, Collections.singletonList(user.getId()))
                    .execute()
                    .getChannels()
                    .get(0);

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

    public static ShoutoutEntity selectRandomWithWeight(List<ShoutoutEntity> list) {
        var distribution = new EnumeratedDistribution<>(
                list.stream()
                        .map(shoutout -> Pair.create(shoutout, shoutout.getWeight()))
                        .collect(Collectors.toList()));

        return distribution.sample();
    }

}
