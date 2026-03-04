# Adapter Wave 1 Setup (`0.8.0`)

## Scope
Wave 1 abilita adapter reali per:
- `CommandPort` -> CommandAPI
- `ScoreboardPort` -> FastBoard
- `HologramPort` -> FancyHolograms
- `NpcPort` -> FancyNpcs

Gli adapter restano opzionali: se non bindano, il runtime usa fallback no-op e capability `unavailable(...)`.

## Moduli Gradle
- `:adapter-commandapi`
- `:adapter-fastboard`
- `:adapter-fancyholograms`
- `:adapter-fancynpcs`

Ogni modulo dipende da `project(":")` e non può dipendere da altri adapter (`verifyAdapterDependencyPolicy`).

## Installazione lato plugin consumer
1. Embeddare `minecraft-common-lib` nel plugin.
2. Embeddare i moduli adapter necessari.
3. Registrare i component adapter nel runtime builder.

Esempio:
```java
runtime = CommonRuntime.builder(this)
        .component(new CommandApiAdapterComponent())
        .component(new FastBoardAdapterComponent())
        .component(new FancyHologramsAdapterComponent())
        .component(new FancyNpcsAdapterComponent())
        .build();
```

## Requisiti server-side
- CommandAPI: plugin installato e enabled, versione >= `11.1.0`.
- FastBoard: nessun plugin server richiesto (library adapter).
- FancyHolograms: plugin installato e enabled, versione >= `2.9.1`.
- FancyNpcs: plugin installato e enabled, versione >= `2.9.0`.

## Capability detection
`PortBindingService` aggiorna delegate + `CapabilityRegistry` in modo atomico.

Reason code standard:
- `missing-plugin:<name>`
- `disabled-plugin:<name>`
- `incompatible-version:<name>:<installed><required>`
- `missing-class:<fqcn>`
- `binding-failed:<name>:<exception>`

Metadata `available`:
- `commandapi:<version>`
- `fastboard:<version>`
- `fancyholograms:<version>`
- `fancynpcs:<version>`

## Verifica rapida
```bash
./gradlew --no-daemon test
./gradlew --no-daemon adapterIntegrationTest -PrunAdapterIntegration=true
```
