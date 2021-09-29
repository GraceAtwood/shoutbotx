package com.github.shoutbotx.chatbot.dynamodb;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class ShoutoutSettingRepository {
    private static final Logger logger = LogManager.getLogger();

    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapperConfig mapperConfig;
    private final DynamoDBMapper dynamoDBMapper;
    private final String channel;
    private final LoadingCache<String, ShoutoutSetting> cache;

    private boolean isInitialized;

    public ShoutoutSettingRepository(
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
                    public ShoutoutSetting load(@NotNull String user) throws Exception {
                        checkIsInitialized();

                        var queryExpression = new DynamoDBQueryExpression<ShoutoutSetting>()
                                .withHashKeyValues(ShoutoutSetting.builder().channel(channel).build())
                                .withRangeKeyCondition("user", new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(user)));

                        var settings = new ArrayList<>(dynamoDBMapper.query(ShoutoutSetting.class, queryExpression, mapperConfig));

                        if (settings.size() == 0)
                            return ShoutoutSetting.createDefaultSettings(channel, user);

                        if (settings.size() > 1)
                            throw new IllegalStateException(format("The shoutout setting query for channel '%s' and user '%s' produced multiple results!", channel, user));

                        return settings.get(0);
                    }
                });
    }

    public ShoutoutSetting getShoutoutSettingForUser(String user) {
        try {
            return cache.get(user);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveShoutoutSetting(ShoutoutSetting shoutoutSetting) {
        checkIsInitialized();

        dynamoDBMapper.save(shoutoutSetting, mapperConfig);
        cache.refresh(shoutoutSetting.getUser());
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
        var createTableRequest = dynamoDBMapper.generateCreateTableRequest(ShoutoutSetting.class, mapperConfig);

        var provisionedThroughput = new ProvisionedThroughput(20L, 20L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);

        try {
            amazonDynamoDB.createTable(createTableRequest);
        } catch (ResourceInUseException ignored) {
        }
    }

}
