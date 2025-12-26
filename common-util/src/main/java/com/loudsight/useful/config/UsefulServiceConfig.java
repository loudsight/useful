package com.loudsight.useful.config;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.SerialDispatcher;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandles;

@Configuration
public class UsefulServiceConfig {
    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    /**
     * Creates TopicFactory bean for publisher/subscriber topics.
     *
     * @return TopicFactory initialized with MetaRepository
     */
    @Bean
    public TopicFactory topicFactory() {
        logger.logInfo("Creating TopicFactory bean");
        return new TopicFactory(MetaRepository.getInstance());
    }

    /**
     * Creates Dispatcher bean for message processing.
     *
     * @param topicFactory the TopicFactory bean
     * @return SerialDispatcher for sequential message processing
     */
    @Bean
    public Dispatcher dispatcher(TopicFactory topicFactory) {
        logger.logInfo("Creating Dispatcher bean");
        return new SerialDispatcher(topicFactory);
    }

    @Bean
    MetaRepository metaRepository() {
        return MetaRepository.getInstance();
    }
}
