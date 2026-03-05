# Compatibility Matrix

## Baseline supportata
- Minecraft server: Paper `1.21.x` (baseline build/test: `1.21.11`)
- Java runtime/toolchain: `21`

## Stato release
- `0.1.0-alpha.2`: baseline validata per core runtime/plugin-generic (senza adapter hard esterni).
- `0.1.0-rc.1`: freeze API `0.1.x` confermato, scope core bloccato senza adapter esterni.
- `0.1.0`: prima release stabile core (`production-ready` per adozione iniziale).
- `0.2.0`: baseline porte plugin-generic (`Npc/Hologram/Claims/Schematic`) + capability registry tipizzato.
- `0.3.0`: command model backend-agnostic + i18n advanced (resolver chain, locale fallback multiplo, plural rules base).
- `0.4.0`: GUI session layer (`api.gui`) + `GuiPort` redesign + policy-routed portable events.
- `0.5.0`: HUD primitives (`api.hud`) con scoreboard sessions, bossbar service e rate limiting default.
- `0.6.0`: match/state engine (`api.match`) con loop deterministico, end reasons e policy rejoin/timeout.
- `0.7.0`: foundation arena/team/persistence (`api.arena`, `api.team`, `api.persistence`) con schema migrations integer-based.
- `0.8.0`: adapter wave 1 (`CommandAPI`, `FastBoard`, `FancyHolograms`, `FancyNpcs`) con fallback no-op trasparente.
- `0.9.0`: adapter wave 2 (`HuskClaims`, `WorldEdit`, `FAWE`, `BossBar`, `bStats`, `ProtocolLib`) + packet model wrapper-oriented.
- `0.9.1`: wrapper Dialog API core-native (`api.dialog`) con service/session model, policy hooks e response tipizzato.
- `1.0.0-rc.1`: freeze governance, cleanup legacy e hardening quality gates pre-GA.
- `1.0.0`: GA stabile con freeze API ufficiale e policy semver post-1.0.
- `2.0.0`: GUI power-up major con API v2, policy slot first-class e adapter InvUI backend reale.
- `2.1.0`: Module System first-class (`api.module`) con dependency planner deterministico e lifecycle soft-disable.
- Build di riferimento: `./gradlew --no-daemon clean test javadoc build`.

## Policy corrente
- `v0.1.x` è focalizzata su core runtime generico.
- `v0.2.x` estende il core con porte plugin-generic e no-op ufficiali.
- `v0.3.x` estende il core con command abstraction e message rendering avanzato.
- `v0.4.x` estende il core con gestione GUI session-oriented adapter-friendly.
- `v0.5.x` estende il core con primitive HUD e cleanup audience policy.
- `v0.6.x` estende il core con match lifecycle riusabile e orchestration deterministic-first.
- `v0.7.x` estende il core con foundation arena/team/persistence pronta per adapter wave successive.
- `v0.8.x` introduce adapter reali opzionali in moduli separati (nessuna dipendenza hard nel core).
- `v0.9.x` completa wave 2 adapter e hardening policy/licenze/matrix esterna mantenendo core dependency-safe.
- `v0.9.1` aggiunge integrazione Dialog API Paper nel core senza dipendenze esterne aggiuntive.
- `v1.0.0` formalizza freeze API pubblico (`api/*`, `api/port/*`) con compat policy stabile.
- `v2.0.0` stabilizza il redesign GUI major con adapter InvUI in modulo separato.
- `v2.1.0` aggiunge orchestration modulare nativa mantenendo compatibilita' con `CommonComponent`.
- Nessun supporto NMS diretto nel core.

## Policy ufficiale `1.0.0`
- Support scope ufficiale: Paper `1.21.x` + Java `21`.
- Fuori scope ufficiale: minor Paper precedenti/non `1.21.x`.
- Compatibilita' public API regolata da freeze `docs/api/API-FREEZE-1.0.0.md`.

## Prossimo target
- Stabilizzazione `2.1.x` + preparazione wave GUI successiva (`inventory-framework`) e hardening module ecosystem.
