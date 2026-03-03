package dev.patric.commonlib.services;

import dev.patric.commonlib.api.capability.CapabilityKey;
import dev.patric.commonlib.api.capability.CapabilityRegistry;
import dev.patric.commonlib.api.capability.CapabilityStatus;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default in-memory capability registry.
 */
public final class DefaultCapabilityRegistry implements CapabilityRegistry {

    private final Map<CapabilityKey<?>, CapabilityStatus<?>> statuses = new ConcurrentHashMap<>();

    @Override
    public <T> void publish(CapabilityKey<T> key, CapabilityStatus<T> status) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(status, "status");
        statuses.put(key, status);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<CapabilityStatus<T>> status(CapabilityKey<T> key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable((CapabilityStatus<T>) statuses.get(key));
    }

    @Override
    public boolean isAvailable(CapabilityKey<?> key) {
        Objects.requireNonNull(key, "key");
        return statusUnchecked(key)
                .map(status -> status.available())
                .orElse(false);
    }

    private Optional<CapabilityStatus<?>> statusUnchecked(CapabilityKey<?> key) {
        return Optional.ofNullable(statuses.get(key));
    }
}
