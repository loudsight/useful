package com.loudsight.pattern.visitor;

/**
 * Marker interface for graph structures used with GraphVisitor pattern.
 * Concrete graph implementations should implement this interface to support visitor operations.
 *
 * @param <T> The type of node in the graph
 */
public interface Graph<T> {
    // Marker interface for graph structures
    // Actual graph operations are defined in concrete implementations
    // See com.loudsight.collections for concrete implementations
}
