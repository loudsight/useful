package com.loudsight.pattern.visitor;

/**
 * Specialized visitor pattern for traversing graph structures.
 * Extends the base Visitor pattern with graph-specific operations.
 *
 * @param <T> The type of node in the graph
 * @param <R> The type of result returned by visiting graph nodes
 */
public interface GraphVisitor<T, R> extends Visitor<T, R> {

    /**
     * Visit a graph structure itself (not individual nodes).
     * This allows operations that need to understand the overall graph topology.
     *
     * @param graph the graph to visit
     * @return the result of visiting the graph structure
     */
    R visitGraph(Graph<T> graph);

}
