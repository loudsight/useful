package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Introspect;

/**
 * Who may READ an entity, independent of who owns it.
 *
 * An entity declares this by carrying a field of this type. CypherQueryUtils writes that field to
 * the reserved node property {@code __visibility__} (never under the field's own name), and
 * CypherPermissionFilter is the only reader. Detection is by declared TYPE, not field name, so an
 * unrelated {@code String audience = "PUBLIC"} field cannot accidentally publish an entity.
 *
 * <h2>Absent means private</h2>
 *
 * An entity with no field of this type, or a null value, gets no {@code __visibility__} property and
 * stays owner-only. Private is the default and requires no declaration.
 *
 * <h2>Why PRIVATE exists when absent already means private</h2>
 *
 * Because null cannot un-publish. buildFieldSpec skips null-valued fields entirely, so they never
 * reach editProps, so a MERGE leaves whatever property the node already had. Setting a previously
 * PUBLIC entity's field back to null therefore leaves it PUBLIC on disk. PRIVATE is the only way to
 * retract publication.
 *
 * <h2>READ only</h2>
 *
 * PUBLIC widens reads. It does NOT widen writes or deletes - CypherPermissionFilter takes an
 * Operation and only applies this on READ, and Neo4JPersistenceApi.checkWritePermission does not
 * consult it at all. A public entity is world-readable and still owner-writable.
 *
 * Sharing with SPECIFIC users is a different mechanism (PermissionGrant) and is not expressible
 * here - see notebook/plans/claude/permission-aware-ui-visibility.md.
 */
@Introspect(clazz = Visibility.class)
public enum Visibility {
    /** Readable by anyone, including anonymous callers. */
    PUBLIC,
    /** Readable by the owner (and admin) only. Same effect as absent, but retractable. */
    PRIVATE
}
