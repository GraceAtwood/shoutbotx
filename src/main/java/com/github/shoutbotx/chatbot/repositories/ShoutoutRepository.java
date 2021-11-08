package com.github.shoutbotx.chatbot.repositories;

import com.github.shoutbotx.chatbot.entities.ShoutoutEntity;
import com.github.shoutbotx.chatbot.entities.ShoutoutEntity_;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.shoutbotx.chatbot.config.ConfigModule.CHANNEL;

public class ShoutoutRepository extends Repository {

    private final String channel;

    @Inject
    public ShoutoutRepository(
            SessionFactory sessionFactory,
            @Named(CHANNEL) String channel
    ) {
        super(sessionFactory);
        this.channel = channel;
    }

    public Long countShoutouts() {
        var entityManager = createEntityManager();

        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(ShoutoutEntity.class);
        cq.select(cb.count(cq.from(ShoutoutEntity.class)))
                .where(cb.equal(root.get(ShoutoutEntity_.channel), channel));
        return entityManager.createQuery(cq).getSingleResult();
    }

    public List<ShoutoutEntity> getShoutoutsForUser(String user) {
        var entityManager = createEntityManager();

        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(ShoutoutEntity.class);
        var root = cq.from(ShoutoutEntity.class);

        cq.select(cq.from(ShoutoutEntity.class))
                .where(cb.and(
                        cb.equal(root.get(ShoutoutEntity_.channel), channel),
                        cb.equal(root.get(ShoutoutEntity_.user), user)))
                .orderBy(cb.desc(root.get(ShoutoutEntity_.timeAdded)));
        var resultList = entityManager.createQuery(cq).getResultList();

        if (resultList == null) {
            return Collections.emptyList();
        }

        return resultList;
    }

    public ShoutoutEntity addShoutout(ShoutoutEntity shoutout) {
        var entityManager = createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(shoutout);

            transaction.commit();
        } catch (Throwable throwable) {
            transaction.rollback();
            throw throwable;
        }

        return shoutout;
    }

    public void deleteShoutout(ShoutoutEntity shoutout) {
        var entityManager = createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.remove(shoutout);

            transaction.commit();
        } catch (Throwable throwable) {
            transaction.rollback();
            throw throwable;
        }
    }

    public void deleteShoutoutById(String id) {
        var entityManager = createEntityManager();
        var shoutoutEntity = entityManager.find(ShoutoutEntity.class, id);
        deleteShoutout(shoutoutEntity);
    }

    public ShoutoutEntity updateShoutout(ShoutoutEntity shoutout) {
        var entityManager = createEntityManager();

        var transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.merge(shoutout);

            transaction.commit();
        } catch (Throwable throwable) {
            transaction.rollback();
            throw throwable;
        }

        return shoutout;
    }
}
