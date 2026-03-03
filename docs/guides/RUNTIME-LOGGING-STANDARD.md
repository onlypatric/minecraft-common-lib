# Runtime Logging Standard

Questo documento definisce lo standard di logging runtime della libreria common.

## Obiettivi
- output consistente tra componenti.
- tracciabilità delle fasi lifecycle.
- separazione chiara tra info operative e errori.

## Prefisso standard
- Prefisso obbligatorio: `[common-lib]`.
- Qualunque log runtime emesso dal core deve usare il prefisso.

## Mapping livelli
- `debug`: diagnosi lifecycle e flussi interni.
- `info`: eventi operativi principali.
- `warn`: degrado controllato / fallback.
- `error`: failure non recuperabile nel ciclo corrente.

## Eventi lifecycle minimi da loggare
- `onLoad -> <component-id>`
- `onEnable -> <component-id>`
- `onDisable -> <component-id>`

## Regole pratiche
- Evitare stacktrace in `debug`.
- Usare `error(message, throwable)` per failure che causano rollback.
- Mantenere `component-id` stabile e umano.
