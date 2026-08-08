package com.loudsight.useful.entity.permission;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Which email addresses may obtain an account on this deployment, checked before any account
 * exists.
 *
 * This is an ADMISSION control, not an authorization one. Everything else in this package answers
 * "what may this Subject do"; this answers the earlier question "may a Subject come into being for
 * this email at all". It exists so a deployment can be private - dev is locked to the operator's
 * own address while prod admits anyone - without either environment growing a different security
 * model.
 *
 * <h2>Two call sites, and both are required</h2>
 *
 * Social sign-in auto-provisions an account for whatever address the provider returns, and
 * {@code POST /api/auth/register} self-confirms ({@code ConfirmMethod.NONE} is read as
 * {@code confirmed = true}), so it also yields a usable account with no verification step. Gating
 * only one of them leaves the other as a way in. The sign-in gate must also reject DURING
 * authentication rather than after it: the session repository is HttpSessionSecurityContextRepository,
 * so a rejection raised once authentication has succeeded would leave the caller authenticated in
 * session and resolvable by CallerSupport as a real Subject.
 *
 * <h2>There is deliberately no default</h2>
 *
 * The configuration property that supplies this has no fallback value, so a deployment that fails
 * to declare who may register does not start. An empty list would otherwise mean "nobody", which
 * reads as a hung login page rather than a misconfiguration, and a permissive default would mean a
 * dropped setting silently opens the deployment to the world. Use {@code "*"} to state
 * "anyone may register" explicitly - that is a declaration, not an omission.
 */
public final class EmailAllowlist {

    private static final String WILDCARD = "*";

    private final Set<String> allowed;
    private final boolean allowAll;

    /**
     * Builds the allowlist from a configuration value, refusing to build one at all when nothing
     * was configured.
     *
     * Both gates - social sign-in and registration - run in different containers and so read the
     * property separately. They share this factory so the refusal, and the wording that tells an
     * operator how to fix it, exists once.
     *
     * @throws IllegalStateException when nothing was configured. Thrown during bean creation, so
     *                               the context fails to start rather than starting in a state
     *                               nobody chose.
     */
    public static EmailAllowlist fromConfiguration(String configured) {
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException(
                    "application.auth.allowedEmails must be set - a comma-separated list of email "
                            + "addresses permitted to sign in or register, or '*' to allow anyone. "
                            + "There is deliberately no default: an unset value would either lock "
                            + "everyone out silently or open the deployment to the world.");
        }
        return new EmailAllowlist(configured);
    }

    /**
     * @param commaSeparated one or more email addresses, or {@code "*"} for anyone. Entries are
     *                       trimmed and lower-cased; blank entries are ignored so a trailing comma
     *                       is harmless.
     */
    public EmailAllowlist(String commaSeparated) {
        this.allowed = Arrays.stream((commaSeparated == null ? "" : commaSeparated).split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .map(entry -> entry.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.allowAll = allowed.contains(WILDCARD);
    }

    /**
     * Whether this address may sign in or register. A null or blank address is refused even when
     * the list is otherwise populated - an unidentified caller is not on any list - but {@code "*"}
     * admits it, because a deployment that admits anyone has nothing to check the address against.
     */
    public boolean permits(String email) {
        if (allowAll) {
            return true;
        }
        if (email == null || email.isBlank()) {
            return false;
        }
        return allowed.contains(email.trim().toLowerCase(Locale.ROOT));
    }

    /** Whether this deployment admits any address at all, i.e. the list is {@code "*"}. */
    public boolean permitsEveryone() {
        return allowAll;
    }
}
