package com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;

import static java.lang.String.format;

public class ShoutoutRecordRepository {

    private final AmazonDynamoDB amazonDynamoDB;
    private final DynamoDBMapperConfig mapperConfig;
    private final DynamoDBMapper dynamoDBMapper;

    private boolean isInitialized;

    @Inject
    public ShoutoutRecordRepository(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig,
            DynamoDBMapper dynamoDBMapper
    ) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.mapperConfig = mapperConfig;
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Instant getShoutoutTime(String channel, String viewer) {
        var queryExpression = new DynamoDBQueryExpression<ShoutoutRecord>()
                .withHashKeyValues(ShoutoutRecord.builder().channel(channel).build())
                .withRangeKeyCondition("viewer", new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(viewer)));

        var shoutoutRecords = new ArrayList<ShoutoutRecord>(dynamoDBMapper.query(ShoutoutRecord.class, queryExpression, mapperConfig));

        if (shoutoutRecords.size() == 0)
            return null;

        if (shoutoutRecords.size() > 1)
            throw new IllegalStateException(format("The shoutout record query for channel '%s' and viewer '%s' produced multiple results!", channel, viewer));

        return shoutoutRecords.get(0).getShoutoutTime();
    }

    public void updateShoutoutTimeToNow(String channel, String viewer) {
        dynamoDBMapper.save(ShoutoutRecord.builder()
                .shoutoutTime(Instant.now())
                .channel(channel)
                .viewer(viewer)
                .build(), mapperConfig);
    }

    public void initialize() {
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
        var createTableRequest = dynamoDBMapper.generateCreateTableRequest(ShoutoutRecord.class, mapperConfig);

        var provisionedThroughput = new ProvisionedThroughput(20L, 20L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);
        /*createTableRequest.getGlobalSecondaryIndexes().forEach(x -> {
            x.setProvisionedThroughput(provisionedThroughput);
            x.setProjection(new Projection().withProjectionType(ProjectionType.ALL));
        });*/

        try {
            amazonDynamoDB.createTable(createTableRequest);
        } catch (ResourceInUseException ignored) {
        }
    }

}
