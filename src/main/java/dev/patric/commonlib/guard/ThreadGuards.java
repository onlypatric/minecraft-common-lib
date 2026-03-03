package dev.patric.commonlib.guard;

import dev.patric.commonlib.api.CommonContext;
import java.util.Objects;

/**
 * Convenience thread guard utilities.
 */
public final class ThreadGuards {

    private ThreadGuards() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Ensures execution on primary thread via runtime scheduler.
     *
     * @param context runtime context.
     * @param operationName operation label.
     */
    public static void requirePrimaryThread(CommonContext context, String operationName) {
        Objects.requireNonNull(context, "context");
        context.scheduler().requirePrimaryThread(operationName);
    }
}
