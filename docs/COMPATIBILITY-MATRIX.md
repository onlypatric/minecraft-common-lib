# Compatibility Matrix

## Baseline supportata
- Minecraft server: Paper `1.21.11`
- Java runtime/toolchain: `21`

## Stato release
- `0.1.0-alpha.2`: baseline validata per core runtime/plugin-generic (senza adapter hard esterni).
- `0.1.0-rc.1`: freeze API `0.1.x` confermato, scope core bloccato senza adapter esterni.
- `0.1.0`: prima release stabile core (`production-ready` per adozione iniziale).
- Build di riferimento: `./gradlew --no-daemon clean test javadoc build`.

## Policy corrente
- `v0.1.x` è focalizzata su core runtime generico.
- Nessun adapter esterno incluso in core.
- Nessun supporto NMS diretto nel core.

## Prossimo target
- Mantenere compatibilità su patch/minor `1.21.x` con verifica continua in CI locale.
- Avanzare verso checklist `v0.2.0` con nuove porte plugin-generic.
