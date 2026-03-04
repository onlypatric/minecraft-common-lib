# Adapter Backlog (post v0.9.0)

## Wave 1 completed (`0.8.0`)
- `CommandPort` -> CommandAPI adapter
- `ScoreboardPort` -> FastBoard adapter
- `NpcPort` -> FancyNpcs adapter
- `HologramPort` -> FancyHolograms adapter

## Wave 2 completed (`0.9.0`)
- `ClaimsPort` -> HuskClaims adapter
- `SchematicPort` -> WorldEdit adapter
- `SchematicPort` -> FAWE adapter (precedence winner)
- `BossBarPort` -> Paper bossbar adapter
- `MetricsPort` -> bStats adapter
- `PacketPort` -> ProtocolLib adapter

## Backlog `1.0.x`
1. `GuiPort` -> InvUI adapter
2. `GuiPort` -> inventory-framework adapter
3. `CommandPort` -> Cloud Commands backend adapter
4. `ArenaResetPort` -> advanced world instance/reset adapter
5. `SqlPersistencePort` -> SQL backend adapter module (JDBC/Hikari-based)
6. Packets backend alternative: PacketEvents (evaluation go/no-go)

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
- `v0.9.0` chiude wave 2 e prepara freeze API `1.0.0`.
