package com.loudsight.useful.config;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.helper.logging.LoggingHelper;
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

    @Bean
    MetaRepository metaRepository() {
        return MetaRepository.getInstance();
    }
}
