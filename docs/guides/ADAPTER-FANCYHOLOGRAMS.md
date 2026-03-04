# Adapter FancyHolograms

## Obiettivo
Bindare `HologramPort` su backend FancyHolograms in modo opzionale.

## Modulo
- Gradle: `:adapter-fancyholograms`
- Repository: `https://repo.fancyinnovations.com/releases`
- Dependency: `de.oliver:FancyHolograms:2.9.1` (`compileOnly`)

## Component
- `FancyHologramsAdapterComponent`
  - probe plugin `FancyHolograms` + versione minima `2.9.1`
  - bind di `HologramPort` quando disponibile
  - fallback no-op quando assente/incompatibile

## Port implementation
- `FancyHologramsPort`
  - tracking identità in-memory (`UUID -> entry`)
  - operazioni: `create`, `updateLines`, `move`, `delete`
  - comportamento deterministic-safe

## Capability behavior
- available: `fancyholograms:<version>`
- unavailable reason tipici:
  - `missing-plugin:FancyHolograms`
  - `disabled-plugin:FancyHolograms`
  - `incompatible-version:FancyHolograms:<installed><required>`
  - `binding-failed:fancyholograms:<Exception>`

## Server requirement
Plugin `FancyHolograms` installato e enabled sul server.
