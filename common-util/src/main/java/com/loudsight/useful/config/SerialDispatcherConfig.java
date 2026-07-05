package com.loudsight.useful.config;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.SerialDispatcher;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.lang.invoke.MethodHandles;

/**
 * Provides the in-process {@link SerialDispatcher} as the {@code Dispatcher} bean.
 * Split out of {@link UsefulServiceConfig} so consumers that only need
 * {@code TopicFactory}/{@code MetaRepository} don't also register a competing
 * {@code Dispatcher} bean when a different transport (e.g. Aeron) is already providing one.
 * {@code @ConditionalOnMissingBean} makes this a fallback: it only activates when nothing
 * else in the context (e.g. a real Aeron dispatcher) has already supplied a {@code Dispatcher}.
 */
@Configuration
public class SerialDispatcherConfig {
    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    @Lazy
    @Bean
    @ConditionalOnMissingBean(Dispatcher.class)
    public Dispatcher dispatcher(TopicFactory topicFactory) {
        logger.logInfo("Creating Dispatcher bean");
        return new SerialDispatcher(topicFactory);
    }
}
