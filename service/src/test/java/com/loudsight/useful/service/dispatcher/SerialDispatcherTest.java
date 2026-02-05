package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;

public class SerialDispatcherTest extends DispatcherTest {

    private final TopicFactory topicFactory = new TopicFactory(MetaRepository.getInstance());
    private final SerialDispatcher clientDispatcher = new SerialDispatcher(topicFactory);
    private final SerialDispatcher serverDispatcher = new SerialDispatcher(topicFactory);

    public SerialDispatcherTest() {
        // Register server dispatcher as peer of client dispatcher for cross-dispatcher communication
        clientDispatcher.registerPeerDispatcher(serverDispatcher);
    }

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
