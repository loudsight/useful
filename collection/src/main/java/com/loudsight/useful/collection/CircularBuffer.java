package com.loudsight.useful.collection;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

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
        IntStream.range(0, length).forEach( i -> this.nodes[i] = new Node());
    }

    private int indexOf(int count) {
        return count % nodes.length;
    }

    public T poll() {
        int currentReadPosition = readPosition.get();
        Node node = nodes[indexOf(currentReadPosition)];
        Optional<T> value;

        if (writePosition.get() - currentReadPosition == 0 ||
                (value = node.getValue()).isEmpty()) {
            return null;
        }

        return value.get();
    }

    public T take() {
        int currentReadPosition = readPosition.get();
        Node node = nodes[indexOf(currentReadPosition)];
        Optional<T> value;

        while (writePosition.get() - currentReadPosition == 0 ||
                (value = node.getValue()).isEmpty()) {
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
