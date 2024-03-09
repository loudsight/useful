package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;

@Introspect(clazz = Publication.class)
public class Publication {
    private final Address to;
    private final Address replyTo;
    private final Subject recipient;
    private Subject sender;
    private final Object payload;
    BridgeMessageType publicationType;

    public Publication(
            Address to,
            Address replyTo,
            Subject recipient,
            Subject sender,
            Object payload,
            BridgeMessageType publicationType
    ) {
        this.to = to;
        this.replyTo = replyTo;
        this.recipient = recipient;
        this.sender = sender;
        this.payload = payload;
        this.publicationType = publicationType;
    }


    public Publication(Object payload) {
        this(null, null, null, null, payload, null);
    }

    public Address getTo() {
        return to;
    }

    public Address getReplyTo() {
        return replyTo;
    }

    public Subject getRecipient() {
        return recipient;
    }

    public Subject getSender() {
        return sender;
    }

    public void setSender(Subject sender) {
        this.sender = sender;
    }

    public Object getPayload() {
        return payload;
    }

    public BridgeMessageType getPublicationType() {
        return publicationType;
    }

    public  <T> T getData() {
        if (payload instanceof NullValue) {
            return null;
        } else {
            return (T)payload;
        }
    }
}
