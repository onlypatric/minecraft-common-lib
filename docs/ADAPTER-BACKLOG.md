# Adapter Backlog (post v0.7.0)

## Priority order
1. `CommandPort` -> CommandAPI adapter
2. `ScoreboardPort` -> FastBoard adapter
3. `BossBarPort` -> Adventure/Paper bossbar adapter module
4. `GuiPort` -> InvUI adapter
5. `GuiPort` -> inventory-framework adapter
6. `ClaimsPort` -> HuskClaims adapter
7. `SchematicPort` -> WorldEdit adapter
8. `SchematicPort` -> FAWE adapter
9. `NpcPort` -> FancyNpcs adapter
10. `HologramPort` -> FancyHolograms adapter
11. `ArenaResetPort` -> advanced world instance/reset adapter
12. `SqlPersistencePort` -> SQL backend adapter module (JDBC/Hikari-based)
13. `TeamService/PartyService` -> optional external sync/bridge adapter (se necessario)

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
- `v0.6.0` introduce il match/state engine core-only: arena/team/persistence restano pianificati per `v0.7.0` prima delle wave adapter successive.
- `v0.7.0` completa foundation arena/team/persistence nel core: la prossima wave (`v0.8.0`) può concentrarsi su adapter esterni reali senza cambiare i contratti core.
