package com.github.shoutbotx.chatbot.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DynamoDBTable(tableName = "channel_setting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSetting {

    @DynamoDBHashKey(attributeName = "channel")
    private String channel;

    @DynamoDBAttribute(attributeName = "shoutoutUnknownStreamers")
    private Boolean shoutoutUnknownStreamers;

    public static ChannelSetting createDefaultSettings(String channel) {
        return new ChannelSetting(channel, true);
    }
}
