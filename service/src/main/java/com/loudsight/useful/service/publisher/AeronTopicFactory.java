package com.loudsight.useful.service.publisher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.dispatcher.Topic;

public class AeronTopicFactory extends TopicFactory {

    public AeronTopicFactory(MetaRepository metaRepository) {
        super(metaRepository);
    }

    @Override
    public <P, I, O> Topic<P, I, O> create(Class<P> publisherClass, Class<I> requestType, Class<O> responseType) {

        return super.create(publisherClass, requestType, responseType);
    }
}
