package com.freeolympus.notyourfathersbot.chatbot;

import com.freeolympus.notyourfathersbot.chatbot.bot.ChatBotFactory;
import com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb.ShoutoutRecordRepository;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            logger.info("{} : {}", entry.getKey(), entry.getValue());
        }

        try {
            logger.info("Creating injector");
            var injector = Guice.createInjector(new ConfigModule());
            logger.info("Created injector");
            for (Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
                logger.info("Key: {} | Binding: {}", entry.getKey(), entry.getValue());
            }

            logger.info("Injecting ChatBotFactory");
            var chatBotFactory = injector.getInstance(ChatBotFactory.class);
            logger.info("Injected ChatBotFactory");

            logger.info("Constructing chatbot");
            var chatBot = chatBotFactory.construct("MaedreamTV");
            logger.info("Constructed chatbot");

            chatBot.start();
        } catch (Throwable throwable) {
            logger.error("An unhandled error occurred!", throwable);
        }
    }
}
