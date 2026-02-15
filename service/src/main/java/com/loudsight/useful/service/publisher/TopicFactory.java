package com.loudsight.useful.service.publisher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.dispatcher.Topic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TopicFactory {

    /*private final */MetaRepository metaRepository;

    public TopicFactory(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public <P, I, O> Topic<P, I, O> create(Class<P> publisherClass, Class<I> requestType, Class<O> responseType) {
//        var meta = metaRepository.getMeta(responseType);

        return new Topic<>(publisherClass, requestType, responseType);
    }

    /**
     * Creates a canonical Topic with required service and name properties for registry resolution.
     * 
     * @param publisherClass the publisher class
     * @param requestType the request type
     * @param responseType the response type  
     * @param service the service name for registry lookup
     * @param name the topic name for registry lookup
     * @return Topic with immutable properties containing service and name
     */
    public <P, I, O> Topic<P, I, O> createCanonical(Class<P> publisherClass, Class<I> requestType, Class<O> responseType, String service, String name) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("service", service);
        properties.put("name", name);
        
        return new Topic<>(publisherClass, requestType, responseType, Collections.unmodifiableMap(properties));
    }
}
