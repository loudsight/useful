package com.loudsight.useful.entity.query;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanOperationTest {

    @Test
    public void testEq() {
        var equalsFive = BooleanOperation.newBooleanOperation(
                "equalsFive",
                t -> Objects.equals(t, 5)
        );
        assertTrue(equalsFive.apply(5));
        assertFalse(equalsFive.apply(6));
    }
}
