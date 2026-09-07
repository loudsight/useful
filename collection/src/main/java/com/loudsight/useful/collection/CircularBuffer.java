package com.loudsight.useful.collection;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class CircularBuffer<T> {

     static class Node<T> {
         private final AtomicReference<Optional<T>> value = new AtomicReference<>();

        Node() {
            value.set(Optional.empty());
        }
        Optional<T> getValue() {
            return value.get();
        }

        void clearValue() {
            value.set(Optional.empty());
        }

        void setValue(T element) {
            value.set(Optional.of(element));
        }
    }


    private final List<Node<T>> nodes;
    private final AtomicInteger readPosition = new AtomicInteger();
    private final AtomicInteger writePosition = new AtomicInteger();

    public CircularBuffer(int length) {
        this.nodes = IntStream.range(0, length).mapToObj(ignored -> new Node<T>()).toList();
    }

    private int indexOf(int count) {
        return count % nodes.size();
    }

    public T poll() {
        int currentReadPosition = readPosition.get();
        Node<T> node = nodes.get(indexOf(currentReadPosition));

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
        Node<T> node = nodes.get(indexOf(currentReadPosition));
        Optional<T> value;

        while (true) {
            if (writePosition.get() - currentReadPosition != 0) {
                value = node.getValue();
                if (!value.isEmpty()) {
                    break;
                }
            }
            Thread.onSpinWait();
        }

        node.clearValue();
        readPosition.incrementAndGet();

        return value.get();
    }

    public void add(T element) {
        int currentWritePosition = writePosition.getAndIncrement();
        Node<T> nextNode = nodes.get(indexOf(currentWritePosition));

        while (currentWritePosition - readPosition.get() > nodes.size() - 1) {
            Thread.onSpinWait();
        }

        nextNode.setValue(element);
    }

}
