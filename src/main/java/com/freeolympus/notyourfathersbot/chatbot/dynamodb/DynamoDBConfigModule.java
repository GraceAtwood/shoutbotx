package com.freeolympus.notyourfathersbot.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.freeolympus.notyourfathersbot.chatbot.config.ConfigModule.CHANNEL;
import static java.lang.String.format;

public class DynamoDBConfigModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger(DynamoDBConfigModule.class);

    @Inject
    @Provides
    @Singleton
    private DynamoDBMapperConfig provideDynamoDBMapperConfig(
            @Named(CHANNEL) String channelName
    ) {
        logger.info("Producing DynamoDBMapperConfig");
        var mapperConfig = DynamoDBMapperConfig.builder()
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
    private ShoutoutRepository provideShoutoutRepository(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig,
            DynamoDBMapper dynamoDBMapper,
            @Named(CHANNEL) String channel
    ) {
        var shoutoutRepository = new ShoutoutRepository(amazonDynamoDB, mapperConfig, dynamoDBMapper, channel);
        shoutoutRepository.initialize();
        return shoutoutRepository;
    }

    @Inject
    @Singleton
    @Provides
    private ShoutoutSettingRepository provideShoutoutSettingRepository(
            AmazonDynamoDB amazonDynamoDB,
            DynamoDBMapperConfig mapperConfig,
            DynamoDBMapper dynamoDBMapper,
            @Named(CHANNEL) String channel
    ) {
        var shoutoutRepository = new ShoutoutSettingRepository(amazonDynamoDB, mapperConfig, dynamoDBMapper, channel);
        shoutoutRepository.initialize();
        return shoutoutRepository;
    }

    @Override
    protected void configure() {
        super.configure();
    }
}
