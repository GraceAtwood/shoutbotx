package com.freeolympus.notyourfathersbot.chatbot;

import com.freeolympus.notyourfathersbot.chatbot.bot.ChatBotFactory;
import com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb.ShoutoutRecordRepository;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        try {
            var injector = Guice.createInjector(new ConfigModule());

            var chatBotFactory = injector.getInstance(ChatBotFactory.class);

            var chatBot = chatBotFactory.construct("MaedreamTV");

            chatBot.start();
        } catch (Throwable throwable) {
            logger.error("An unhandled error occurred!", throwable);
        }
    }
}
