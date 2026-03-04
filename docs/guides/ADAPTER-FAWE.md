# Adapter FAWE

## Modulo
- `adapter-fawe`

## Scopo
Fornire backend `SchematicPort` preferenziale per reset/paste.

## Component
- `FaweAdapterComponent`
- `FaweSchematicPort`
- capability: `StandardCapabilities.SCHEMATIC`

## Precedenza
Il binder V2 garantisce che, se presenti entrambi i backend schematic:
- FAWE resta il delegate attivo.

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-fawe:test
```
