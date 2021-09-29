package com.github.shoutbotx.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.Instant;

public class InstantToStringTypeConverter implements DynamoDBTypeConverter<String, Instant> {

    @Override
    public String convert(final Instant instant) {
        return instant.toString();
    }

    @Override
    public Instant unconvert(final String string) {
        return Instant.parse(string);
    }
}