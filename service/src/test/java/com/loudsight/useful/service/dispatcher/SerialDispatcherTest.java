package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.junit.jupiter.api.Disabled;

@Disabled
public class SerialDispatcherTest extends DispatcherTest {

    private final TopicFactory topicFactory = new TopicFactory(MetaRepository.getInstance());
    private final Dispatcher clientDispatcher = new SerialDispatcher(topicFactory);
    private final Dispatcher serverDispatcher = new SerialDispatcher(topicFactory);

    @Override
    protected MetaRepository getMetaRepository() {
        return MetaRepository.getInstance();
    }

    @Override
    protected Dispatcher getClientDispatcher() {
        return clientDispatcher;
    }

    @Override
    public Dispatcher getServerDispatcher() {
        return serverDispatcher;
    }
}
