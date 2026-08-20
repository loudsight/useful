package com.loudsight.useful.collection;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircularBuffer<T> {

    static class Node {
       private final AtomicReference<Optional<Object>> value = new AtomicReference<>();

        Node() {
            value.set(Optional.empty());
        }
        <T> Optional<T> getValue() {
            return (Optional<T>)value.get();
        }

        void clearValue() {
            value.set(Optional.empty());
        }

        void setValue(Object element) {
            value.set(Optional.of(element));
        }
    }


    private final Node[] nodes;
    private final AtomicInteger readPosition = new AtomicInteger();
    private final AtomicInteger writePosition = new AtomicInteger();

    public CircularBuffer(int length) {
        this.nodes = new Node[length];
        Arrays.setAll(this.nodes, i -> new Node());
    }

    private int indexOf(int count) {
        return count % nodes.length;
    }

    public T poll() {
        int currentReadPosition = readPosition.get();
        Node node = nodes[indexOf(currentReadPosition)];

        if (writePosition.get() - currentReadPosition == 0) {
            return null;
        }

        Optional<T> value = node.getValue();
        if (value.isEmpty()) {
            return null;
        }

        return value.get();
    }

    public T take() {
        int currentReadPosition = readPosition.get();
        Node node = nodes[indexOf(currentReadPosition)];
        Optional<T> value;

        while (true) {
            if (writePosition.get() - currentReadPosition != 0) {
                value = node.getValue();
                if (!value.isEmpty()) {
                    break;
                }
            }
            Thread.yield();
        }

        node.clearValue();
        readPosition.incrementAndGet();

        return value.get();
    }

    public void add(T element) {
        int currentWritePosition = writePosition.getAndIncrement();
        Node nextNode = nodes[indexOf(currentWritePosition)];

        while (currentWritePosition - readPosition.get() > nodes.length - 1) {
            Thread.yield();
        }

        nextNode.setValue(element);
    }

}
