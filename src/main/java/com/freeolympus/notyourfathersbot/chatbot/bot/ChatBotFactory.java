package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.freeolympus.notyourfathersbot.chatbot.handlers.ChatFilterEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.handlers.MessageLoggerEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.MessageShoutoutEventHandler;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.RaidShoutoutEventHandler;
import com.github.twitch4j.TwitchClient;
import com.google.inject.Inject;

import java.util.function.Supplier;

public class ChatBotFactory {

    private final Supplier<TwitchClient> twitchClientSupplier;
    private final MessageLoggerEventHandler messageLoggerEventHandler;
    private final ChatFilterEventHandler chatFilterEventHandler;
    private final RaidShoutoutEventHandler raidShoutoutEventHandler;
    private final MessageShoutoutEventHandler messageShoutoutEventHandler;

    @Inject
    public ChatBotFactory(
            Supplier<TwitchClient> twitchClientSupplier,
            MessageLoggerEventHandler messageLoggerEventHandler,
            ChatFilterEventHandler chatFilterEventHandler,
            RaidShoutoutEventHandler raidShoutoutEventHandler,
            MessageShoutoutEventHandler messageShoutoutEventHandler
    ) {
        this.twitchClientSupplier = twitchClientSupplier;
        this.messageLoggerEventHandler = messageLoggerEventHandler;
        this.chatFilterEventHandler = chatFilterEventHandler;
        this.raidShoutoutEventHandler = raidShoutoutEventHandler;
        this.messageShoutoutEventHandler = messageShoutoutEventHandler;
    }

    public ChatBot construct(String channel) {
        var chatBot = new ChatBot(channel, twitchClientSupplier.get());

        chatBot.registerEvent(messageLoggerEventHandler);
        chatBot.registerEvent(chatFilterEventHandler);
        chatBot.registerEvent(raidShoutoutEventHandler);
        chatBot.registerEvent(messageShoutoutEventHandler);

        return chatBot;
    }
}
