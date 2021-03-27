package com.freeolympus.notyourfathersbot.chatbot.shoutout.chat;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomShoutouts {

    private Map<String, CustomShoutoutInfo> shoutoutInfoMap = new LinkedHashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {

        var shoutoutInfo = new ObjectMapper().convertValue(value, CustomShoutoutInfo.class);

        shoutoutInfoMap.put(key, shoutoutInfo);
    }

    public CustomShoutoutInfo forStreamer(String streamer) {
        return shoutoutInfoMap.get(streamer);
    }
}
