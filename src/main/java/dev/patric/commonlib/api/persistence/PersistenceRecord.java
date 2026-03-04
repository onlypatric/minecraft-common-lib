package dev.patric.commonlib.api.persistence;

import java.util.Map;
import java.util.Objects;

/**
 * Portable persistence record.
 *
 * @param namespace record namespace.
 * @param key record key.
 * @param fields record fields.
 * @param updatedAtEpochMilli last update timestamp.
 */
public record PersistenceRecord(String namespace, String key, Map<String, String> fields, long updatedAtEpochMilli) {

    /**
     * Creates an immutable persistence record.
     */
    public PersistenceRecord {
        namespace = requireText(namespace, "namespace");
        key = requireText(key, "key");
        fields = Map.copyOf(Objects.requireNonNull(fields, "fields"));
        if (updatedAtEpochMilli < 0L) {
            throw new IllegalArgumentException("updatedAtEpochMilli must be >= 0");
        }
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}
