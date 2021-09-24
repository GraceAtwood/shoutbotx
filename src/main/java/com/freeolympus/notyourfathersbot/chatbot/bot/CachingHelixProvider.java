package com.freeolympus.notyourfathersbot.chatbot.bot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.ChannelInformation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachingHelixProvider {

    private final LoadingCache<String, ChannelInformation> channelInformationCache;

    private final TwitchClient twitchClient;
    private final String authToken;

    public CachingHelixProvider(
            TwitchClient twitchClient,
            String authToken
    ) {
        this.twitchClient = twitchClient;
        this.authToken = authToken;

        this.channelInformationCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new ChannelInformationCacheLoader());
    }

    public ChannelInformation getChannelInformation(String broadcasterId) {
        try {
            return channelInformationCache.get(broadcasterId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private class ChannelInformationCacheLoader extends CacheLoader<String, ChannelInformation> {

        @Override
        public ChannelInformation load(@NotNull String broadcasterId) throws Exception {

            return twitchClient.getHelix().getChannelInformation(authToken, List.of(broadcasterId)).execute().getChannels().get(0);
        }
    }
}
