# Schema Migrations

`SchemaMigrationService` gestisce versioning schema per namespace con versioni integer.

## API
- `currentVersion(namespace)`
- `register(namespace, migration)`
- `migrateToLatest(namespace)`

## Vincoli migrator
Ogni `SchemaMigration` deve rispettare:
- `fromVersion >= 0`
- `toVersion >= 0`
- `toVersion == fromVersion + 1`

Se la catena è incompleta, `migrateToLatest` fallisce fast con `IllegalStateException`.

## Persistenza versione
Versioni correnti salvate in:
- `${pluginDataFolder}/schema-versions.yml`

Struttura:
```yaml
versions:
  my-namespace: 2
```

## Esempio
```java
SchemaMigrationService migrations = runtime.services().require(SchemaMigrationService.class);

migrations.register("player-stats", new SchemaMigration() {
    @Override
    public int fromVersion() { return 0; }

    @Override
    public int toVersion() { return 1; }

    @Override
    public void migrate(SchemaMigrationContext context) {
        // trasformazione dati
    }
});

int version = migrations.migrateToLatest("player-stats");
```

## Scope 0.7.0
- supporto completo a migrator chain integer-based;
- nessuna migrazione automatica SQL reale (porta SQL è abstraction-only).
