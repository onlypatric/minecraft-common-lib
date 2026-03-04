# Adapter Backlog (post v0.8.0)

## Wave 1 completed (`0.8.0`)
- `CommandPort` -> CommandAPI adapter
- `ScoreboardPort` -> FastBoard adapter
- `NpcPort` -> FancyNpcs adapter
- `HologramPort` -> FancyHolograms adapter

## Wave 2 priority (`0.9.0` target)
1. `BossBarPort` -> Adventure/Paper bossbar adapter module
2. `GuiPort` -> InvUI adapter
3. `GuiPort` -> inventory-framework adapter
4. `ClaimsPort` -> HuskClaims adapter
5. `SchematicPort` -> WorldEdit adapter
6. `SchematicPort` -> FAWE adapter
7. `ArenaResetPort` -> advanced world instance/reset adapter
8. `SqlPersistencePort` -> SQL backend adapter module (JDBC/Hikari-based)

## Future wave candidates (>= `1.0.0`)
1. `CommandPort` -> Cloud Commands backend adapter
2. `TeamService/PartyService` -> optional external sync/bridge adapter
3. `BossBarService` high-level backend alternatives

## Adoption criteria
- API maturity: backend API stabile e documentata.
- Paper compatibility: supporto confermato sulla baseline target.
- License fit: conforme a `docs/policy/ADAPTER-LICENSE-POLICY.md`.
- Operational risk: fallback no-op possibile senza rompere i consumer.
- Maintenance signal: progetto attivo con update/release recenti.

## Notes
- Tutti gli adapter restano opzionali e separati dal core.
- Nessuna dipendenza adapter entra nel core `minecraft-common-lib`.
- Capability detection (`CapabilityRegistry`) governa availability a runtime.
- `v0.8.0` chiude la prima wave adapter reale mantenendo modello `embed-first`.
