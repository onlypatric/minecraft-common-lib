package dev.patric.commonlib.api;

import java.util.Optional;

/**
 * Type-safe registry for runtime services.
 */
public interface ServiceRegistry {

    /**
     * Registers a service instance once.
     *
     * @param type service type key.
     * @param instance service implementation.
     * @param <T> service type.
     */
    <T> void register(Class<T> type, T instance);

    /**
     * Finds a service instance.
     *
     * @param type service type key.
     * @param <T> service type.
     * @return optional service.
     */
    <T> Optional<T> find(Class<T> type);

    /**
     * Resolves a service instance or fails fast.
     *
     * @param type service type key.
     * @param <T> service type.
     * @return service instance.
     */
    <T> T require(Class<T> type);
}
