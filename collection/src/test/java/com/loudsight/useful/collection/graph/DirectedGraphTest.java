package com.loudsight.useful.collection.graph;

import com.loudsight.pattern.visitor.GraphVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DirectedGraph implementation.
 */
public class DirectedGraphTest {

    private DirectedGraph<String> graph;

    @BeforeEach
    public void setUp() {
        graph = new AdjacencyListGraph<>();
    }

    @Test
    public void testAddAndGetNodes() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        Set<String> nodes = graph.getNodes();
        assertEquals(3, nodes.size());
        assertTrue(nodes.contains("A"));
        assertTrue(nodes.contains("B"));
        assertTrue(nodes.contains("C"));
    }

    @Test
    public void testAddDuplicateNode() {
        graph.addNode("A");
        graph.addNode("A");

        Set<String> nodes = graph.getNodes();
        assertEquals(1, nodes.size());
    }

    @Test
    public void testRemoveNode() {
        graph.addNode("A");
        graph.addNode("B");
        graph.removeNode("A");

        Set<String> nodes = graph.getNodes();
        assertEquals(1, nodes.size());
        assertFalse(nodes.contains("A"));
    }

    @Test
    public void testRemoveNonExistentNode() {
        assertThrows(IllegalArgumentException.class, () -> {
            graph.removeNode("A");
        });
    }

    @Test
    public void testAddEdge() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");

        Set<String> dependencies = graph.getDependencies("A");
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.contains("B"));
    }

    @Test
    public void testAddEdgeWithMissingNode() {
        graph.addNode("A");
        assertThrows(IllegalArgumentException.class, () -> {
            graph.addEdge("A", "B"); // B doesn't exist
        });
    }

    @Test
    public void testGetDependencies() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        Set<String> dependencies = graph.getDependencies("A");
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.contains("B"));
        assertTrue(dependencies.contains("C"));
    }

    @Test
    public void testGetDependents() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");

        Set<String> dependents = graph.getDependents("A");
        assertEquals(0, dependents.size());

        Set<String> dependentsOfB = graph.getDependents("B");
        assertEquals(1, dependentsOfB.size());
        assertTrue(dependentsOfB.contains("A"));
    }

    @Test
    public void testCycleDetection() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");
        graph.addEdge("C", "A"); // Creates cycle

        assertTrue(graph.hasCycle());
    }

    @Test
    public void testNoCycle() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("A", "B");
        graph.addEdge("B", "C");

        assertFalse(graph.hasCycle());
    }

    @Test
    public void testTopologicalSort() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");

        List<String> sorted = graph.topologicalSort();
        assertEquals(4, sorted.size());

        // A should come before B and C
        assertTrue(sorted.indexOf("A") < sorted.indexOf("B"));
        assertTrue(sorted.indexOf("A") < sorted.indexOf("C"));
        // B should come before D
        assertTrue(sorted.indexOf("B") < sorted.indexOf("D"));
    }

    @Test
    public void testTopologicalSortWithCycle() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        graph.addEdge("B", "A"); // Cycle

        assertThrows(IllegalStateException.class, () -> {
            graph.topologicalSort();
        });
    }

    @Test
    public void testRemoveEdge() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addEdge("A", "B");
        graph.removeEdge("A", "B");

        Set<String> dependencies = graph.getDependencies("A");
        assertEquals(0, dependencies.size());
    }

    @Test
    public void testVisitorPattern() {
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");

        GraphVisitor<String, Set<String>> collectingVisitor = new GraphVisitor<String, Set<String>>() {
            @Override
            public Set<String> visit(String element) {
                Set<String> result = new HashSet<>();
                result.add(element);
                return result;
            }

            @Override
            public Set<String> accumulate(String element, Set<String> currentResult) {
                currentResult.add(element);
                return currentResult;
            }

            @Override
            public Set<String> visitGraph(com.loudsight.pattern.visitor.Graph<String> g) {
                return ((DirectedGraph<String>) g).getNodes();
            }
        };

        Set<String> visitedNodes = graph.accept(collectingVisitor);
        assertEquals(3, visitedNodes.size());
        assertTrue(visitedNodes.contains("A"));
        assertTrue(visitedNodes.contains("B"));
        assertTrue(visitedNodes.contains("C"));
    }

}
