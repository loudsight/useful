package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;

// All test cases live in the abstract DispatcherTest base class; PMD can't see inherited @Test
// methods, so it misreads this concrete fixture as a test class with no test cases.
@SuppressWarnings("PMD.TestClassWithoutTestCases")
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
