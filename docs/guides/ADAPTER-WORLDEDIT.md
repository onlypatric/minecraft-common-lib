# Adapter WorldEdit

## Modulo
- `adapter-worldedit`

## Scopo
Fornire backend `SchematicPort` con binding opzionale separato.

## Component
- `WorldEditAdapterComponent`
- `WorldEditSchematicPort`
- capability: `StandardCapabilities.SCHEMATIC`

## Note di precedenza
Quando è presente anche FAWE, la selezione finale è gestita dal binder core:
- `fawe` priority `200`
- `worldedit` priority `100`

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-worldedit:test
```
