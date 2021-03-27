package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.Objects;
import java.util.function.Supplier;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CLIENT_ID_KEY;

public class TwitchConfigModule extends AbstractModule {

    public static final String TWITCH_AUTH_TOKEN_KEY = "TWITCH_AUTH_TOKEN";
    public static final String TWITCH_IRC_AUTH_TOKEN_KEY = "TWITCH_IRC_AUTH_TOKEN";


    @Provides
    @Singleton
    @Named(TWITCH_AUTH_TOKEN_KEY)
    public String provideTwitchAuthToken() {
        var authToken = System.getenv(TWITCH_AUTH_TOKEN_KEY);
        Objects.requireNonNull(authToken, "Failed to get auth token from env variable TWITCH_AUTH_TOKEN!");

        return authToken;
    }

    @Provides
    @Singleton
    @Named(TWITCH_IRC_AUTH_TOKEN_KEY)
    public String provideTwitchIrcAuthToken() {
        var authToken = System.getenv(TWITCH_IRC_AUTH_TOKEN_KEY);
        Objects.requireNonNull(authToken, "Failed to get auth irc token from env variable TWITCH_IRC_AUTH_TOKEN!");

        return authToken;
    }

    @Provides
    @Singleton
    @Inject
    public Supplier<TwitchClient> provideTwitchClientProvider(
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken,
            @Named(TWITCH_IRC_AUTH_TOKEN_KEY) String ircAuthToken,
            @Named(CLIENT_ID_KEY) String clientId
    ) {
        return () -> TwitchClientBuilder.builder()
                .withClientSecret(authToken)
                .withClientId(clientId)
                .withDefaultAuthToken(new OAuth2Credential("twitch", ircAuthToken))
                .withChatAccount(new OAuth2Credential("twitch", ircAuthToken))
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnableKraken(true)
                .withEnablePubSub(true)
                .build();
    }

    @Override
    protected void configure() {
        super.configure();
    }
}
