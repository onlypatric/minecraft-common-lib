# MATCH-STATE-ENGINE

## Scopo
`api.match` introduce un engine lifecycle riusabile per match plugin-generic, senza legarlo a uno specifico gioco.

## Stato lifecycle
- `LOBBY`
- `COUNTDOWN`
- `RUNNING`
- `ENDING`
- `RESET`

Transizioni contrattuali:
- `LOBBY -> COUNTDOWN`
- `COUNTDOWN -> RUNNING`
- `RUNNING -> ENDING`
- `ENDING -> RESET`
- `RESET -> CLOSED` (chiusura interna)

## Runtime behavior
- Single engine loop sync (`runSyncRepeating(1, 1, ...)`) condiviso tra tutti i match.
- Startup lazy: il loop parte al primo `open(...)`.
- Idle shutdown: quando non ci sono match attivi il loop viene cancellato.
- Ordine deterministico: i match sono processati in ordine di creazione.

## End reasons e cleanup
`EndReason` standard:
- `COMPLETED`
- `TIME_LIMIT`
- `ABANDONED`
- `ADMIN_STOP`
- `PLUGIN_DISABLE`
- `ERROR`

Il `MatchCleanup` è invocato una sola volta in fase terminale e riceve anche `ServiceRegistry` per integrare servizi core (HUD, message, config, ecc.).

## Uso rapido
```java
MatchEngineService engine = runtime.services().require(MatchEngineService.class);

MatchSession session = engine.open(new MatchOpenRequest(
        "duel-main",
        MatchPolicy.competitiveDefaults(),
        new MatchCallbacks() {
            @Override
            public void onStateEnter(MatchSession match) {
                // hook di stato (teleport, ui update, ecc.)
            }
        },
        (match, reason, services) -> {
            // cleanup deterministico (hud, inventory, arena placeholder)
        },
        Set.of(playerId)
));

engine.startCountdown(session.matchId());
```

## Optionalità
Il servizio è registrato di default nel runtime ma resta lazy:
- nessun task attivo finché non apri un match,
- nessun impatto per plugin non-minigame.

## Scope `v0.6.0`
- Include: match lifecycle, timer orchestration, rejoin/timeout base.
- Esclude: arena/team/persistence (previsti in `v0.7.0`).
