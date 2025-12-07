package com.loudsight.useful.collection.graph;

import com.loudsight.pattern.visitor.Graph;
import com.loudsight.pattern.visitor.GraphVisitor;

import java.util.List;
import java.util.Set;

/**
 * Generic directed graph interface supporting visitor pattern operations.
 * Represents a directed acyclic graph (DAG) of nodes with dependency relationships.
 *
 * @param <T> The type of node in the graph
 */
public interface DirectedGraph<T> extends Graph<T> {

    /**
     * Add a node to the graph.
     *
     * @param node the node to add
     */
    void addNode(T node);

    /**
     * Remove a node from the graph.
     *
     * @param node the node to remove
     */
    void removeNode(T node);

    /**
     * Add a directed edge from one node to another.
     *
     * @param from the source node
     * @param to the target node
     * @throws IllegalArgumentException if either node doesn't exist
     */
    void addEdge(T from, T to);

    /**
     * Remove a directed edge between nodes.
     *
     * @param from the source node
     * @param to the target node
     */
    void removeEdge(T from, T to);

    /**
     * Get all nodes in the graph.
     *
     * @return set of all nodes
     */
    Set<T> getNodes();

    /**
     * Get all nodes that this node directly depends on (outgoing edges).
     *
     * @param node the node to query
     * @return set of nodes this node depends on
     * @throws IllegalArgumentException if node doesn't exist
     */
    Set<T> getDependencies(T node);

    /**
     * Get all nodes that depend on this node (incoming edges).
     * Reverse operation of getDependencies for impact analysis.
     *
     * @param node the node to query
     * @return set of nodes that depend on this node
     * @throws IllegalArgumentException if node doesn't exist
     */
    Set<T> getDependents(T node);

    /**
     * Detect if the graph contains a cycle (circular dependency).
     *
     * @return true if a cycle exists, false otherwise
     */
    boolean hasCycle();

    /**
     * Topologically sort the nodes in the graph.
     * Nodes with no dependencies come first, then nodes that depend on them, etc.
     *
     * @return list of nodes in topological order
     * @throws IllegalStateException if the graph contains a cycle
     */
    List<T> topologicalSort();

    /**
     * Accept a visitor for traversing the graph.
     * Visitor pattern for extensible graph operations without modifying the graph structure.
     *
     * @param visitor the visitor to accept
     * @param <R> the result type returned by the visitor
     * @return the result of the visitor's operation
     */
    <R> R accept(GraphVisitor<T, R> visitor);

}
