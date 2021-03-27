package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb.ShoutoutRecordRepository;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public class ShoutoutConfigModule extends AbstractModule {

    private static final String SUBSCRIPTION_TABLE = "notyourfathersbot_shoutout_records";
    private static final String SUBSCRIPTION_TABLE_KEY = "subscription_table";

    @Inject
    @Provides
    @Singleton
    private DynamoDBMapperConfig provideDynamoDBMapperConfig(
            @Named(SUBSCRIPTION_TABLE_KEY) String tableName
    ) {
        return DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                .withConversionSchema(ConversionSchemas.V2_COMPATIBLE)
                .build();
    }

    @Inject
    @Singleton
    @Provides
    private DynamoDBMapper provideDynamoDBMapper(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig
    ) {
        return new DynamoDBMapper(amazonDynamoDB, mapperConfig);
    }

    @Inject
    @Singleton
    @Provides
    public CustomShoutouts provideCustomShoutouts() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("shoutouts.json")) {
            var shoutoutJson = IOUtils.toString(Objects.requireNonNull(inputStream), Charset.defaultCharset());

            return new ObjectMapper().readValue(shoutoutJson, CustomShoutouts.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(SUBSCRIPTION_TABLE_KEY)).toInstance(SUBSCRIPTION_TABLE);
    }
}
