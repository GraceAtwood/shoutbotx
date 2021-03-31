package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.freeolympus.notyourfathersbot.chatbot.Main;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.lang.NullArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CLIENT_ID_KEY;

public class TwitchConfigModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger(TwitchConfigModule.class);

    public static final String TWITCH_AUTH_TOKEN_KEY = "TWITCH_AUTH_TOKEN";
    public static final String TWITCH_IRC_AUTH_TOKEN_KEY = "TWITCH_IRC_AUTH_TOKEN";

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

        var twitchAuthToken = System.getenv(TWITCH_AUTH_TOKEN_KEY);
        var twitchIrcAuthToken = System.getenv(TWITCH_IRC_AUTH_TOKEN_KEY);

        if (twitchAuthToken == null) {
            logger.error("Failed to retrieve twitchAuthToken from env variable {}!", TWITCH_AUTH_TOKEN_KEY);
            throw new NullArgumentException(TWITCH_AUTH_TOKEN_KEY);
        }

        if (twitchIrcAuthToken == null) {
            logger.error("Failed to retrieve twitchIrcAuthToken from env variable {}!", TWITCH_IRC_AUTH_TOKEN_KEY);
            throw new NullArgumentException(TWITCH_IRC_AUTH_TOKEN_KEY);
        }

        bind(String.class).annotatedWith(Names.named(TWITCH_AUTH_TOKEN_KEY)).toInstance(twitchAuthToken);
        bind(String.class).annotatedWith(Names.named(TWITCH_IRC_AUTH_TOKEN_KEY)).toInstance(twitchIrcAuthToken);
    }
}
