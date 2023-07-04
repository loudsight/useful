package com.loudsight.useful.service.publisher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.dispatcher.Topic;

public class TopicFactory {

    private final MetaRepository metaRepository;

    public TopicFactory(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public <P, I, O> Topic<P, I, O> create(Class<P> publisherClass, Class<I> requestType, Class<O> responseType) {
        var meta = metaRepository.getMeta(responseType);

        return new Topic<>(publisherClass, requestType, responseType);
    }
}
