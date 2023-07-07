package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.meta.entity.SimpleEntity;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TopicTest {

    @Test
    public void verifyTopicsCanBeDifferentiatedUsingProperties() {
        var topicFactory = new TopicFactory(MetaRepository.INSTANCE);

        var simpleEntityTopic = topicFactory.create(
                TopicTest.class,
                SimpleEntity.class,
                SimpleEntity.class
        );

        var anotherSimpleEntityTopic = topicFactory.create(
                TopicTest.class,
                SimpleEntity.class,
                SimpleEntity.class
        );

        assertEquals(simpleEntityTopic, anotherSimpleEntityTopic);
    }
}
