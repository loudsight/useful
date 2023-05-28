package com.loudsight.utilities.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.utilities.service.dispatcher.Dispatcher;
import com.loudsight.utilities.service.dispatcher.SerialDispatcher;

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
