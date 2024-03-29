package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;

public class SerialDispatcherTest extends DispatcherTest {

    private final TopicFactory topicFactory = new TopicFactory(MetaRepository.getInstance());
    private final Dispatcher dispatcherValue = new SerialDispatcher(topicFactory);

    @Override
    protected MetaRepository getMetaRepository() {
        return MetaRepository.getInstance();
    }

    @Override
    protected Dispatcher getClientDispatcher() {
        return dispatcherValue;
    }

}
