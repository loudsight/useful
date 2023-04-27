package com.loudsight.useful.entity.query;

import java.util.function.Function;

public interface BooleanOperation<K, T> {

    BooleanOperation<?, ?> TRUE = newBooleanOperation("TRUE", (t)-> true);

	String name();

	boolean apply(K t);


    BooleanOperation<K, T> and(final BooleanOperation<K, T> second);

    BooleanOperation<K, T> or(BooleanOperation<K, T> second);

    static <K, T> BooleanOperation<K, T> not(final BooleanOperation<K, T> second) {

        final String description = " NOT (" + second.name() + ") ";

        return newBooleanOperation(description, t -> !second.apply(t));
    }

    static <K, T> BooleanOperation<K, T> eq(final BooleanOperation<K, T> second) {

        final String description = " EQUALS (" + second.name() + ") ";

        return newBooleanOperation(description, t -> second.apply(t));
    }

 	static <K, T> BooleanOperation<K, T> newBooleanOperation(String name, Function<K, Boolean> operation) {
        return new BooleanOperation<>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean apply(K t) {
                return operation.apply(t);
            }

            @Override
            public BooleanOperation<K, T> or(BooleanOperation<K, T> second) {
                BooleanOperation<K, T> first = this;
                String description = "(" + first.name() + " OR " + second.name() + ")";

                return newBooleanOperation(description, t -> first.apply(t) || second.apply(t));
            }

            @Override
            public BooleanOperation<K, T> and(BooleanOperation<K, T> second) {
                BooleanOperation<K, T> first = this;
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
