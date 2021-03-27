package com.freeolympus.notyourfathersbot.chatbot.cron;

import com.github.twitch4j.TwitchClient;

public interface ChatBotRunnable {

    void run(String channel, TwitchClient twitchClient);
}
