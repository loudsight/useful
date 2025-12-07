package com.loudsight.pattern.visitor;

/**
 * Base visitor pattern interface for traversing and processing element hierarchies.
 * This is a generic, reusable visitor pattern that can be applied to any data structure.
 *
 * @param <T> The type of element being visited
 * @param <R> The type of result returned by the visitor
 */
public interface Visitor<T, R> {

    /**
     * Visit a single element and return a result.
     *
     * @param element the element to visit
     * @return the result of visiting this element
     */
    R visit(T element);

    /**
     * Accumulate a result from visiting an element with a previously computed result.
     * This allows chaining visitor operations across multiple elements.
     *
     * @param element the element to visit
     * @param currentResult the accumulated result from previous visits
     * @return the new accumulated result
     */
    R accumulate(T element, R currentResult);

}
