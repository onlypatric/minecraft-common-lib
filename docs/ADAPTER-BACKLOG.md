# Adapter Backlog (post v0.1.x)

## Priority order
1. `CommandPort` -> CommandAPI adapter
2. `ScoreboardPort` -> FastBoard adapter
3. `GuiPort` -> InvUI adapter
4. `ArenaResetPort` -> WorldEdit adapter
5. `ArenaResetPort` -> FAWE adapter
6. `NpcPort` (future) -> FancyNpcs adapter
7. `HologramPort` (future) -> FancyHolograms adapter

## Notes
- Tutti gli adapter restano opzionali e separati dal core.
- Nessuna dipendenza adapter entra in `v0.1.x`.
- FancyNpcs e FancyHolograms restano integrazioni future opzionali (no dipendenza hard nel core).
