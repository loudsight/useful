package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.Listener;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.loudsight.useful.service.dispatcher.Topic.WILDCARD_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    // Wildcard delivery is implementation-specific; the in-memory dispatchers support it.
    // The dispatcher is a fixture owned by this class, as in DispatcherTest.
    @SuppressWarnings("PMD.CloseResource")
    @Test
    public void testWildCardSubscription() {
        Dispatcher dispatcher = getClientDispatcher();
        var received = new Listener<List<Integer>>();
        List<Integer> results = new ArrayList<>();

        Function<Object, Object> handler =
                (payload) -> {
                    results.add((Integer) payload);

                    if (results.size() == 100) {
                        received.accept(results);
                    }

                    return null;
                };

        dispatcher.subscribe(WILDCARD_ADDRESS, handler);

        for (int i = 0; i < 100; i++) {
            var address = new Topic<>(DispatcherTest.class, Integer.class, Integer.class,
                    Map.of(
                            "service", "persistence",
                            "name", "FIND_ADDRESS",
                            "id", String.valueOf(i),
                            "id2", String.valueOf(i)));
            dispatcher.publish(address, i);
        }
        for (int i = 0; i < 100; i++) {
            received.getResult(); // blocks until all results received
            assertEquals(i, results.get(i));
        }
    }
}
