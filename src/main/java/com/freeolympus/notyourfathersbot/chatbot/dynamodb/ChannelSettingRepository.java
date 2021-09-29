package com.freeolympus.notyourfathersbot.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class ChannelSettingRepository {
    private static final Logger logger = LogManager.getLogger();

    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapperConfig mapperConfig;
    private final DynamoDBMapper dynamoDBMapper;
    private final String channel;
    private final LoadingCache<String, ChannelSetting> cache;

    private boolean isInitialized;

    public ChannelSettingRepository(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig,
            DynamoDBMapper dynamoDBMapper,
            String channel
    ) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.mapperConfig = mapperConfig;
        this.dynamoDBMapper = dynamoDBMapper;
        this.channel = channel;

        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {

                    @Override
                    public ChannelSetting load(@NotNull String channel) throws Exception {
                        checkIsInitialized();

                        var queryExpression = new DynamoDBQueryExpression<ChannelSetting>()
                                .withHashKeyValues(ChannelSetting.builder().channel(channel).build());

                        var settings = new ArrayList<>(dynamoDBMapper.query(ChannelSetting.class, queryExpression, mapperConfig));

                        if (settings.size() == 0)
                            return ChannelSetting.createDefaultSettings(channel);

                        if (settings.size() > 1)
                            throw new IllegalStateException(format("Found multiple rows for channel %s in settings.  That makes no sense!", channel));

                        return settings.get(0);
                    }
                });
    }

    public ChannelSetting getChannelSetting() {
        try {
            return cache.get(channel);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveChannelSetting(ChannelSetting channelSetting) {
        if (!channelSetting.getChannel().equals(channel)) {
            throw new IllegalStateException(format("Attempted to save a channel setting for channel '%s' but this repository only manages settings for channel %s!", channelSetting.getChannel(), channel));
        }

        dynamoDBMapper.save(channelSetting);
        cache.refresh(channel);
    }

    public void initialize() {
        logger.info("Doing initialization...");

        if (isInitialized) {
            return;
        }

        createTableIfNotExists();

        isInitialized = true;
    }

    private void checkIsInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("Please initialize dynamo db before using it!");
        }
    }

    private void createTableIfNotExists() {
        var createTableRequest = dynamoDBMapper.generateCreateTableRequest(ChannelSetting.class, mapperConfig);

        var provisionedThroughput = new ProvisionedThroughput(20L, 20L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);

        try {
            amazonDynamoDB.createTable(createTableRequest);
        } catch (ResourceInUseException ignored) {
        }
    }

}
