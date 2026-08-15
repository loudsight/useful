package com.loudsight.useful.entity.permission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /**
     * The guard the test above refers to, pinned directly - it had no test at all, and three
     * separate notebook passages consequently described this as "empty means allow all", which is
     * the exact opposite of what it does. An unset property does not open the deployment; it stops
     * the container starting.
     */
    @Test
    public void fromConfigurationRefusesToBuildAnythingFromABlankValue() {
        assertThrows(IllegalStateException.class, () -> EmailAllowlist.fromConfiguration(null));
        assertThrows(IllegalStateException.class, () -> EmailAllowlist.fromConfiguration(""));
        // Whitespace matters as its own case: both call sites read the property through
        // ${application.auth.allowedEmails:}, whose empty default is what actually arrives when
        // the property is missing, and a stray-space value must not slip past as "configured".
        assertThrows(IllegalStateException.class, () -> EmailAllowlist.fromConfiguration("   "));
    }

    @Test
    public void fromConfigurationMessageNamesThePropertyAndItsRemedy() {
        // An operator sees this at container-start with no other context, so it has to say which
        // property is missing and what a valid value looks like.
        var thrown = assertThrows(IllegalStateException.class,
                () -> EmailAllowlist.fromConfiguration(""));

        assertTrue(thrown.getMessage().contains("application.auth.allowedEmails"),
                "the message must name the property: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains("*"),
                "the message must give the allow-anyone value: " + thrown.getMessage());
    }

    @Test
    public void fromConfigurationBuildsNormallyWhenAValueIsPresent() {
        // The control - without it, a factory that threw unconditionally would satisfy the above.
        assertTrue(EmailAllowlist.fromConfiguration("*").permitsEveryone());
        assertTrue(EmailAllowlist.fromConfiguration("owner@example.com").permits("owner@example.com"));
    }
}
