package com.loudsight.useful.entity.query;

import java.util.function.Function;

public interface BooleanOperation<K> {

    BooleanOperation<?> TRUE = newBooleanOperation("TRUE", (t)-> true);

	String name();

	boolean apply(K t);


    BooleanOperation<K> and(final BooleanOperation<K> second);

    BooleanOperation<K> or(BooleanOperation<K> second);

    static <K> BooleanOperation<K> not(final BooleanOperation<K> second) {

        final String description = " NOT (" + second.name() + ") ";

        return newBooleanOperation(description, t -> !second.apply(t));
    }

    static <K> BooleanOperation<K> eq(final BooleanOperation<K> second) {

        final String description = " EQUALS (" + second.name() + ") ";

        return newBooleanOperation(description, t -> second.apply(t));
    }

 	static <K> BooleanOperation<K> newBooleanOperation(String name, Function<K, Boolean> operation) {
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
            public BooleanOperation<K> or(BooleanOperation<K> second) {
                BooleanOperation<K> first = this;
                String description = "(" + first.name() + " OR " + second.name() + ")";

                return newBooleanOperation(description, t -> first.apply(t) || second.apply(t));
            }

            @Override
            public BooleanOperation<K> and(BooleanOperation<K> second) {
                BooleanOperation<K> first = this;
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
