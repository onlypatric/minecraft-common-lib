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

## Backlog `2.1.x+`
1. `GuiPort` -> inventory-framework adapter
2. `CommandPort` -> Cloud Commands backend adapter
3. `ArenaResetPort` -> advanced world instance/reset adapter
4. `SqlPersistencePort` -> SQL backend adapter module (JDBC/Hikari-based)
5. Packets backend alternative: PacketEvents (evaluation go/no-go)

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
- `v2.0.0` chiude il primo ciclo GUI adapter con InvUI backend reale.
