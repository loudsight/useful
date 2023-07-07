package com.loudsight.useful.service.aeron;

import com.loudsight.useful.service.dispatcher.ParallelDispatcher;
import com.loudsight.useful.service.publisher.TopicFactory;


public class AeronDispatcher extends ParallelDispatcher {

    public AeronDispatcher(TopicFactory topicFactory) {

        super(topicFactory, 1);
    }
}
