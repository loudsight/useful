package com.loudsight.useful.service.aeron;

import com.loudsight.meta.serialization.EntityTransform;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.service.dispatcher.Subscription;
import com.loudsight.useful.service.dispatcher.Topic;
import io.aeron.FragmentAssembler;
import io.aeron.logbuffer.FragmentHandler;
import kotlin.collections.ByteIterator;

import java.util.function.Consumer;

public record AeronSubscription<P, Q, A>(
            io.aeron.Subscription aeronSubscription,
            Topic<P, Q, A> topic
            ) implements Subscription<P, Q, A> {

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public A onEvent(Subject sender, Q payload) {
            return null;
        }

        @Override
        public void unsubscribe() {
            aeronSubscription.close();
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean isBridged() {
            return false;
        }

    public boolean poll(Consumer<A> publicationHandler) {
        FragmentHandler handler = (bytes, offset, length, header) -> {
            var publication = EntityTransform.<A>deserialize(
                    new ByteIterator() {
                        int index = offset;

                        @Override
                        public boolean hasNext() {
                            return index < length;
                        }

                        @Override
                        public byte nextByte() {
                            var it = bytes.getByte(index);
                            index++;
                            return it;
                        }
                    }
            );
            publicationHandler.accept(publication);
        }
                ;
        FragmentAssembler fragmentAssembler = new FragmentAssembler(handler);

        return aeronSubscription.poll(fragmentAssembler, 1) <= 0;
    }
}
