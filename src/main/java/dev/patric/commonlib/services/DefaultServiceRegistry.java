package dev.patric.commonlib.services;

import dev.patric.commonlib.api.ServiceRegistry;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry with fail-fast duplicate protection.
 */
public final class DefaultServiceRegistry implements ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Creates an empty service registry.
     */
    public DefaultServiceRegistry() {
        // default constructor for explicit API documentation.
    }

    @Override
    public <T> void register(Class<T> type, T instance) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(instance, "instance");

        Object previous = services.putIfAbsent(type, instance);
        if (previous != null) {
            throw new IllegalStateException("Service already registered for type: " + type.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return Optional.ofNullable((T) services.get(type));
    }

    @Override
    public <T> T require(Class<T> type) {
        return find(type)
                .orElseThrow(() -> new IllegalStateException("Missing required service: " + type.getName()));
    }
}
