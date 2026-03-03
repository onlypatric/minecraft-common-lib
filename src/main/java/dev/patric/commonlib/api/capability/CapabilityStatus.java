package dev.patric.commonlib.api.capability;

import java.util.Objects;

/**
 * Availability status for a typed capability.
 *
 * @param available whether capability is available.
 * @param reason unavailable reason when not available.
 * @param metadata optional metadata payload.
 * @param <T> metadata type.
 */
public record CapabilityStatus<T>(boolean available, String reason, T metadata) {

    /**
     * Creates an available status.
     *
     * @param metadata optional metadata.
     * @param <T> metadata type.
     * @return status instance.
     */
    public static <T> CapabilityStatus<T> available(T metadata) {
        return new CapabilityStatus<>(true, null, metadata);
    }

    /**
     * Creates an unavailable status.
     *
     * @param reason reason message.
     * @param <T> metadata type.
     * @return status instance.
     */
    public static <T> CapabilityStatus<T> unavailable(String reason) {
        return new CapabilityStatus<>(false, Objects.requireNonNull(reason, "reason"), null);
    }

    /**
     * Compact constructor validation.
     */
    public CapabilityStatus {
        if (available && reason != null) {
            throw new IllegalArgumentException("Available capability must not define reason");
        }
        if (!available && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Unavailable capability must define reason");
        }
    }
}
