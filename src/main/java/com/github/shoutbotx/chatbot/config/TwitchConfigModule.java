package com.github.shoutbotx.chatbot.config;

import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.lang.NullArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TwitchConfigModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger();

    public static final String TWITCH_AUTH_TOKEN_KEY = "TWITCH_AUTH_TOKEN";
    public static final String TWITCH_IRC_AUTH_TOKEN_KEY = "TWITCH_IRC_AUTH_TOKEN";
    public static final String CLIENT_ID_KEY = "CLIENT_ID";

    @Provides
    @Singleton
    @Inject
    public TwitchClient provideTwitchClient(
            @Named(TWITCH_AUTH_TOKEN_KEY) String authToken,
            @Named(TWITCH_IRC_AUTH_TOKEN_KEY) String ircAuthToken,
            @Named(CLIENT_ID_KEY) String clientId
    ) {

        var credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(new TwitchIdentityProvider(clientId, authToken, ""));

        return TwitchClientBuilder.builder()
                .withCredentialManager(credentialManager)
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
        var clientId = System.getenv(CLIENT_ID_KEY);

        if (twitchAuthToken == null) {
            logger.error("Failed to retrieve twitchAuthToken from env variable {}!", TWITCH_AUTH_TOKEN_KEY);
            throw new NullArgumentException(TWITCH_AUTH_TOKEN_KEY);
        }

        if (twitchIrcAuthToken == null) {
            logger.error("Failed to retrieve twitchIrcAuthToken from env variable {}!", TWITCH_IRC_AUTH_TOKEN_KEY);
            throw new NullArgumentException(TWITCH_IRC_AUTH_TOKEN_KEY);
        }

        if (clientId == null) {
            logger.error("Failed to retrieve clientId from env variable {}!", CLIENT_ID_KEY);
            throw new NullArgumentException(CLIENT_ID_KEY);
        }

        bind(String.class).annotatedWith(Names.named(CLIENT_ID_KEY)).toInstance(clientId);
        bind(String.class).annotatedWith(Names.named(TWITCH_AUTH_TOKEN_KEY)).toInstance(twitchAuthToken);
        bind(String.class).annotatedWith(Names.named(TWITCH_IRC_AUTH_TOKEN_KEY)).toInstance(twitchIrcAuthToken);
    }
}
