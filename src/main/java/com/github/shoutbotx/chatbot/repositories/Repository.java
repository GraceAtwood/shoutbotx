package com.github.shoutbotx.chatbot.repositories;

import lombok.Getter;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;

public abstract class Repository {

    @Getter
    protected final SessionFactory sessionFactory;

    public Repository(
            SessionFactory sessionFactory
    ) {
        this.sessionFactory = sessionFactory;
    }

    protected EntityManager createEntityManager() {
        return sessionFactory.createEntityManager();
    }
}
