package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;

public class SerialDispatcherTest extends DispatcherTest {

    private static final Dispatcher dispatcherValue = new SerialDispatcher();

    @Override
    protected MetaRepository getMetaRepository() {
        return MetaRepository.INSTANCE;
    }

    @Override
    protected Dispatcher getClientDispatcher() {
        return dispatcherValue;
    }
}
