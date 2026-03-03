package dev.patric.commonlib.api.capability;

import java.util.Optional;

/**
 * Typed capability registry for optional adapters.
 */
public interface CapabilityRegistry {

    /**
     * Publishes status for a capability key.
     *
     * @param key capability key.
     * @param status availability status.
     * @param <T> metadata type.
     */
    <T> void publish(CapabilityKey<T> key, CapabilityStatus<T> status);

    /**
     * Returns status for a capability key.
     *
     * @param key capability key.
     * @param <T> metadata type.
     * @return optional status.
     */
    <T> Optional<CapabilityStatus<T>> status(CapabilityKey<T> key);

    /**
     * Returns true when capability is published as available.
     *
     * @param key capability key.
     * @return true when available.
     */
    boolean isAvailable(CapabilityKey<?> key);
}
