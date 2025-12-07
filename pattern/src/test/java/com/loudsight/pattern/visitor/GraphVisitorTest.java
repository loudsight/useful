package com.loudsight.pattern.visitor;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GraphVisitor pattern interface.
 */
public class GraphVisitorTest {

    /**
     * Simple test graph implementation for testing.
     */
    static class TestGraph implements Graph<String> {
        private Set<String> nodes = new HashSet<>();

        public TestGraph(String... nodeNames) {
            for (String name : nodeNames) {
                nodes.add(name);
            }
        }

        public Set<String> getNodes() {
            return nodes;
        }
    }

    /**
     * Simple test visitor implementation for graphs.
     */
    static class CollectingGraphVisitor implements GraphVisitor<String, Set<String>> {
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
        public Set<String> visitGraph(Graph<String> graph) {
            TestGraph testGraph = (TestGraph) graph;
            return new HashSet<>(testGraph.getNodes());
        }
    }

    @Test
    public void testGraphVisitorInterface() {
        GraphVisitor<String, Set<String>> visitor = new CollectingGraphVisitor();

        // Test visiting individual nodes
        Set<String> result = visitor.visit("node1");
        assertTrue(result.contains("node1"));
        assertEquals(1, result.size());

        // Test accumulation
        Set<String> accumulated = visitor.accumulate("node2", result);
        assertTrue(accumulated.contains("node1"));
        assertTrue(accumulated.contains("node2"));
        assertEquals(2, accumulated.size());
    }

    @Test
    public void testGraphVisitorWithGraph() {
        Graph<String> testGraph = new TestGraph("a", "b", "c");
        GraphVisitor<String, Set<String>> visitor = new CollectingGraphVisitor();

        Set<String> graphNodes = visitor.visitGraph(testGraph);
        assertTrue(graphNodes.contains("a"));
        assertTrue(graphNodes.contains("b"));
        assertTrue(graphNodes.contains("c"));
        assertEquals(3, graphNodes.size());
    }

    @Test
    public void testGraphVisitorIsAlsoVisitor() {
        GraphVisitor<String, Set<String>> graphVisitor = new CollectingGraphVisitor();

        // Should be usable as a Visitor
        Visitor<String, Set<String>> visitor = graphVisitor;
        assertNotNull(visitor);

        Set<String> result = visitor.visit("test");
        assertTrue(result.contains("test"));
    }

}
