package com.github.shoutbotx.chatbot.repositories;

import com.github.shoutbotx.chatbot.entities.ChannelEntity;
import com.github.shoutbotx.chatbot.entities.ChannelEntity_;
import com.github.shoutbotx.chatbot.entities.ShoutoutEntity;
import com.github.shoutbotx.chatbot.entities.ShoutoutEntity_;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.hibernate.SessionFactory;

import java.util.List;

import static com.github.shoutbotx.chatbot.config.ConfigModule.CHANNEL;

public class ChannelRepository extends Repository {

    private final String channel;

    @Inject
    public ChannelRepository(
            SessionFactory sessionFactory,
            @Named(CHANNEL) String channel
    ) {
        super(sessionFactory);
        this.channel = channel;
    }

    public ChannelEntity getChannel() {
        var entityManager = createEntityManager();

        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(ChannelEntity.class);
        var root = cq.from(ChannelEntity.class);
        cq.select(cq.from(ChannelEntity.class))
                .where(cb.equal(root.get(ChannelEntity_.channel), channel));
        return entityManager.createQuery(cq).getSingleResult();
    }
}
