# Adapter CommandAPI

## Obiettivo
Bindare `CommandPort` su backend CommandAPI quando il plugin `CommandAPI` è disponibile.

## Modulo
- Gradle: `:adapter-commandapi`
- Dependency principale: `dev.jorel:commandapi-paper-core:11.1.0` (`compileOnly`)

## Component
- `CommandApiAdapterComponent`
  - `onEnable`: probe dependency + bind tramite `PortBindingService`.
  - se probe fallisce: `markUnavailable(StandardCapabilities.COMMAND, reason)`.

## Port implementation
- `CommandApiCommandPort`
  - `register(CommandModel)` con duplicate guard su root normalizzata.
  - `unregister(String)` idempotente.
  - `supportsSuggestions() == true`.

## Capability behavior
- available: `commandapi:<version>`
- unavailable reason tipici:
  - `missing-plugin:CommandAPI`
  - `disabled-plugin:CommandAPI`
  - `incompatible-version:CommandAPI:<installed><required>`
  - `binding-failed:commandapi:<Exception>`

## Consumer note
`CommandApiAdapterComponent` va registrato nel runtime builder del plugin host.
