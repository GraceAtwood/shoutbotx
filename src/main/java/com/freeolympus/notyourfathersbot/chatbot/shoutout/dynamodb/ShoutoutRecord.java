package com.freeolympus.notyourfathersbot.chatbot.shoutout.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@DynamoDBTable(tableName = "REPLACE_ME")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoutoutRecord {

    @DynamoDBHashKey(attributeName = "channel")
    private String channel;

    @DynamoDBRangeKey(attributeName = "viewer")
    private String viewer;

    @DynamoDBTypeConverted(converter = InstantToStringTypeConverter.class)
    @DynamoDBAttribute(attributeName = "shoutoutTime")
    private Instant shoutoutTime;

    public static class InstantToStringTypeConverter implements DynamoDBTypeConverter<String, Instant> {

        @Override
        public String convert(final Instant instant) {
            return instant.toString();
        }

        @Override
        public Instant unconvert(final String string) {
            return Instant.parse(string);
        }
    }
}
