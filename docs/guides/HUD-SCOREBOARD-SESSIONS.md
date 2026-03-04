# HUD Scoreboard Sessions

`v0.5.0` introduce un modello high-level per scoreboard session-oriented in `dev.patric.commonlib.api.hud`.

## Concetti principali
- `ScoreboardSessionService`: servizio centrale per lifecycle scoreboard.
- `ScoreboardSession`: snapshot immutable di una sessione.
- `ScoreboardSnapshot`: payload (`title`, `lines`) da renderizzare.
- `HudUpdatePolicy`: policy di throttling/dedup.

## Lifecycle
1. `open(ScoreboardOpenRequest)` apre una sessione e invoca `ScoreboardPort.open(...)`.
2. `update(...)` valida payload e applica policy:
   - `APPLIED` se render immediato consentito,
   - `THROTTLED` se fuori finestra minima,
   - `DEDUPED` se payload invariato.
3. `close(...)` chiude sessione con `HudAudienceCloseReason`.

## Policy default
`HudUpdatePolicy.competitiveDefaults()`:
- `minUpdateIntervalTicks = 5`
- `deduplicatePayload = true`
- `maxScoreboardLines = 15`

## Cleanup hooks
- `onPlayerQuit(UUID)` -> chiusura con reason `QUIT`.
- `onPlayerWorldChange(UUID)` -> chiusura con reason `WORLD_CHANGE`.
- in shutdown runtime: close con `PLUGIN_DISABLE`.

## Note per adapter
- Il core non include adapter esterni.
- `ScoreboardPort` resta una porta adapter-facing; implementazioni reali arriveranno in wave adapter successive.
