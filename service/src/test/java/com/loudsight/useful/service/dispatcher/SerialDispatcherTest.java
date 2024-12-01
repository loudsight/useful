package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.junit.jupiter.api.Disabled;

@Disabled
public class SerialDispatcherTest extends DispatcherTest {

    static {
        MetaRepository.getInstance().register(EnvelopeMeta.getInstance());
        MetaRepository.getInstance().register(TopicMeta.getInstance());
    }
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
