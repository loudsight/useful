package com.loudsight.pattern.visitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Visitor pattern interface.
 */
public class VisitorTest {

    /**
     * Simple test visitor implementation for testing.
     */
    static class CountingVisitor implements Visitor<String, Integer> {
        @Override
        public Integer visit(String element) {
            return 1;
        }

        @Override
        public Integer accumulate(String element, Integer currentResult) {
            return currentResult + 1;
        }
    }

    @Test
    public void testVisitorInterface() {
        Visitor<String, Integer> visitor = new CountingVisitor();

        // Test single visit
        Integer result = visitor.visit("test");
        assertEquals(Integer.valueOf(1), result);

        // Test accumulation
        Integer accumulated = visitor.accumulate("another", 5);
        assertEquals(Integer.valueOf(6), accumulated);
    }

    /**
     * Test that visitor can be used for different types.
     */
    static class SumVisitor implements Visitor<Integer, Long> {
        @Override
        public Long visit(Integer element) {
            return element.longValue();
        }

        @Override
        public Long accumulate(Integer element, Long currentResult) {
            return currentResult + element;
        }
    }

    @Test
    public void testVisitorWithDifferentTypes() {
        Visitor<Integer, Long> visitor = new SumVisitor();

        Long result = visitor.visit(42);
        assertEquals(Long.valueOf(42), result);

        Long accumulated = visitor.accumulate(8, 100L);
        assertEquals(Long.valueOf(108), accumulated);
    }

}
