package com.freeolympus.notyourfathersbot.chatbot.config;

import com.freeolympus.notyourfathersbot.chatbot.bot.TwitchConfigModule;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.chat.ShoutoutConfigModule;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ConfigModule extends AbstractModule {

    public static final String CHANNEL = "channel";

    private static final String BOT_NAME = "notyourfathersbot";
    public static final String BOT_NAME_KEY = "BOT_NAME_KEY";

    private static final String CLIENT_ID = "2r1wzhjjfcl259bif9vugx1hstvi7v";
    public static final String CLIENT_ID_KEY = "client_id";

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(CLIENT_ID_KEY)).toInstance(CLIENT_ID);
        bind(String.class).annotatedWith(Names.named(BOT_NAME_KEY)).toInstance(BOT_NAME);

        install(new TwitchConfigModule());
        install(new AwsConfigModule());
        install(new ShoutoutConfigModule());
    }
}
