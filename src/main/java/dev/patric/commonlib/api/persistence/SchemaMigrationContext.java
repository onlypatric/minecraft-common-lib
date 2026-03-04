package dev.patric.commonlib.api.persistence;

import dev.patric.commonlib.api.ServiceRegistry;
import java.util.Objects;

/**
 * Context provided to schema migrations.
 *
 * @param namespace migration namespace.
 * @param yaml yaml persistence port.
 * @param sql sql persistence port.
 * @param services runtime services.
 */
public record SchemaMigrationContext(
        String namespace,
        YamlPersistencePort yaml,
        SqlPersistencePort sql,
        ServiceRegistry services
) {

    /**
     * Creates schema migration context.
     */
    public SchemaMigrationContext {
        namespace = Objects.requireNonNull(namespace, "namespace").trim();
        if (namespace.isEmpty()) {
            throw new IllegalArgumentException("namespace must not be blank");
        }
        yaml = Objects.requireNonNull(yaml, "yaml");
        sql = Objects.requireNonNull(sql, "sql");
        services = Objects.requireNonNull(services, "services");
    }
}
