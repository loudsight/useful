package com.loudsight.useful.entity.permission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailAllowlistTest {

    @Test
    public void admitsAListedAddressAndRefusesAnUnlistedOne() {
        var allowlist = new EmailAllowlist("owner@example.com");

        assertTrue(allowlist.permits("owner@example.com"));
        assertFalse(allowlist.permits("stranger@example.com"));
    }

    @Test
    public void wildcardAdmitsEveryone() {
        var allowlist = new EmailAllowlist("*");

        assertTrue(allowlist.permits("anyone@example.com"));
        assertTrue(allowlist.permitsEveryone());
    }

    @Test
    public void comparisonIgnoresCaseAndSurroundingWhitespace() {
        // The address arrives from an OAuth provider or a form field, so its casing and padding
        // are not ours to control; the configured list is hand-written and equally uncontrolled.
        var allowlist = new EmailAllowlist("  Owner@Example.COM  ");

        assertTrue(allowlist.permits("owner@example.com"));
        assertTrue(allowlist.permits(" OWNER@EXAMPLE.com "));
    }

    @Test
    public void admitsAnyAddressInAMultiEntryList() {
        var allowlist = new EmailAllowlist("first@example.com,second@example.com");

        assertTrue(allowlist.permits("first@example.com"));
        assertTrue(allowlist.permits("second@example.com"));
        assertFalse(allowlist.permits("third@example.com"));
    }

    @Test
    public void blankEntriesAreIgnoredRatherThanAdmittingBlankAddresses() {
        // A trailing comma is an easy thing to leave in a compose file; it must not turn into an
        // entry that an empty email would then match.
        var allowlist = new EmailAllowlist("owner@example.com,,");

        assertTrue(allowlist.permits("owner@example.com"));
        assertFalse(allowlist.permits(""));
        assertFalse(allowlist.permits("  "));
    }

    @Test
    public void refusesAnAbsentAddressUnlessEveryoneIsAdmitted() {
        assertFalse(new EmailAllowlist("owner@example.com").permits(null));
        // "*" has nothing to compare against, so an unidentified caller is admitted along with
        // everyone else - the deployment has declared it does not care who registers.
        assertTrue(new EmailAllowlist("*").permits(null));
    }

    @Test
    public void wildcardAmongOtherEntriesStillAdmitsEveryone() {
        var allowlist = new EmailAllowlist("owner@example.com,*");

        assertTrue(allowlist.permits("stranger@example.com"));
        assertTrue(allowlist.permitsEveryone());
    }

    @Test
    public void anEmptyConfigurationAdmitsNobody() {
        // The bean factory rejects a blank value before constructing this, so this state should be
        // unreachable in a running deployment. Pinned anyway: if that guard is ever removed, the
        // failure must be "nobody gets in", never "everybody does".
        var allowlist = new EmailAllowlist("");

        assertFalse(allowlist.permits("owner@example.com"));
        assertFalse(allowlist.permitsEveryone());
    }
}
