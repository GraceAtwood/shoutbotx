package com.github.shoutbotx.chatbot.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UserEntity {

    private static final Logger logger = LogManager.getLogger();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "guid")
    @Column(unique = true, updatable = false, insertable = true, nullable = false)
    private String id;

    @Column(unique = false, updatable = false, insertable = true, nullable = false)
    private String channel;

    @Column(unique = false, updatable = false, insertable = true, nullable = false)
    private String user;

    @Column(unique = false, updatable = true, insertable = false, nullable = true)
    private Instant lastShoutoutTime;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    private Integer shoutoutIntervalMins;

    public Boolean isReadyForShoutout() {

        if (lastShoutoutTime == null) {
            logger.info("{} has never received a shoutout", user);
            return true;
        }

        return Instant.now().isAfter(lastShoutoutTime.plus(shoutoutIntervalMins, ChronoUnit.MINUTES));
    }
}
