# Adapter InvUI

## Modulo
`adapter-invui`

## Ruolo
Implementa `GuiPort` con backend **InvUI reale** (window/gui model), mantenendo fallback no-op trasparente via binder runtime.

## Binding
`InvUiAdapterComponent` usa `PortBindingService.bindGuiPort(...)`.

## Dependency setup
Nel progetto common-lib:
- repository: `https://repo.xenondevs.xyz/releases`
- property: `invuiVersion` in `gradle.properties`
- modulo adapter:
  - `compileOnly("xyz.xenondevs.invui:invui-core:$invuiVersion")`
  - `testImplementation("xyz.xenondevs.invui:invui-core:$invuiVersion")`

## Runtime behavior
- `InvUiGuiPort#open(...)` crea una view InvUI (`Gui` + `Window`) da `GuiRenderModel`.
- `close(...)` chiude in modo idempotente la view backend associata alla sessione.
- `render(...)` applica patch in modo deterministic-safe (no throw verso consumer plugin).

## Degrade mode
Se InvUI non è presente/classpath incompatibile:
- port resta fallback no-op,
- `StandardCapabilities.GUI` rimane unavailable con reason esplicita.

Reason codes usati:
- `missing-class:xyz.xenondevs.invui.InvUI`
- `binding-failed:invui:<ExceptionType>`
