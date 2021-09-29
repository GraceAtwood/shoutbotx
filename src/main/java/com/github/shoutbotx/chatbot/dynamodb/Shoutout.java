package com.github.shoutbotx.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@DynamoDBTable(tableName = "shoutout")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shoutout {

    public static final String channelUserIndexName = "channel-user-index";

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBIndexHashKey(attributeName = "channel", globalSecondaryIndexName = channelUserIndexName)
    private String channel;

    @DynamoDBIndexRangeKey(attributeName = "user", globalSecondaryIndexName = channelUserIndexName)
    private String user;

    @DynamoDBAttribute(attributeName = "message")
    private String message;

    @DynamoDBTypeConverted(converter = InstantToStringTypeConverter.class)
    @DynamoDBAttribute(attributeName = "timeAdded")
    private Instant timeAdded;
}
