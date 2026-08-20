package com.loudsight.useful.entity.tag;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Tag.class)
public class Tag {

    @Id
    private String name;

    public Tag() {
        // Required for deserialization.
    }

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
