package com.github.shoutbotx.chatbot.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class AwsConfigModule extends AbstractModule {

    private static final String AWS_REGION = "us-east-1";
    public static final String AWS_REGION_KEY = "aws_region";

    @Provides
    @Singleton
    @Inject
    private AmazonDynamoDB provideAmazonDynamoDBClient(
            @Named(AWS_REGION_KEY) String region
    ) {
        return AmazonDynamoDBClientBuilder.standard().withRegion(region)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).build();
    }

    @Override
    protected void configure() {
        super.configure();

        bind(String.class).annotatedWith(Names.named(AWS_REGION_KEY)).toInstance(AWS_REGION);
    }
}
