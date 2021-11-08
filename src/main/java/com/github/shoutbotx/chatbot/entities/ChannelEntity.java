package com.github.shoutbotx.chatbot.entities;

import lombok.Data;

import javax.persistence.*;

@Entity(name = "Channel")
@Table(name = "channel")
@Data
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "guid")
    @Column(unique = true, updatable = false, insertable = true, nullable = false)
    private String id;

    @Column(unique = true, updatable = false, insertable = true, nullable = false)
    private String channel;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    private Boolean shoutoutUnknownStreamers;

    @Column(unique = false, updatable = true, insertable = true, nullable = false)
    private Integer maxShoutouts;
}
