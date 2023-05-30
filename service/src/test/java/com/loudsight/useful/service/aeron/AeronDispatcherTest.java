package com.loudsight.useful.service.aeron;

import com.loudsight.meta.MetaRepository;
//import com.loudsight.utilities.service.dispatcher.aeron.AeronDispatcher;
//import com.loudsight.utilities.service.config.ServiceTestConfig;
//import com.loudsight.utilities.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.DispatcherTest;
import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled
//@SpringJUnitConfig(classes = {ServiceTestConfig.class})
public class AeronDispatcherTest extends DispatcherTest {

    @Autowired
    MetaRepository metaRepository;

    private final Dispatcher serialDispatcher = new AeronDispatcher();

    private final Dispatcher serverDispatcher = new AeronDispatcher();;

    @Override
    public Dispatcher getClientDispatcher() {
        return serialDispatcher;
    }

    @Override
    public Dispatcher getServerDispatcher() {
        return serverDispatcher;
    }

    @Override
    public MetaRepository getMetaRepository() {
        return metaRepository;
    }

//    @Test
//    public void testClusteredPublishSubscribe() throws Exception {
//
//        testPublishSubscribe(metaRepository, serialDispatcher, serverDispatcher);
//    }
}
