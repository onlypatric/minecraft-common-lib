# Adapter Backlog (post v0.2.0)

## Priority order
1. `CommandPort` -> CommandAPI adapter
2. `ScoreboardPort` -> FastBoard adapter
3. `GuiPort` -> InvUI adapter
4. `ClaimsPort` -> HuskClaims adapter
5. `SchematicPort` -> WorldEdit adapter
6. `SchematicPort` -> FAWE adapter
7. `NpcPort` -> FancyNpcs adapter
8. `HologramPort` -> FancyHolograms adapter
9. `ArenaResetPort` -> advanced world instance/reset adapter

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
