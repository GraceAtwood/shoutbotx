package com.freeolympus.notyourfathersbot.chatbot.config;

import com.freeolympus.notyourfathersbot.chatbot.bot.TwitchConfigModule;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.DynamoDBConfigModule;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigModule extends AbstractModule {

    public static final String CHANNEL = "channel";

    private static final String BOT_NAME = "notyourfathersbot";
    public static final String BOT_NAME_KEY = "BOT_NAME_KEY";

    private static final String CLIENT_ID = "gp762nuuoqcoxypju8c569th9wz7q5";
    public static final String CLIENT_ID_KEY = "client_id";
    private final String channel;

    public ConfigModule(String channel) {
        this.channel = channel;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(CLIENT_ID_KEY)).toInstance(CLIENT_ID);
        bind(String.class).annotatedWith(Names.named(BOT_NAME_KEY)).toInstance(BOT_NAME);
        bind(String.class).annotatedWith(Names.named(CHANNEL)).toInstance(channel);

        install(new TwitchConfigModule());
        install(new AwsConfigModule());
        install(new DynamoDBConfigModule());
    }
}
