package com.freeolympus.notyourfathersbot.chatbot.handlers;

import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageLoggerEventHandler {
    private static final Logger logger = LogManager.getLogger(MessageLoggerEventHandler.class);

    @Inject
    public MessageLoggerEventHandler() {
    }

    @EventSubscriber
    public void handle(ChannelMessageEvent event) {
        logger.info("[{}][{}] - {}", event.getChannel().getName(), event.getUser().getName(), event.getMessage());
    }
}