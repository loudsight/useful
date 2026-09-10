package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;

// recipient/sender are com.loudsight.useful.entity Subject instances: shared persistence beans
// that are mutable by contract (see the useful.entity EI_EXPOSE_REP exclusion in
// spotbugs-exclude.xml). This @Introspect wire envelope carries them by reference, so there is
// nothing to defensively copy on getRecipient / getSender / setSender.
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "Subject is a shared mutable persistence bean carried by reference; see class comment")
    public Subject getRecipient() {
        return recipient;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP",
            justification = "Subject is a shared mutable persistence bean carried by reference; see class comment")
    public Subject getSender() {
        return sender;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Subject is a shared mutable persistence bean carried by reference; see class comment")
    public void setSender(Subject sender) {
        this.sender = sender;
    }

    public Object getPayload() {
        return payload;
    }

    public BridgeMessageType getPublicationType() {
        return publicationType;
    }

    // Caller-driven cast: the payload type is only known at the call site.
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public  <T> T getData() {
        if (payload instanceof NullValue) {
            return null;
        } else {
            return ClassHelper.uncheckedCast(payload);
        }
    }
}
