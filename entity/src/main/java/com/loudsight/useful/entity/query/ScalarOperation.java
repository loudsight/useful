package com.loudsight.useful.entity.query;

import java.util.function.Function;

public interface ScalarOperation<K, T> {

    ScalarOperation<?, ?> TRUE = newBooleanOperation("TRUE", (t)-> true);

	String name();

	boolean apply(K t);


    ScalarOperation<K, T> and(final ScalarOperation<K, T> second);

    ScalarOperation<K, T> or(ScalarOperation<K, T> second);

    static <K, T> ScalarOperation<K, T> not(final ScalarOperation<K, T> second) {

        final String description = " NOT (" + second.name() + ") ";

        return newBooleanOperation(description, t -> !second.apply(t));
    }

    static <K, T> ScalarOperation<K, T> eq(final ScalarOperation<K, T> second) {

        final String description = " EQUALS (" + second.name() + ") ";

        return newBooleanOperation(description, t -> second.apply(t));
    }

 	static <K, T> ScalarOperation<K, T> newBooleanOperation(String name, Function<K, Boolean> operation) {
        return new ScalarOperation<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean apply(K t) {
                return operation.apply(t);
            }

            @Override
            public ScalarOperation<K, T> or(ScalarOperation<K, T> second) {
                ScalarOperation<K, T> first = this;
                String description = "(" + first.name() + " OR " + second.name() + ")";

                return newBooleanOperation(description, t -> first.apply(t) || second.apply(t));
            }

            @Override
            public ScalarOperation<K, T> and(ScalarOperation<K, T> second) {
                ScalarOperation<K, T> first = this;
                String description = "(" + first.name() + " AND " + second.name() + ")";
                return newBooleanOperation(description, t -> first.apply(t) && second.apply(t));

            }

            @Override
            public String toString() {
                return name();
            }
        };
    }
}
