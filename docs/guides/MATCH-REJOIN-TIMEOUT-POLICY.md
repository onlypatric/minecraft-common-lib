# MATCH-REJOIN-TIMEOUT-POLICY

## Obiettivo
Definire comportamento deterministico per disconnect/rejoin senza boilerplate ripetuto nei plugin.

## Policy di default
`RejoinPolicy.competitiveDefaults()`:
- `enabled = true`
- `rejoinWindowTicks = 200`
- `sessionTimeoutTicks = 1200`

## Semantica operativa
- `disconnect(matchId, playerId)`:
  - sposta il player in `disconnectedPlayers` con timestamp tick.
- `rejoin(matchId, playerId)`:
  - consentito solo in `LOBBY`, `COUNTDOWN`, `RUNNING`.
  - verifica `enabled`, finestra rejoin e timeout sessione.
- Expiry sessione:
  - oltre `sessionTimeoutTicks` il player viene rimosso dal tracking disconnected.

## Risultati API principali
`RejoinResult`:
- `REJOINED`
- `DENIED_POLICY`
- `DENIED_STATE`
- `WINDOW_EXPIRED`
- `SESSION_EXPIRED`
- `NOT_PARTICIPANT`
- `MATCH_NOT_FOUND`
- `MATCH_CLOSED`

## Auto-abandon
In `COUNTDOWN`/`RUNNING`, se:
- `connectedPlayers` è vuoto,
- non esistono `disconnectedPlayers` ancora rejoinable,

allora il match viene chiuso lungo il path `ENDING` con `EndReason.ABANDONED`.

## Player lifecycle bridge built-in
Il runtime registra un bridge listener:
- `PlayerQuitEvent` -> `onPlayerQuit(playerId)`
- `PlayerChangedWorldEvent` -> `onPlayerWorldChange(playerId)`

Questo mantiene le policy coerenti senza listener duplicati nei plugin consumer.
