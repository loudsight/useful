/**
 * Generic directed-graph types and the adjacency-list implementation behind them.
 *
 * <p>This package is {@code @NullMarked}: every type usage in it that is not explicitly
 * {@code @Nullable} is non-null, and NullAway enforces that. It is the first package in the tree to
 * opt in, chosen because it is small, hand-written, free of generated sources, and generic enough to
 * exercise NullAway's JSpecify mode rather than only its simplest checks.
 */
@NullMarked
package com.loudsight.useful.collection.graph;

import org.jspecify.annotations.NullMarked;
