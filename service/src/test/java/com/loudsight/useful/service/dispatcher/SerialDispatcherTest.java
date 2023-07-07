package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;

public class SerialDispatcherTest extends DispatcherTest {

    private static final TopicFactory topicFactory = new TopicFactory(MetaRepository.INSTANCE);
    private static final Dispatcher dispatcherValue = new SerialDispatcher(topicFactory);

    @Override
    protected MetaRepository getMetaRepository() {
        return MetaRepository.INSTANCE;
    }

    @Override
    protected Dispatcher getClientDispatcher() {
        return dispatcherValue;
    }
}
