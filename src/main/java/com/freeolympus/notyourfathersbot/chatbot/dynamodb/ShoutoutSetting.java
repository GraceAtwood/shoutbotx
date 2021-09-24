package com.freeolympus.notyourfathersbot.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@DynamoDBTable(tableName = "shoutout_setting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoutoutSetting {

    @DynamoDBHashKey(attributeName = "channel")
    private String channel;

    @DynamoDBRangeKey(attributeName = "user")
    private String user;

    @DynamoDBAttribute(attributeName = "shoutoutIntervalMins")
    private Integer shoutoutIntervalMins;

    @DynamoDBTypeConverted(converter = InstantToStringTypeConverter.class)
    @DynamoDBAttribute(attributeName = "lastShoutoutTime")
    private Instant lastShoutoutTime;

    @DynamoDBTypeConverted(converter = InstantToStringTypeConverter.class)
    @DynamoDBAttribute(attributeName = "forceShoutoutAfter")
    private Instant forceShoutoutAfter;
}
