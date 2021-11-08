package com.github.shoutbotx.chatbot.config;

import com.github.shoutbotx.chatbot.entities.ChannelEntity;
import com.github.shoutbotx.chatbot.entities.ShoutoutEntity;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Properties;

public class PersistenceConfig extends AbstractModule {
    private static final Logger logger = LogManager.getLogger();

    @Provides
    @Inject
    @Singleton
    public SessionFactory provideSessionFactory() {
        try {

            Properties properties = new Properties();
            properties.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
            properties.put(Environment.URL, "jdbc:mysql://localhost:3306/jpa_jbd?serverTimezone=UTC&useSSL=false");
            properties.put(Environment.USER, "root");
            properties.put(Environment.PASS, "password");
            properties.put(Environment.FORMAT_SQL, "true");
            properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
            properties.put(Environment.SHOW_SQL, "true");
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            properties.put(Environment.HBM2DDL_AUTO, "create");
            properties.put(Environment.POOL_SIZE, "5");

            return new Configuration()
                    .setProperties(properties)
                    .addAnnotatedClass(ChannelEntity.class)
                    .addAnnotatedClass(ShoutoutEntity.class)
                    .buildSessionFactory();

        } catch (Throwable ex) {
            logger.error(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
}
