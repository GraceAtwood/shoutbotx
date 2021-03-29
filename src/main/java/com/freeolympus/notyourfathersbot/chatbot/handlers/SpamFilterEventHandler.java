package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class SpamFilterEventHandler {
    private static final Logger logger = LogManager.getLogger(SpamFilterEventHandler.class);

    private static final int spamMessageCountThreshold = 3;
    private static final int spamWindowMinutes = 2;
    private static final int timeoutDurationMinutes = 2;
    private static final String banReason = "You were banned because your message looked like spam and because you were previously timed out for this same issue.  I'm a bot though and known to be a little dumb.  If you think this ban was in error, please contact a moderator and we'll set it right <3";
    private static final String timeoutReason = "You were timed out because your message looked like spam at a time in chat when multiple such messages were being sent.  Please avoid overuse of special characters and ascii emojis like \uD83D\uDE0A - twitch emojis are fine.  If you think this was in error, please contact a moderator and we'll set it right <3";

    private static final List<Predicate<ChannelMessageEvent>> spamDetectionRules = new ArrayList<>() {{
        add(event -> {
            var countSpecial = 0;

            for (char c : event.getMessage().toCharArray()) {
                if (!Character.isLetterOrDigit(c))
                    countSpecial++;
            }

            var countStandard = event.getMessage().length() - countSpecial;

            logger.info("Special = {}, Standard = {}", countSpecial, countStandard);

            return countSpecial > countStandard;
        });
        add(event -> {
            var isSubscriber = event.getPermissions().contains(CommandPermission.SUBSCRIBER);
            var isVIP = event.getPermissions().contains(CommandPermission.VIP);
            var isBroadcaster = event.getPermissions().contains(CommandPermission.BROADCASTER);
            var isMod = event.getPermissions().contains(CommandPermission.MODERATOR);

            return !isBroadcaster && !isMod && !isSubscriber && !isVIP;
        });
    }};

    private List<ChannelMessageEvent> suspectedSpamMessages = new ArrayList<>();
    private Boolean isAntiSpamEnabled = false;
    private final HashSet<String> timedOutSpammers = new HashSet<>();

    @Inject
    public SpamFilterEventHandler() {
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {

        removeOldMessages();

        logger.info("Evaluating message...");

        // Did the spam filter receive a command?
        if (event.getMessage().startsWith("!antispam")) {
            handleAntiSpamCommand(event);
            return;
        }

        // No the message wasn't a command for us, was it a spam message?
        if (spamDetectionRules.stream().allMatch(predicate -> predicate.test(event))) {
            spamSuspected(event);
        }

        logger.info("Permitted message");
    }

    private void handleAntiSpamCommand(ChannelMessageEvent event) {

        var isVIP = event.getPermissions().contains(CommandPermission.VIP);
        var isBroadcaster = event.getPermissions().contains(CommandPermission.BROADCASTER);
        var isMod = event.getPermissions().contains(CommandPermission.MODERATOR);

        var canUse = isVIP || isBroadcaster || isMod;

        if (!canUse) {
            logger.info("Rejecting command from user '{}'.  This user is not authorized to use this command!", event.getUser().getName());
            return;
        }

        var elements = event.getMessage().split(Pattern.quote(" "));

        if (elements.length != 2) {
            logger.info("Invalid message syntax for !antispam command!");
            event.getTwitchChat().sendMessage(event.getChannel().getName(), "Invalid command syntax!  Please try again <3!");
            return;
        }

        var command = elements[1];

        switch (command.toLowerCase()) {
            case "on" -> {
                event.getTwitchChat().sendMessage(event.getChannel().getName(), format("Activating antispam.  %s message(s) exist(s) and will be handled.  '!antispam off' to turn me off.", suspectedSpamMessages.size()));
                deleteAndTimeoutOrBanSuspectedSpamMessages();
                isAntiSpamEnabled = true;
            }
            case "off" -> {
                isAntiSpamEnabled = false;
                event.getTwitchChat().sendMessage(event.getChannel().getName(), "antispam disabled.  Now in passive listening mode.");
            }
            case "status" -> {
                var spammerNames = suspectedSpamMessages.stream().map(spamEvent -> spamEvent.getUser().getName()).collect(Collectors.joining(", "));
                event.getTwitchChat().sendPrivateMessage(event.getUser().getName(), format("Status: %s | Suspected Spam Messages In Window: %s | Suspected Spammers: %s | Spam Window: %s min(s) | Antispam Activation Threshold: %s",
                        isAntiSpamEnabled ? "on" : "off",
                        suspectedSpamMessages.size(),
                        spammerNames,
                        spamWindowMinutes,
                        spamMessageCountThreshold));
            }
            case "help" -> sendHelpMessage(event);
            default -> event.getTwitchChat().sendMessage(event.getChannel().getName(), "Unknown command.  '!antispam help' for options.");
        }
    }

    private void sendHelpMessage(ChannelMessageEvent event) {
        var message = format("Messages sent by non-subscribers whose special character (!,&,#, etc.) count exceeds normal characters (a,b, 1, 2, etc.) are considered spam.  Twitch emojis do not count as spam, but ascii emojis like \uD83D\uDE0A do count.  If %s spam messages are sent within %s minutes of each other, the antispam will activate, delete, and then timeout all messages from that window.  It will stay active until disabled - while active, all suspected spam messages will be removed.  Commands: !antispam [on,off,status,help]", spamMessageCountThreshold, spamWindowMinutes);
        logger.info(message);
        event.getTwitchChat().sendPrivateMessage(event.getUser().getName(), message);
    }

    private void spamSuspected(ChannelMessageEvent event) {
        logger.info("Spam suspected!");
        suspectedSpamMessages.add(event);

        if (isAntiSpamEnabled) {
            logger.info("Antispam is currently enabled.  In this mode, messages are automatically deleted and their senders are timed out.");
            deleteAndTimeoutOrBanSuspectedSpamMessages();
        } else {
            if (suspectedSpamMessages.size() >= spamMessageCountThreshold) {
                var message = format("The number of suspected spam messages (%s) exceeds the threshold allowed (%s) within %s " +
                                "minutes.  Activating anti-spam measures.  Send '!antispam off' to disable these measures.",
                        suspectedSpamMessages.size(), spamMessageCountThreshold, spamWindowMinutes);

                logger.info(message);
                event.getTwitchChat().sendMessage(event.getChannel().getName(), message);

                deleteAndTimeoutOrBanSuspectedSpamMessages();
                isAntiSpamEnabled = true;
            }
        }
    }

    private void removeOldMessages() {
        var originalSize = suspectedSpamMessages.size();

        suspectedSpamMessages = suspectedSpamMessages.stream().filter(event ->
                Duration.between(event.getFiredAtInstant(), Instant.now()).toMinutes() < (long) SpamFilterEventHandler.spamWindowMinutes)
                .collect(Collectors.toList());

        logger.info("Removed {} messages from suspected spam messages because they were older than {} minutes", originalSize - suspectedSpamMessages.size(), (long) SpamFilterEventHandler.spamWindowMinutes);
    }

    private void deleteAndTimeoutOrBanSuspectedSpamMessages() {

        for (ChannelMessageEvent event : suspectedSpamMessages) {
            var timedOutBefore = timedOutSpammers.contains(event.getUser().getName());

            if (timedOutBefore) {
                event.ban(event.getUser().getName(), banReason);
            } else {
                event.getTwitchChat().delete(event.getChannel().getName(), event.getMessageEvent().getMessageId().orElseThrow());
                event.timeout(event.getUser().getName(), Duration.ofMinutes(timeoutDurationMinutes), timeoutReason);

                timedOutSpammers.add(event.getUser().getName());
            }
        }

        suspectedSpamMessages.clear();
    }
}