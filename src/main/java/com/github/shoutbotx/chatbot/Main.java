package com.github.shoutbotx.chatbot;

import com.github.shoutbotx.chatbot.config.ConfigModule;
import com.google.inject.*;
import org.apache.commons.cli.*;
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
            var injector = processCommandlineArgs(args);
            logger.info("Created injector");
            for (Map.Entry<Key<?>, Binding<?>> entry : injector.getAllBindings().entrySet()) {
                logger.info("Key: {} | Binding: {}", entry.getKey(), entry.getValue());
            }

            logger.info("Injecting ChatBot");
            var chatBot = injector.getInstance(ChatBot.class);
            logger.info("Injected ChatBot");

            chatBot.start();
        } catch (Throwable throwable) {
            logger.error("An unhandled error occurred!", throwable);
            throw throwable;
        }
    }

    private static Injector processCommandlineArgs(String[] args) {

        final var options = new Options();

        options.addOption(Option.builder()
                .argName(ConfigModule.CHANNEL)
                .required()
                .hasArg()
                .desc("The channel to which to connect.")
                .longOpt(ConfigModule.CHANNEL)
                .build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        // TODO: The error we print here isn't fantastic.  A better usage statement would be nice if parsing fails.
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return Guice.createInjector(new ConfigModule(cmd.getOptionValue(ConfigModule.CHANNEL)));
    }
}
