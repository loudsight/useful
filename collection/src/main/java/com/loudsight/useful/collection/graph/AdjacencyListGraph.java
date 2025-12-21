package com.loudsight.useful.collection.graph;

import com.loudsight.pattern.visitor.GraphVisitor;

import java.util.*;

/**
 * Concrete implementation of DirectedGraph using adjacency list representation.
 * Supports cycle detection and topological sorting.
 *
 * @param <T> The type of node in the graph
 */
public class AdjacencyListGraph<T> implements DirectedGraph<T> {

    private final Map<T, Set<T>> adjacencyList = new LinkedHashMap<>();
    private final Set<T> nodes = new LinkedHashSet<>();

    @Override
    public void addNode(T node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            adjacencyList.putIfAbsent(node, new HashSet<>());
        }
    }

    @Override
    public void removeNode(T node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException("Node " + node + " does not exist in graph");
        }
        nodes.remove(node);
        adjacencyList.remove(node);
        for (Set<T> edges : adjacencyList.values()) {
            edges.remove(node);
        }
    }

    @Override
    public void addEdge(T from, T to) {
        if (!nodes.contains(from) || !nodes.contains(to)) {
            throw new IllegalArgumentException("Both nodes must exist in the graph");
        }
        adjacencyList.get(from).add(to);
    }

    @Override
    public void removeEdge(T from, T to) {
        if (adjacencyList.containsKey(from)) {
            adjacencyList.get(from).remove(to);
        }
    }

    @Override
    public Set<T> getNodes() {
        // Return the actual LinkedHashSet to preserve insertion order
        // Note: this returns a reference to the internal set, so modifications to the
        // returned set will modify the graph. For true immutability, callers should treat
        // the returned set as read-only. The LinkedHashSet preserves insertion order which is
        // critical for deterministic graph traversal and topological sorting.
        return nodes;
    }

    @Override
    public Set<T> getDependencies(T node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException("Node " + node + " does not exist in graph");
        }
        return new HashSet<>(adjacencyList.getOrDefault(node, new HashSet<>()));
    }

    @Override
    public Set<T> getDependents(T node) {
        if (!nodes.contains(node)) {
            throw new IllegalArgumentException("Node " + node + " does not exist in graph");
        }
        Set<T> dependents = new HashSet<>();
        for (Map.Entry<T, Set<T>> entry : adjacencyList.entrySet()) {
            if (entry.getValue().contains(node)) {
                dependents.add(entry.getKey());
            }
        }
        return dependents;
    }

    @Override
    public boolean hasCycle() {
        Set<T> visited = new HashSet<>();
        Set<T> recursionStack = new HashSet<>();

        for (T node : nodes) {
            if (!visited.contains(node)) {
                if (hasCycleDFS(node, visited, recursionStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCycleDFS(T node, Set<T> visited, Set<T> recursionStack) {
        visited.add(node);
        recursionStack.add(node);

        for (T neighbor : adjacencyList.getOrDefault(node, new HashSet<>())) {
            if (!visited.contains(neighbor)) {
                if (hasCycleDFS(neighbor, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    @Override
    public List<T> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Graph contains a cycle and cannot be topologically sorted");
        }

        Map<T, Integer> inDegree = new HashMap<>();
        for (T node : nodes) {
            inDegree.put(node, 0);
        }

        for (Set<T> edges : adjacencyList.values()) {
            for (T neighbor : edges) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
            }
        }

        // Use a sorted collection (based on natural ordering or comparable) for deterministic results
        // This ensures consistent ordering when multiple nodes have the same in-degree
        Queue<T> queue = new PriorityQueue<>((a, b) -> {
            // Compare by in-degree first, then by natural ordering if available
            int inDegreeA = inDegree.get(a);
            int inDegreeB = inDegree.get(b);
            if (inDegreeA != inDegreeB) {
                return Integer.compare(inDegreeA, inDegreeB);
            }
            // If both have same in-degree, use string representation for deterministic ordering
            return a.toString().compareTo(b.toString());
        });

        for (T node : nodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        List<T> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            T node = queue.poll();
            result.add(node);

            for (T neighbor : adjacencyList.getOrDefault(node, new HashSet<>())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }

    @Override
    public <R> R accept(GraphVisitor<T, R> visitor) {
        return visitor.visitGraph(this);
    }

}
