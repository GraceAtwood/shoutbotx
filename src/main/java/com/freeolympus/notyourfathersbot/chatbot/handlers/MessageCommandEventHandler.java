package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.freeolympus.notyourfathersbot.chatbot.bot.CachingHelixProvider;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.Shoutout;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutRepository;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSettingRepository;
import com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.freeolympus.notyourfathersbot.chatbot.utils.ChatUtils.respondToMessage;
import static java.lang.String.format;

public class MessageCommandEventHandler {
    private static final Logger logger = LogManager.getLogger();

    private static final AtomicReference<Instant> lastInteraction = new AtomicReference<>(null);
    private final ShoutoutRepository shoutoutRepository;
    private final ShoutoutSettingRepository shoutoutSettingRepository;
    private final CachingHelixProvider cachingHelixProvider;

    @Inject
    public MessageCommandEventHandler(
            ShoutoutRepository shoutoutRepository,
            ShoutoutSettingRepository shoutoutSettingRepository,
            CachingHelixProvider cachingHelixProvider
    ) {
        this.shoutoutRepository = shoutoutRepository;
        this.shoutoutSettingRepository = shoutoutSettingRepository;
        this.cachingHelixProvider = cachingHelixProvider;
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {

        if (event.getMessage().startsWith("!") && event.getMessage().length() > 1) {
            var spaceIndex = event.getMessage().indexOf(" ");
            if (spaceIndex == -1) {
                logger.info("malformed command?");
                return;
            }

            var command = event.getMessage().substring(1, spaceIndex);
            switch (command) {
                case "xso":
                    doAddSo(event);
                    break;
                case "delso":
                    doDelSo(event);
                    break;
                case "setso":
                    doSetSo(event);
                    break;
                case "tso":
                    doTriggerSo(event);
                    break;
                case "listso":
                    doListSo(event);
                    break;
                default:
                    logger.info("Message not a command");
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkCanUseCommands(ChannelMessageEvent event) {
        return event.getPermissions().contains(CommandPermission.BROADCASTER) || event.getPermissions().contains(CommandPermission.MODERATOR);
    }

    private boolean canInteractAndUpdateTime() {
        var canInteract = false;

        if (lastInteraction.get() == null || Duration.between(lastInteraction.get(), Instant.now()).getSeconds() > Duration.ofSeconds(5).getSeconds()) {
            canInteract = true;

            lastInteraction.getAndUpdate(instant -> Instant.now());
        }

        return canInteract;
    }

    private void sendHelp(ChannelMessageEvent event) {
        if (canInteractAndUpdateTime()) {
            event.getTwitchChat().sendMessage(event.getChannel().getName(), "Invalid parameters.  !helpso for more info <3", null, event.getEventId());
        }
    }

    private void doAddSo(ChannelMessageEvent event) {
        if (!checkCanUseCommands(event)) {
            logger.info("User '{}' isn't a mod but tried to use a command :(", event.getUser().getName());

            if (canInteractAndUpdateTime()) {
                respondToMessage(event, "I'm sorry you must be a mod to use me ;)");
            }

            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));
        if (elements.length < 3) {
            sendHelp(event);
            return;
        }

        var user = elements[1].toLowerCase();


        var message = event.getMessage().substring(StringUtils.ordinalIndexOf(event.getMessage(), " ", 2));

        shoutoutRepository.saveShoutout(user, message);
        shoutoutSettingRepository.createDefaultShoutoutSetting(user);

        respondToMessage(event, "Added new shoutout for %s!", user);
    }

    private void doDelSo(ChannelMessageEvent event) {
        if (!checkCanUseCommands(event)) {
            logger.info("User '{}' isn't a mod but tried to use a command :(", event.getUser().getName());

            if (canInteractAndUpdateTime()) {
                respondToMessage(event, "I'm sorry you must be a mod to use me ;)");
            }

            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));
        if (elements.length < 2 || elements.length > 3) {
            sendHelp(event);
            return;
        }

        var user = elements[1].toLowerCase();
        if (user.startsWith("@")) {
            if (user.length() == 1) {
                sendHelp(event);
                return;
            }
            user = user.substring(1);
        }

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            respondToMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        if (elements.length == 2) {
            shoutouts.forEach(shoutoutRepository::deleteShoutout);
            respondToMessage(event, "Deleted %s shoutout(s) for %s", shoutouts.size(), user);
            return;
        }

        //noinspection UnstableApiUsage
        var index = Optional.ofNullable(elements[2])
                .map(Ints::tryParse);

        if (index.isEmpty()) {
            respondToMessage(event, "Index must be a number!");
            return;
        }

        if (index.get() < 1 || index.get() > shoutouts.size()) {
            respondToMessage(event, "Index is out of range! There are %s shoutouts for %s.", shoutouts.size(), user);
            return;
        }

        var shoutoutToDelete = shoutouts.stream().sorted(Comparator.comparing(Shoutout::getTimeAdded)).collect(Collectors.toList()).get(index.get() - 1);
        shoutoutRepository.deleteShoutout(shoutoutToDelete);
        respondToMessage(event, "Deleted 1 shoutout for %s", user);
    }

    private void doSetSo(ChannelMessageEvent event) {
        if (!checkCanUseCommands(event)) {
            logger.info("User '{}' isn't a mod but tried to use a command :(", event.getUser().getName());

            if (canInteractAndUpdateTime()) {
                respondToMessage(event, "I'm sorry you must be a mod to use me ;)");
            }

            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));
        if (elements.length < 2 || elements.length > 3) {
            sendHelp(event);
            return;
        }

        var user = elements[1].toLowerCase();
        if (user.startsWith("@")) {
            if (user.length() == 1) {
                sendHelp(event);
                return;
            }
            user = user.substring(1);
        }

        if (elements.length == 2) {
            var shoutoutSetting = shoutoutSettingRepository.getShoutoutSettingForUser(user);
            if (shoutoutSetting.isEmpty()) {
                respondToMessage(event, "Shoutout settings are not configured for %s.  Try adding a shoutout first <3", user);
                return;
            }

            respondToMessage(event, "Shoutouts will occur every %s minutes for %s", shoutoutSetting.get().getShoutoutIntervalMins(), user);
            return;
        }

        //noinspection UnstableApiUsage
        var newInterval = Optional.ofNullable(elements[2])
                .map(Ints::tryParse);

        if (newInterval.isEmpty()) {
            respondToMessage(event, "New interval must be a number!");
            return;
        }

        if (newInterval.get() <= 0) {
            respondToMessage(event, "New interval must be greater than 0!");
            return;
        }

        shoutoutSettingRepository.createDefaultShoutoutSetting(user);
        respondToMessage(event, "Shoutout interval for %s set to %s!", user, newInterval);
    }

    private void doTriggerSo(ChannelMessageEvent event) {
        if (!checkCanUseCommands(event)) {
            logger.info("User '{}' isn't a mod but tried to use a command :(", event.getUser().getName());

            if (canInteractAndUpdateTime()) {
                respondToMessage(event, "I'm sorry you must be a mod to use me ;)");
            }

            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));
        if (elements.length < 2 || elements.length > 3) {
            sendHelp(event);
            return;
        }

        var user = elements[1].toLowerCase();
        if (user.startsWith("@")) {
            if (user.length() == 1) {
                sendHelp(event);
                return;
            }
            user = user.substring(1);
        }

        Shoutout shoutout;

        if (elements.length == 2) {
            shoutout = shoutoutRepository.getRandomShoutout(user);
        } else {
            var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

            if (shoutouts.isEmpty()) {
                respondToMessage(event, "No shoutouts exist for %s", user);
                return;
            }

            //noinspection UnstableApiUsage
            var index = Optional.ofNullable(elements[2])
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

        if (shoutout == null) {
            respondToMessage(event, "There are no shoutouts configured for %s.  Try adding one <3", user);
            return;
        }

        event.getTwitchChat().sendMessage(event.getChannel().getName(), ChatUtils.formatShoutoutMessage(cachingHelixProvider, shoutout, event.getUser()));
    }

    private void doListSo(ChannelMessageEvent event) {
        if (!checkCanUseCommands(event)) {
            logger.info("User '{}' isn't a mod but tried to use a command :(", event.getUser().getName());

            if (canInteractAndUpdateTime()) {
                respondToMessage(event, "I'm sorry you must be a mod to use me ;)");
            }

            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));
        if (elements.length != 2) {
            sendHelp(event);
            return;
        }

        var user = elements[1].toLowerCase();
        if (user.startsWith("@")) {
            if (user.length() == 1) {
                sendHelp(event);
                return;
            }
            user = user.substring(1);
        }

        var shoutouts = shoutoutRepository.getShoutoutsForUser(user);

        if (shoutouts.isEmpty()) {
            respondToMessage(event, "No shoutouts exist for %s", user);
            return;
        }

        //noinspection UnstableApiUsage
        var message = format("Shoutouts for %s:", user).concat(
                Streams.mapWithIndex(
                        shoutouts.stream(),
                        (shoutout, x) -> format("%s. %s", x + 1, shoutout.getMessage())).collect(Collectors.joining(" ||| ")));

        respondToMessage(event, message);
    }
}