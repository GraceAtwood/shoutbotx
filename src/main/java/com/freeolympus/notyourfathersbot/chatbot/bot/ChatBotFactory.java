package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.freeolympus.notyourfathersbot.chatbot.Main;
import com.freeolympus.notyourfathersbot.chatbot.handlers.SpamFilterEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.handlers.MessageLoggerEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.MessageShoutoutEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.RaidShoutoutEventHandler;
import com.github.twitch4j.TwitchClient;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ChatBotFactory {
    private static final Logger logger = LogManager.getLogger(ChatBotFactory.class);

    private final Supplier<TwitchClient> twitchClientSupplier;
    private final MessageLoggerEventHandler messageLoggerEventHandler;
    private final SpamFilterEventHandler spamFilterEventHandler;
    private final RaidShoutoutEventHandler raidShoutoutEventHandler;
    private final MessageShoutoutEventHandler messageShoutoutEventHandler;

    @Inject
    public ChatBotFactory(
            Supplier<TwitchClient> twitchClientSupplier,
            MessageLoggerEventHandler messageLoggerEventHandler,
            SpamFilterEventHandler spamFilterEventHandler,
            RaidShoutoutEventHandler raidShoutoutEventHandler,
            MessageShoutoutEventHandler messageShoutoutEventHandler
    ) {
        this.twitchClientSupplier = twitchClientSupplier;
        this.messageLoggerEventHandler = messageLoggerEventHandler;
        this.spamFilterEventHandler = spamFilterEventHandler;
        this.raidShoutoutEventHandler = raidShoutoutEventHandler;
        this.messageShoutoutEventHandler = messageShoutoutEventHandler;
    }

    public ChatBot construct(String channel) {

        logger.info("Constructing chat bot for channel {}", channel);

        var chatBot = new ChatBot(channel, twitchClientSupplier.get());

        chatBot.registerEvent(messageLoggerEventHandler);
        //chatBot.registerEvent(spamFilterEventHandler);
        chatBot.registerEvent(raidShoutoutEventHandler);
        chatBot.registerEvent(messageShoutoutEventHandler);

        return chatBot;
    }
}
