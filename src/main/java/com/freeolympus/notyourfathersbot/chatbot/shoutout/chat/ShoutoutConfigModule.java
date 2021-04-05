package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;
import static java.lang.String.format;

public class ShoutoutConfigModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger(ShoutoutConfigModule.class);

    private static final String SUBSCRIPTION_TABLE = "notyourfathersbot_shoutout_records";
    private static final String SUBSCRIPTION_TABLE_KEY = "subscription_table";

    @Inject
    @Provides
    @Singleton
    private DynamoDBMapperConfig provideDynamoDBMapperConfig(
            @Named(SUBSCRIPTION_TABLE_KEY) String tableName
    ) {
        logger.info("Producing DynamoDBMapperConfig");
        var mapperConfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                .withConversionSchema(ConversionSchemas.V2_COMPATIBLE)
                .build();
        logger.info("Produced DynamoDBMapperConfig");
        return mapperConfig;
    }

    @Inject
    @Singleton
    @Provides
    private DynamoDBMapper provideDynamoDBMapper(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig
    ) {
        logger.info("Producing dynamodb mapper...");
        var dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, mapperConfig);
        logger.info("Produced dynamodb mapper");
        return dynamoDBMapper;
    }

    @Inject
    @Singleton
    @Provides
    public CustomShoutouts provideCustomShoutouts(
            @Named(CHANNEL) String channelName
    ) {

        logger.info("Begin loading shout outs configuration...");
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(format("shoutouts/%s.json", channelName.toLowerCase()))) {
            var shoutoutJson = IOUtils.toString(Objects.requireNonNull(inputStream, format("Failed to load shoutouts for '%s'!  Does the file exist?", channelName)), Charset.defaultCharset());

            var customShoutouts = new ObjectMapper().readValue(shoutoutJson, CustomShoutouts.class);
            logger.info("Finished loading shout outs configuration.");
            return customShoutouts;

        } catch (Throwable t) {
            logger.error("An error occurred while loading the shouts config!", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(SUBSCRIPTION_TABLE_KEY)).toInstance(SUBSCRIPTION_TABLE);
    }
}
