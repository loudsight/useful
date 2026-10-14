package com.loudsight.useful.entity.metadata;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * One key/value fact about any persisted entity, attached the way {@code TagAssignment} attaches a tag:
 * {@code target} is a real relationship to the described entity, so nothing about the target's own
 * schema changes when a new kind of metadata appears.
 *
 * <p>Keys are namespaced, lower-case and dotted ({@code source.url}, {@code preview.image}), owned by
 * whichever app defines them. A target may carry several entries, and several entries may share a key;
 * what that means is the key owner's rule. Per-subject isolation needs no owner field: the persistence
 * layer adds an {@code OWNED_BY} edge to the caller on save and filters every read by it, exactly as it
 * does for {@code TagAssignment} and {@code PermissionGrant}
 * (kb/decisions/cross-cutting-tag-primitive.md).
 */
@Introspect(clazz = MetadataEntry.class)
public class MetadataEntry {

    @Id
    private String id;
    // Null only on the no-arg instance the persistence layer fills in when it reads an entry back.
    private @Nullable Object target;
    private @Nullable String key;
    private @Nullable String value;
    private LocalDateTime updatedAt;

    public MetadataEntry() {
        this.id = UUID.randomUUID().toString();
        // Stored timestamps are UTC instants - see TimeProvider for why the zone is explicit.
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public MetadataEntry(Object target, String key, String value) {
        this();
        this.target = target;
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @Nullable Object getTarget() {
        return target;
    }

    public void setTarget(@Nullable Object target) {
        this.target = target;
    }

    public @Nullable String getKey() {
        return key;
    }

    public void setKey(@Nullable String key) {
        this.key = key;
    }

    public @Nullable String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
