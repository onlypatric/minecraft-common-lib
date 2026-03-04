# GUI Slot Policies

## Policy disponibili
- `BUTTON_ONLY`: slot cliccabile, no transfer item.
- `INPUT_DIALOG`: click apre dialog/azioni input.
- `TAKE_ONLY`: consente prelievo.
- `DEPOSIT_ONLY`: consente deposito.
- `TAKE_DEPOSIT`: consente entrambe.
- `LOCKED`: blocco totale.

## Pattern consigliati
- Button command: `BUTTON_ONLY` + `RunCommandAction`.
- Input testo: `INPUT_DIALOG` + `OpenDialogAction` con `DialogResponseBinding`.
- Switch on/off: `BUTTON_ONLY` + `ToggleStateAction` (helper `switchSlot`).
- Submenu: `BUTTON_ONLY` + `OpenSubMenuAction` (helper `subMenuSlot`).
- Back navigation: `BUTTON_ONLY` + `BackMenuAction` (helper `backSlot`).
- Slot inventario reale: `TAKE_ONLY` / `DEPOSIT_ONLY` / `TAKE_DEPOSIT`.

## Regola pratica
Usa `BUTTON_ONLY` o `LOCKED` per slot puramente UI e riserva le policy transfer solo agli slot inventario funzionali.
