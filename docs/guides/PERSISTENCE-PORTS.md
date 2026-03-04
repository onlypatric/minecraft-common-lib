# Persistence Ports

`0.7.0` introduce un layer persistence plugin-generic con due porte.

## Modello record
- `PersistenceRecord(namespace, key, fields, updatedAtEpochMilli)`
- `PersistenceWriteResult`: `APPLIED`, `VALIDATION_FAILED`

## YAML port (core-default)
Interfaccia: `YamlPersistencePort`
- `load(namespace, key)`
- `save(record)`
- `delete(namespace, key)`
- `list(namespace)`

Implementazione default:
- `runtime.persistence.DefaultYamlPersistencePort`
- storage in `${pluginDataFolder}/persistence/<namespace>.yml`

## SQL port (abstraction-only)
Interfaccia: `SqlPersistencePort` asincrona (`CompletionStage`).

Implementazione default:
- `runtime.persistence.NoopSqlPersistencePort`
- `available() == false`
- ritorni safe (nessuna eccezione inattesa)

## Capability runtime
`DefaultCommonRuntime` pubblica:
- `PERSISTENCE_YAML = available("core-default")`
- `PERSISTENCE_SQL = unavailable("No SQL adapter configured")`

## Nota architetturale
In `0.7.0` non è presente alcun backend JDBC reale: la porta SQL rimane contratto pronto per adapter futuri.
