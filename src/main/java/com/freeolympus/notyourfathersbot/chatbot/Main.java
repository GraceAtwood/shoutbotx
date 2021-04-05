package com.freeolympus.notyourfathersbot.chatbot;

import com.freeolympus.notyourfathersbot.chatbot.bot.ChatBotFactory;
import com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule;
import com.google.inject.*;
import com.google.inject.name.Names;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;

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

            logger.info("Injecting ChatBotFactory");
            var chatBotFactory = injector.getInstance(ChatBotFactory.class);
            logger.info("Injected ChatBotFactory");

            var channelName = injector.getInstance(Key.get(String.class, Names.named(CHANNEL)));

            logger.info("Constructing chatbot");
            var chatBot = chatBotFactory.construct(channelName);
            logger.info("Constructed chatbot");

            chatBot.start();
        } catch (Throwable throwable) {
            logger.error("An unhandled error occurred!", throwable);
        }
    }

    private static Injector processCommandlineArgs(String[] args) {

        final var options = new Options();

        options.addOption(Option.builder()
                .argName(CHANNEL)
                .required()
                .hasArg()
                .desc("The channel to which to connect.")
                .longOpt(CHANNEL)
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        // TODO: The error we print here isn't fantastic.  A better usage statement would be nice if parsing fails.
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("cv-config", options);

            System.exit(1);
        }

        final var finalCmd = cmd;

        AbstractModule commandLineModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(String.class)
                        .annotatedWith(Names.named(CHANNEL))
                        .toInstance(finalCmd.getOptionValue(CHANNEL));
            }
        };

        return Guice
                .createInjector(commandLineModule)
                .createChildInjector(new ConfigModule());
    }
}
