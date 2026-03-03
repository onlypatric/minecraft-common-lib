# GUI Session Model Guide

`v0.4.0` introduce un layer GUI session-oriented backend-agnostic in `dev.patric.commonlib.api.gui`.

## Obiettivo
Ridurre il boilerplate GUI e rendere portabile il flusso `open/update/close/timeout` verso adapter diversi.

## Blocchi principali
- `GuiSessionService`: API centrale per lifecycle sessioni.
- `GuiSession`: snapshot immutable della sessione.
- `GuiState`: stato serializzabile con `revision` per optimistic concurrency.
- `GuiEvent`: eventi portabili (`click/close/timeout/disconnect`).
- `GuiPort`: porta adapter-facing per backend rendering.

## Lifecycle
1. `open(GuiOpenRequest)` crea una sessione `OPEN`.
2. Se `timeoutTicks > 0`, viene schedulato timeout automatico.
3. `update(...)` richiede `expectedRevision` uguale a quella corrente.
4. `close(...)` è idempotente: solo la prima chiusura vince.
5. In shutdown runtime: `closeAll(PLUGIN_DISABLE)`.

## Revisioning e race-condition
- Se `expectedRevision` non combacia: `STALE_REVISION`.
- Eventi/update su sessione non `OPEN`: `SESSION_NOT_OPEN`.
- Race `close/disconnect/timeout`: una sola transizione finale (`CLOSED` o `TIMED_OUT`).

## Esempio minimo
```java
GuiSessionService gui = services.require(GuiSessionService.class);
GuiSession session = gui.open(new GuiOpenRequest(playerId, "menu.shop", GuiState.empty(), 200L));

GuiUpdateResult update = gui.update(
        session.sessionId(),
        GuiState.withData(session.state().revision(), Map.of("page", "2")),
        session.state().revision()
);

if (update != GuiUpdateResult.APPLIED) {
    // handle stale/not-open cases
}
```
