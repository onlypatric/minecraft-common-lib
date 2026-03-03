# Compatibility Matrix

## Baseline supportata
- Minecraft server: Paper `1.21.11`
- Java runtime/toolchain: `21`

## Stato release
- `0.1.0-alpha.2`: baseline validata per core runtime/plugin-generic (senza adapter hard esterni).
- Build di riferimento: `./gradlew --no-daemon clean test javadoc build`.

## Policy corrente
- `v0.1.x` è focalizzata su core runtime generico.
- Nessun adapter esterno incluso in core.
- Nessun supporto NMS diretto nel core.

## Prossimo target
- Mantenere compatibilità su patch/minor `1.21.x` con verifica continua in CI locale.
