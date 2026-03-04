# Adapter bStats

## Modulo
- `adapter-bstats`

## Scopo
Fornire `MetricsPort` opzionale separato dal core.

## Component
- `BStatsAdapterComponent`
- `BStatsMetricsPort`
- capability: `StandardCapabilities.METRICS`

## Requisito plugin id
`BStatsAdapterComponent` richiede un `pluginId > 0`.
In caso contrario il binding resta unavailable con reason:
- `binding-failed:bstats:invalid-plugin-id`

## Relocation policy (consumer)
Se il plugin consumer effettua shading della libreria, mantenere il modulo adapter separato
oppure applicare relocation dedicata lato consumer per evitare conflitti classpath.

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-bstats:test
```
