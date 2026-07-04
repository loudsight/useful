package com.loudsight.useful.entity.permission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubjectTest {

    @Test
    public void singletonsAreBuiltinSubjects() {
        assertTrue(Subject.getAdmin().isBuiltinSubject());
        assertTrue(Subject.getAnonymous().isBuiltinSubject());
    }

    @Test
    public void reconstructedBuiltinIdsAreRecognizedAsBuiltinSubjects() {
        // Caller identity crosses the dispatcher as a plain string (e.g. Neo4JPersistenceApi.save
        // rebuilds `new Subject(caller, caller)` from DispatchContext.getCaller()), so a
        // freshly-constructed Subject sharing a builtin id must still count as builtin.
        assertTrue(new Subject("admin", "admin").isBuiltinSubject());
        assertTrue(new Subject("anonymous", "anonymous").isBuiltinSubject());
    }

    @Test
    public void ordinaryUserIsNotBuiltinSubject() {
        assertFalse(new Subject("user@example.com", "user@example.com").isBuiltinSubject());
    }
}
