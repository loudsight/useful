package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.service.publisher.TopicFactory;

public class SerialDispatcher extends ParallelDispatcher {
    public SerialDispatcher(TopicFactory topicFactory) {
        super(topicFactory, 1);
    }
}
