# Migrazione GUI 1.x -> 2.0

## Cambi principali
- `GuiSessionService` introduce `open(GuiDefinition, UUID, GuiOpenOptions)` e `interact(GuiInteractionEvent)`.
- `GuiPort` passa da `open/render` su session/state legacy a `GuiRenderModel/GuiRenderPatch`.
- Nuovo model tipizzato per slot/actions/policies.

## Mapping rapido
- `GuiOpenRequest` -> `GuiDefinition + GuiOpenOptions`
- `publish(GuiClickEvent)` -> `interact(SlotClickEvent)`
- `supportsPortableEvents()` -> `supports(GuiPortFeature.CLICK)`

## Pattern principali (before/after)

### 1) Button semplice
- Prima: listener Bukkit + gestione click manuale.
- Dopo:
```java
.button(10, GuiItemView.of("DIAMOND", "Buy"), List.of(new RunCommandAction("shop buy", false)))
```

### 2) Input testo con Dialog API
- Prima: aprire dialog manualmente e sincronizzare stato GUI a mano.
- Dopo:
```java
.dialogInputSlot(
        13,
        GuiItemView.of("PAPER", "Rinomina"),
        "dialog.shop.rename",
        List.of(new DialogResponseBinding("name", "shop.name", true))
)
```

### 3) Navigazione submenu
- Prima: close/open menu manuale, nessuno stack standard.
- Dopo:
```java
.subMenuSlot(31, GuiItemView.of("CHEST", "Categorie"), "menu.shop.categories")
.backSlot(45, GuiItemView.of("ARROW", "Back"))
```

## Nota compatibilità
Nel ciclo corrente sono mantenuti bridge legacy (`open(GuiOpenRequest)`, `publish(GuiEvent)`) per migrazione graduale.

Per backend GUI reale aggiungi anche il modulo `adapter-invui` e registra `InvUiAdapterComponent` nel runtime del plugin host.
