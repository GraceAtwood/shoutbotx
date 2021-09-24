package com.freeolympus.notyourfathersbot.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;
import static java.lang.String.format;

public class ShoutoutRepository {
    private static final Logger logger = LogManager.getLogger();

    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapperConfig mapperConfig;
    private final DynamoDBMapper dynamoDBMapper;
    private final String channel;
    private final LoadingCache<String, List<Shoutout>> cache;

    private boolean isInitialized;

    public ShoutoutRepository(
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
                    public List<Shoutout> load(@NotNull String user) throws Exception {
                        checkIsInitialized();

                        var queryExpression = new DynamoDBQueryExpression<Shoutout>()
                                .withConsistentRead(false)
                                .withIndexName(Shoutout.channelUserIndexName)
                                .withHashKeyValues(Shoutout.builder().channel(channel).build())
                                .withRangeKeyCondition("user", new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(user)));

                        return new ArrayList<>(dynamoDBMapper.query(Shoutout.class, queryExpression, mapperConfig));
                    }
                });
    }

    public List<Shoutout> getShoutoutsForUser(String user) {
        try {
            return cache.get(user);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Shoutout getRandomShoutout(String user) {
        var shoutouts = getShoutoutsForUser(user);

        if (shoutouts.isEmpty())
            return null;

        return shoutouts.get(ThreadLocalRandom.current().nextInt(shoutouts.size()) % shoutouts.size());
    }

    public void saveShoutout(String user, String text) {
        checkIsInitialized();

        dynamoDBMapper.save(Shoutout.builder()
                .id(UUID.randomUUID().toString())
                .channel(channel)
                .user(user)
                .message(text)
                .timeAdded(Instant.now())
                .build(), mapperConfig);

        cache.refresh(user);
    }

    public void deleteShoutout(Shoutout shoutout) {
        checkIsInitialized();


        dynamoDBMapper.delete(shoutout);
        cache.invalidate(shoutout.getUser());
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
        var createTableRequest = dynamoDBMapper.generateCreateTableRequest(Shoutout.class, mapperConfig);

        var provisionedThroughput = new ProvisionedThroughput(20L, 20L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);
        createTableRequest.getGlobalSecondaryIndexes().forEach(x -> {
            x.setProvisionedThroughput(provisionedThroughput);
            x.setProjection(new Projection().withProjectionType(ProjectionType.ALL));
        });

        try {
            amazonDynamoDB.createTable(createTableRequest);
        } catch (ResourceInUseException ignored) {
        }
    }

}
