# BossBar Service Guide

`v0.5.0` aggiunge `BossBarService` come astrazione bossbar plugin-generic.

## API principali
- `BossBarService`: open/find/update/close + cleanup hooks.
- `BossBarSession`: snapshot sessione bossbar.
- `BossBarState`: stato (`title`, `progress`, `color`, `style`, `visible`).
- `BossBarPort`: porta adapter-facing per il backend.

## Validazioni
- `progress` deve essere in `[0.0, 1.0]`.
- `title` non deve essere blank nelle update.
- payload invariato con dedupe attivo ritorna `DEDUPED`.

## Rate limiting
Lo stesso `HudUpdatePolicy` usato per scoreboard si applica anche ai bossbar update.
Default: max 1 render ogni 5 tick per audience.

## Cleanup
- `onPlayerQuit(UUID)` e `onPlayerWorldChange(UUID)` chiudono le bar dell’audience.
- `closeAll(PLUGIN_DISABLE)` è invocato dal runtime in `onDisable()`.
