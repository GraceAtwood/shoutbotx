package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.freeolympus.notyourfathersbot.chatbot.cron.ChatBotRunnable;
import com.freeolympus.notyourfathersbot.chatbot.cron.Every;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChatBot implements Closeable {
    private static final Logger logger = LogManager.getLogger(ChatBot.class);

    private final String channel;
    private final TwitchClient twitchClient;
    private final ScheduledExecutorService cronExecutor;

    public ChatBot(String channel, TwitchClient twitchClient) {
        this.channel = channel;
        this.twitchClient = twitchClient;

        this.cronExecutor = Executors.newScheduledThreadPool(5);
    }

    public void start() {
        twitchClient.getChat().joinChannel(channel);

        logger.info("Joined channel {}", channel);
        twitchClient.getChat().sendMessage(channel, "I've restarted!");
        logger.info("Sent message!");
    }

    public void registerEvent(Object eventListener) {
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventListener);
    }

    public void registerCron(ChatBotRunnable chatBotRunnable) {
        var annotation = chatBotRunnable.getClass().getAnnotation(Every.class);

        Objects.requireNonNull(annotation, "Cron functions must have an Every annotation!");

        cronExecutor.scheduleAtFixedRate(() -> chatBotRunnable.run(channel, twitchClient), 0, annotation.interval(), annotation.timeUnit());
    }

    @Override
    public void close() throws IOException {
        cronExecutor.shutdownNow();

        twitchClient.getChat().disconnect();

        twitchClient.close();
        twitchClient.getEventManager().close();
        //twitchClient.getChat().close();
    }
}
