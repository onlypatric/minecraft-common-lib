# Adapter BossBar Paper

## Modulo
- `adapter-bossbar-paper`

## Scopo
Implementare `BossBarPort` usando API native Paper/Bukkit.

## Component
- `PaperBossBarAdapterComponent`
- `PaperBossBarPort`
- capability: `StandardCapabilities.BOSSBAR`

## Mapping
- `HudBarColor` -> `BarColor`
- `HudBarStyle` -> `BarStyle`
- progress clamped a `[0.0, 1.0]`

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-bossbar-paper:test
```
