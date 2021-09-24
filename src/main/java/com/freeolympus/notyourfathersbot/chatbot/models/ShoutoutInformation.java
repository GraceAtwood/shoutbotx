package com.freeolympus.notyourfathersbot.chatbot.models;

import com.freeolympus.notyourfathersbot.chatbot.dynamodb.Shoutout;
import com.freeolympus.notyourfathersbot.chatbot.dynamodb.ShoutoutSetting;
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
