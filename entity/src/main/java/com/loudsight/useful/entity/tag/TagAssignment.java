package com.loudsight.useful.entity.tag;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Links a {@link Tag} to an arbitrary tagged entity, mirroring {@code PermissionGrant}'s shape
 * ({@code target: Object}). Querying by {@code target} or {@code tag} relies on
 * CypherQueryUtils.buildRelationshipPredicates (persistence-lib) deriving the traversed
 * relationship's type from the field name - see notebook/decisions/cross-cutting-tag-primitive.md for why
 * this is safe now (it wasn't, for any entity, before that fix).
 */
@Introspect(clazz = TagAssignment.class)
public class TagAssignment {

    @Id
    private String id;
    private Object target;
    private Tag tag;
    private LocalDateTime taggedAt;

    public TagAssignment() {
        this.id = UUID.randomUUID().toString();
        // Stored timestamps are UTC instants - see TimeProvider for why the zone is explicit.
        this.taggedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public TagAssignment(Object target, Tag tag) {
        this();
        this.target = target;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public LocalDateTime getTaggedAt() {
        return taggedAt;
    }

    public void setTaggedAt(LocalDateTime taggedAt) {
        this.taggedAt = taggedAt;
    }
}
