package com.github.shoutbotx.chatbot.config;

import com.github.shoutbotx.chatbot.bot.TwitchConfigModule;
import com.github.shoutbotx.chatbot.commands.CommandConfigModule;
import com.github.shoutbotx.chatbot.dynamodb.DynamoDBConfigModule;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigModule extends AbstractModule {

    public static final String CHANNEL = "channel";

    private final String channel;

    public ConfigModule(String channel) {
        this.channel = channel;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(CHANNEL)).toInstance(channel);

        install(new TwitchConfigModule());
        install(new AwsConfigModule());
        install(new DynamoDBConfigModule());
        install(new CommandConfigModule());
    }
}
