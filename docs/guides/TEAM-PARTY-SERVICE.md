# Team And Party Service

`0.7.0` introduce due servizi core in-memory: `TeamService` e `PartyService`.

## TeamService
`TeamService` è scoped per `matchId`.

Operazioni principali:
- `createRoster(matchId, definitions, policy)`
- `assign(...)`, `autoAssign(...)`
- `teamOf(...)`, `snapshot(...)`
- `canDamage(...)` con `FriendlyFirePolicy`
- `removePlayer(...)`, `clearRoster(...)`

### Friendly-fire
- `ALLOW`: nessuna restrizione.
- `DENY_SAME_TEAM`: blocca danno tra membri dello stesso team.

### Auto-assignment
`autoAssign` sceglie il team meno popolato con slot disponibile.

## PartyService
`PartyService` gestisce party persistenti in memoria con indice membro->party.

Operazioni principali:
- `create(leaderId)`
- `invite(...)`, `acceptInvite(...)`
- `kick(...)`, `leave(...)`, `disband(...)`
- `find(...)`, `findByMember(...)`

Regole base:
- solo leader invita/kicka/disbanda;
- membri già in party non possono accettare inviti di altri party;
- `leave` del leader disbanda il party.

## Integrazione con match
Per evitare leak di roster team a fine match, è disponibile helper:
- `MatchFoundationHooks.withTeamCleanup(callbacks, teamService)`

Questo wrapper esegue `teamService.clearRoster(matchId)` in `onEnd`.
