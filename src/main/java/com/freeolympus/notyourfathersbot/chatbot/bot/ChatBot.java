package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.freeolympus.notyourfathersbot.chatbot.handlers.*;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;

public class ChatBot implements Closeable {
    private static final Logger logger = LogManager.getLogger(ChatBot.class);

    private final String channel;
    private final TwitchClient twitchClient;

    @Inject
    public ChatBot(
            MessageLoggerEventHandler messageLoggerEventHandler,
            SpamFilterEventHandler spamFilterEventHandler,
            RaidShoutoutEventHandler raidShoutoutEventHandler,
            MessageShoutoutEventHandler messageShoutoutEventHandler,
            MessageCommandEventHandler messageCommandEventHandler,
            TwitchClient twitchClient,
            @Named(CHANNEL) String channel
    ) {
        this.channel = channel;
        this.twitchClient = twitchClient;

        registerEvent(messageLoggerEventHandler);
        registerEvent(messageCommandEventHandler);
        // registerEvent(spamFilterEventHandler);
        registerEvent(raidShoutoutEventHandler);
        registerEvent(messageShoutoutEventHandler);
    }

    public void start() {
        twitchClient.getChat().joinChannel(channel);

        logger.info("Joined channel {}", channel);
        // twitchClient.getChat().sendMessage(channel, "I've restarted!");
        logger.info("Sent message!");
    }

    private void registerEvent(Object eventListener) {
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventListener);
    }

    @Override
    public void close() throws IOException {
        twitchClient.getChat().disconnect();

        twitchClient.close();
        twitchClient.getEventManager().close();
    }
}
