package dev.patric.commonlib.api.capability;

import java.util.Objects;

/**
 * Typed capability key.
 *
 * @param name capability name.
 * @param metadataType metadata type associated with this capability.
 * @param <T> metadata type.
 */
public record CapabilityKey<T>(String name, Class<T> metadataType) {

    /**
     * Creates a capability key.
     *
     * @param name capability name.
     * @param metadataType metadata type.
     * @param <T> metadata type.
     * @return capability key.
     */
    public static <T> CapabilityKey<T> of(String name, Class<T> metadataType) {
        return new CapabilityKey<>(name, metadataType);
    }

    /**
     * Compact constructor validation.
     */
    public CapabilityKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(metadataType, "metadataType");
    }
}
