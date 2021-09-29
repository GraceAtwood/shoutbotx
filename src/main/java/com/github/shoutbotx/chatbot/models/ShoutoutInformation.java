package com.github.shoutbotx.chatbot.models;

import com.github.shoutbotx.chatbot.dynamodb.Shoutout;
import com.github.shoutbotx.chatbot.dynamodb.ShoutoutSetting;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ShoutoutInformation {

    private String user;
    private String channel;
    private List<Shoutout> shoutouts;
    private ShoutoutSetting shoutoutSetting;
    private Instant forceShoutoutAfter;
}
