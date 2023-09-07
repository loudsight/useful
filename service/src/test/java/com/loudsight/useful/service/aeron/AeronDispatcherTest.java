package com.loudsight.useful.service.aeron;

import com.loudsight.meta.MetaRepository;
//import com.loudsight.utilities.service.dispatcher.aeron.AeronDispatcher;
//import com.loudsight.utilities.service.config.ServiceTestConfig;
//import com.loudsight.utilities.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.DispatcherTest;
import com.loudsight.useful.service.dispatcher.SerialDispatcher;
import com.loudsight.useful.service.publisher.AeronTopicFactory;
import com.loudsight.useful.service.publisher.TopicFactory;
import io.aeron.Aeron;
import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {AeronDispatcherTestTestConfig.class})
public class AeronDispatcherTest extends DispatcherTest {

    private static final AeronTopicFactory topicFactory = new AeronTopicFactory(MetaRepository.getInstance());
    private final Dispatcher dispatcherValue;

    @Autowired
    AeronDispatcherTest(Aeron aeron) {
        dispatcherValue = new AeronDispatcher(aeron, topicFactory);
    }


    @Override
    protected MetaRepository getMetaRepository() {
        return MetaRepository.getInstance();
    }

    @Override
    protected Dispatcher getClientDispatcher() {
        return dispatcherValue;
    }

    @Disabled
    @Override
    public void testWildCardSubscription() {
        //
    }
    @Disabled
    @Override
    public void testPublishSubscribe() throws Exception {
        //
    }
}
