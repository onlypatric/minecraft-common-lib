# GUI Adapter Mapping Guide

Questa guida descrive il mapping consigliato tra API `v0.4.0` e backend GUI esterni.

## Scope
- Nessun adapter reale è incluso nel core.
- Gli adapter sono moduli separati che implementano `GuiPort` e, opzionalmente, forward degli eventi portabili.

## Mapping target: InvUI
- `GuiSession.viewKey` -> identificatore GUI/template InvUI.
- `GuiState.data` -> model/view state per renderer item dinamici.
- `GuiPort.open(session)` -> apertura inventory/player GUI.
- Eventi click backend -> `GuiClickEvent` verso `GuiSessionService.publish(...)`.
- Close backend -> `GuiCloseEvent` o `GuiDisconnectEvent`.

## Mapping target: inventory-framework
- `viewKey` -> menu factory key.
- `state.data` -> context map per placeholders item/title.
- refresh backend -> `GuiPort.render(sessionId, state)`.

## Requisiti adapter
- Nessuna dipendenza adapter nel core.
- Supporto thread-safety: operazioni Bukkit sempre in sync thread.
- Fallback robusto: su backend error restituire `false` senza lanciare eccezioni non gestite.
- Capability publishing: adapter attivo deve pubblicare `StandardCapabilities.GUI` come available.

## Capability metadata consigliata
- Nome backend (`invui`, `inventory-framework`)
- Versione backend rilevata
- Flag `supportsPortableEvents`
