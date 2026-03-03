package dev.patric.commonlib.guard;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of policy evaluation.
 */
public final class PolicyDecision {

    private static final PolicyDecision ALLOW = new PolicyDecision(true, null);

    private final boolean allowed;
    private final String reason;

    private PolicyDecision(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }

    /**
     * Creates allow decision.
     *
     * @return allow decision.
     */
    public static PolicyDecision allow() {
        return ALLOW;
    }

    /**
     * Creates deny decision.
     *
     * @param reason deny reason.
     * @return deny decision.
     */
    public static PolicyDecision deny(String reason) {
        return new PolicyDecision(false, Objects.requireNonNull(reason, "reason"));
    }

    /**
     * Indicates whether decision allows execution.
     *
     * @return true if allowed.
     */
    public boolean allowed() {
        return allowed;
    }

    /**
     * Indicates whether decision denies execution.
     *
     * @return true if denied.
     */
    public boolean denied() {
        return !allowed;
    }

    /**
     * Optional deny reason.
     *
     * @return optional reason.
     */
    public Optional<String> reason() {
        return Optional.ofNullable(reason);
    }
}
