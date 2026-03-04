package dev.patric.commonlib.api.arena;

import dev.patric.commonlib.api.ServiceRegistry;
import java.util.Objects;

/**
 * Context passed to arena reset strategies.
 *
 * @param services runtime services.
 * @param cause reset cause.
 * @param requestEpochMilli request timestamp.
 */
public record ArenaResetContext(ServiceRegistry services, String cause, long requestEpochMilli) {

    /**
     * Creates a reset context.
     */
    public ArenaResetContext {
        services = Objects.requireNonNull(services, "services");
        cause = Objects.requireNonNull(cause, "cause").trim();
        if (cause.isEmpty()) {
            throw new IllegalArgumentException("cause must not be blank");
        }
    }
}
