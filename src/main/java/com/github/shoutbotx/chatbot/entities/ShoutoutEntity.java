package com.github.shoutbotx.chatbot.entities;

import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity(name = "Shoutout")
@Table(name = "shoutout")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoutoutEntity {
    private static final Logger logger = LogManager.getLogger();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "guid")
    @Column(unique = true, updatable = false, insertable = true, nullable = false)
    private String id;

    @Column(unique = false, updatable = false, insertable = true, nullable = false)
    private String channel;

    @Column(unique = false, updatable = false, insertable = true, nullable = false)
    private String user;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    private String message;

    @Column(unique = false, updatable = false, insertable = true, nullable = false)
    private Instant timeAdded;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    private Double weight;


}
