# Adapter FancyNpcs

## Obiettivo
Bindare `NpcPort` su backend FancyNpcs in modo opzionale.

## Modulo
- Gradle: `:adapter-fancynpcs`
- Repository: `https://repo.fancyinnovations.com/releases`
- Dependency: `de.oliver:FancyNpcs:2.9.0` (`compileOnly`)

## Component
- `FancyNpcsAdapterComponent`
  - probe plugin `FancyNpcs` + versione minima `2.9.0`
  - bind di `NpcPort` quando disponibile
  - fallback no-op quando assente/incompatibile

## Port implementation
- `FancyNpcsPort`
  - tracking identità in-memory (`UUID -> entry`)
  - operazioni: `spawn`, `despawn`, `updateDisplayName`, `teleport`
  - comportamento deterministic-safe

## Capability behavior
- available: `fancynpcs:<version>`
- unavailable reason tipici:
  - `missing-plugin:FancyNpcs`
  - `disabled-plugin:FancyNpcs`
  - `incompatible-version:FancyNpcs:<installed><required>`
  - `binding-failed:fancynpcs:<Exception>`

## Server requirement
Plugin `FancyNpcs` installato e enabled sul server.
