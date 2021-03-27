package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ChatFilterEventHandler {
    private static final Logger logger = LogManager.getLogger(ChatFilterEventHandler.class);

    private static final List<Predicate<ChannelMessageEvent>> rejectionRules = new ArrayList<>() {{
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
    }};

    @Inject
    public ChatFilterEventHandler() {
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {
        var isSubscriber = event.getPermissions().contains(CommandPermission.SUBSCRIBER);
        var isVIP = event.getPermissions().contains(CommandPermission.VIP);
        var isBroadcaster = event.getPermissions().contains(CommandPermission.BROADCASTER);
        var isMod = event.getPermissions().contains(CommandPermission.MODERATOR);

        if ((isBroadcaster || isMod || isSubscriber || isVIP)) {
            return;
        }

        if (event.getMessage().length() < 10) {
            return;
        }

        logger.info("Evaluating message...");

        for (Predicate<ChannelMessageEvent> shouldReject : rejectionRules) {
            if (shouldReject.test(event)) {
                event.getTwitchChat().ban(event.getChannel().getName(), event.getUser().getName(), "In order to combat spammers and trolls, new, more strict chat rules have been put in place.  Your message was deleted due to one or more of these rules.  It's possible this was in error as these new rules can be overzealous.  If you feel this was the case, please private message one of the Moderators and we'll sort it out.");
                logger.info("Deleted message for violating rule!");

                return;
            }
        }

        logger.info("Permitted message");
    }
}