# Arena Reset Strategies

Questa guida descrive il layer arena introdotto in `0.7.0`.

## API principali
- `ArenaService`: lifecycle arena + orchestrazione reset.
- `ArenaResetStrategy`: strategia pluggable (`key()`, `reset(...)`).
- `ArenaResetPort`: porta adapter-facing per reset backend esterno.

## Strategia default
Il runtime registra due strategy chiave:
- `noop`: `NoopArenaResetStrategy` (safe fallback, risultato `APPLIED`).
- `port-backed`: `PortBackedArenaResetStrategy` (delega a `ArenaResetPort`).

Se una strategy key non è registrata, `DefaultArenaService` fa fallback a `noop` e logga warning.

## Lifecycle arena
- `open(...)` crea arena con stato `ACTIVE`.
- `reset(...)` imposta stato `RESETTING` durante l’operazione.
- a reset concluso, torna `ACTIVE` (o `DISPOSED` se applicabile).
- `dispose(...)` rimuove arena dal registry attivo.

## Throttling reset in-flight
Per ogni arena è consentita una sola reset in flight:
- prima reset: avviata normalmente;
- reset concorrente: `THROTTLED`.

Questo previene race non deterministiche in pipeline di reset world/schematic.

## Esempio rapido
```java
ArenaService arenaService = runtime.services().require(ArenaService.class);

arenaService.open(new ArenaOpenRequest(
        "arena-1",
        "template-desert",
        "arena-world",
        "port-backed",
        Map.of("mode", "duel")
));

ArenaResetResult result = arenaService.reset("arena-1", "round-end")
        .toCompletableFuture()
        .join();
```
