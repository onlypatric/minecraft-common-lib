# minecraft-common-lib

Libreria comune per ridurre boilerplate Bukkit/Paper nei plugin Minecraft del workspace.

## Baseline
- Paper API target: `1.21.x` (baseline build: `1.21.11`)
- Java: `21`
- Modello distribuzione: embed-first (shading nei plugin consumer)

## Stable status (`2.0.0`)
- Release stabile corrente disponibile come `v2.0.0`.
- API freeze `1.0.0` documentato in [`docs/api/API-FREEZE-1.0.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/api/API-FREEZE-1.0.0.md).
- Scope core bloccato: nessun adapter/plugin esterno nel dependency set core.
- Release notes:
  - RC: [`docs/releases/0.1.0-rc.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0-rc.1.md)
  - Stable: [`docs/releases/0.1.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0.md)
  - Stable: [`docs/releases/0.2.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.2.0.md)
  - Stable: [`docs/releases/0.3.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.3.0.md)
  - Stable: [`docs/releases/0.4.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.4.0.md)
  - Stable: [`docs/releases/0.5.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.5.0.md)
  - Stable: [`docs/releases/0.6.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.6.0.md)
  - Stable: [`docs/releases/0.7.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.7.0.md)
  - Stable: [`docs/releases/0.8.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.8.0.md)
  - Stable: [`docs/releases/0.9.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.9.0.md)
  - Stable: [`docs/releases/0.9.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.9.1.md)
  - RC: [`docs/releases/1.0.0-rc.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/1.0.0-rc.1.md)
  - Stable: [`docs/releases/1.0.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/1.0.0.md)
  - RC: [`docs/releases/2.0.0-rc.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/2.0.0-rc.1.md)
  - Stable: [`docs/releases/2.0.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/2.0.0.md)

## Current focus (`2.0.0`)
- GUI major power-up completed:
  - nuovo model tipizzato `api.gui`
  - `GuiDsl` fluente per chest GUI
  - slot policies (`BUTTON_ONLY`, `INPUT_DIALOG`, `TAKE_ONLY`, `DEPOSIT_ONLY`, `TAKE_DEPOSIT`, `LOCKED`)
  - bridge Bukkit inventory/player integrato
  - modulo `adapter-invui` con backend InvUI reale + fallback no-op trasparente

## Boundary policy
- API pubblica principale: `dev.patric.commonlib.api`
- Package `dev.patric.commonlib.internal` non stabili e non contrattuali
- NMS non incluso nel core (`v0.1.x`)

## Breaking changes pre-`1.0.0`
Legacy API rimosse:
- `dev.patric.commonlib.plugin.PluginLifecycle`
- `dev.patric.commonlib.scheduler.Tasks`
- `dev.patric.commonlib.message.MiniMessageService`

Guida migrazione:
- [`docs/guides/MIGRATION-0.x-TO-1.0.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MIGRATION-0.x-TO-1.0.0.md)

## Componenti core disponibili
- Runtime composizionale: `CommonRuntime`
- Component lifecycle: `CommonComponent`
- Service registry: `ServiceRegistry`
- Scheduler facade: `CommonScheduler`
- Config service YAML: `ConfigService`
- Message service advanced: `MessageService` + `MessageRequest` + `FallbackChain` + `PluralRules`
- Event router + policy hooks: `EventRouter`, `PolicyDecision`, `PolicyHook`
- Command model backend-agnostic:
  - `api.command/*` (`CommandModel`, `CommandNode`, `CommandValidator`, `CommandRegistry`, ...)
  - execution utility `command.CommandExecutions`
- GUI session layer backend-agnostic (v2):
  - `api.gui/*` (`GuiDefinition`, `GuiDefinitionRegistry`, `SlotDefinition`, `GuiAction`, `GuiInteractionEvent`, `GuiSessionService`, ...)
  - action first-class: `ToggleStateAction`, `OpenSubMenuAction`, `BackMenuAction`
  - dialog binding: `DialogResponseBinding` + `DialogOpenOptionsMapping.responseBindings`
  - `api.gui.render/*` (`GuiRenderModel`, `GuiRenderPatch`)
  - runtime service `DefaultGuiSessionService`
- HUD primitives backend-agnostic:
  - `api.hud/*` (`ScoreboardSessionService`, `BossBarService`, `HudUpdatePolicy`, ...)
  - runtime services `DefaultScoreboardSessionService`, `DefaultBossBarService`
- Match/state engine backend-agnostic:
  - `api.match/*` (`MatchEngineService`, `MatchState`, `MatchPolicy`, `EndReason`, ...)
  - runtime service `DefaultMatchEngineService`
- Arena/team/persistence foundation:
  - `api.arena/*` (`ArenaService`, `ArenaInstance`, `ArenaResetStrategy`, ...)
  - `api.team/*` (`TeamService`, `PartyService`, `FriendlyFirePolicy`, ...)
  - `api.persistence/*` (`YamlPersistencePort`, `SqlPersistencePort`, `SchemaMigrationService`, ...)
- Plugin-generic ports (adapter-first):
  - `CommandPort`, `GuiPort`, `ScoreboardPort`, `BossBarPort`, `ArenaResetPort`
  - `NpcPort`, `HologramPort`, `ClaimsPort`, `SchematicPort`, `MetricsPort`, `PacketPort`
- Packet API model:
  - `api.packet/*` (`PacketEnvelope`, `PacketListenerOptions`, `PacketListenerHandle`, ...)
- Dialog wrapper core-native:
  - `api.dialog/*` (`DialogService`, `DialogTemplateRegistry`, `DialogSession`, `DialogEvent`, `DialogResponse`, ...)
  - runtime services `DefaultDialogService`, `DefaultDialogTemplateRegistry`
- Capability model:
  - `CapabilityRegistry`, `CapabilityKey`, `CapabilityStatus`, `StandardCapabilities`
- Adapter binding runtime:
  - `api.adapter.PortBindingService`
  - delegating wrappers con fallback no-op trasparente

## Adapter Modules (`0.8.0` wave 1 + `0.9.0` wave 2)
- `adapter-commandapi`: `CommandApiAdapterComponent`, `CommandApiCommandPort`
- `adapter-fastboard`: `FastBoardAdapterComponent`, `FastBoardScoreboardPort`
- `adapter-fancyholograms`: `FancyHologramsAdapterComponent`, `FancyHologramsPort`
- `adapter-fancynpcs`: `FancyNpcsAdapterComponent`, `FancyNpcsPort`
- `adapter-huskclaims`: `HuskClaimsAdapterComponent`, `HuskClaimsClaimsPort`
- `adapter-worldedit`: `WorldEditAdapterComponent`, `WorldEditSchematicPort`
- `adapter-fawe`: `FaweAdapterComponent`, `FaweSchematicPort`
- `adapter-bossbar-paper`: `PaperBossBarAdapterComponent`, `PaperBossBarPort`
- `adapter-bstats`: `BStatsAdapterComponent`, `BStatsMetricsPort`
- `adapter-protocollib`: `ProtocolLibAdapterComponent`, `ProtocolLibPacketPort`
- `adapter-invui`: `InvUiAdapterComponent`, `InvUiGuiPort`
- Guide setup:
  - [`docs/guides/ADAPTER-WAVE1-SETUP.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-WAVE1-SETUP.md)
  - [`docs/guides/ADAPTER-COMMANDAPI.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-COMMANDAPI.md)
  - [`docs/guides/ADAPTER-FASTBOARD.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FASTBOARD.md)
  - [`docs/guides/ADAPTER-FANCYHOLOGRAMS.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FANCYHOLOGRAMS.md)
  - [`docs/guides/ADAPTER-FANCYNPCS.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FANCYNPCS.md)
  - [`docs/guides/ADAPTER-HUSKCLAIMS.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-HUSKCLAIMS.md)
  - [`docs/guides/ADAPTER-WORLDEDIT.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-WORLDEDIT.md)
  - [`docs/guides/ADAPTER-FAWE.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FAWE.md)
  - [`docs/guides/ADAPTER-BOSSBAR-PAPER.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-BOSSBAR-PAPER.md)
  - [`docs/guides/ADAPTER-BSTATS.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-BSTATS.md)
  - [`docs/guides/ADAPTER-PROTOCOLLIB.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-PROTOCOLLIB.md)
  - [`docs/guides/PACKETS-BACKEND-EVALUATION.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/PACKETS-BACKEND-EVALUATION.md)
  - [`docs/guides/EXTERNAL-MATRIX-TESTS.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/EXTERNAL-MATRIX-TESTS.md)

## Build e test
```bash
./gradlew clean test javadoc build
```

## Quickstart
```java
public final class MyPlugin extends JavaPlugin {
    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        var built = RuntimeBootstrap.build(this, builder ->
                builder.component(new MyComponent())
        );
        if (built.isFailure()) {
            throw new IllegalStateException(built.errorOrNull().message(), built.errorOrNull().cause());
        }
        runtime = built.valueOrNull();

        var loadResult = RuntimeBootstrap.safeLoad(runtime);
        if (loadResult.isFailure()) {
            throw new IllegalStateException(loadResult.errorOrNull().message(), loadResult.errorOrNull().cause());
        }
    }

    @Override
    public void onEnable() {
        var enableResult = RuntimeBootstrap.safeEnable(runtime);
        if (enableResult.isFailure()) {
            throw new IllegalStateException(enableResult.errorOrNull().message(), enableResult.errorOrNull().cause());
        }
    }

    @Override
    public void onDisable() {
        RuntimeBootstrap.safeDisable(runtime);
    }
}
```

## How To Verify As New Consumer
1. Add the library to your plugin project and shade it in your final jar.
2. Create a minimal plugin using the quickstart pattern with `RuntimeBootstrap`.
3. Start a Paper `1.21.11` test server and confirm no startup exceptions.
4. Trigger one command/event that touches at least one registered `CommonComponent`.
5. Stop the server and confirm clean disable with no task warnings.
6. Run local quality gates:
   - `./gradlew --no-daemon test`
   - `./gradlew --no-daemon clean test javadoc build`
7. Run the in-repo consumer validation project:
   - `./gradlew --no-daemon -p examples/consumer-demo clean test -PcommonLibJar=../../build/libs/minecraft-common-lib-2.0.0.jar`

## Documentazione
- [ADR-001](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-001-embed-first-no-nms-core.md)
- [ADR-002](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-002-api-boundaries.md)
- [ADR-003](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-003-versioning-changelog-policy.md)
- [API Freeze 1.0.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/api/API-FREEZE-1.0.0.md)
- [Cookbook 5 minuti](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COOKBOOK-5-MINUTES.md)
- [Compatibility Matrix](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COMPATIBILITY-MATRIX.md)
- [Adapter Backlog](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/ADAPTER-BACKLOG.md)
- [Checklist Versioni 0.x -> 1.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/checklist/README.md)
- [Release Checklist](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/RELEASE-CHECKLIST.md)
- [Release Notes 0.1.0-rc.1](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0-rc.1.md)
- [Release Notes 0.1.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0.md)
- [Release Notes 0.2.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.2.0.md)
- [Release Notes 0.3.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.3.0.md)
- [Release Notes 0.4.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.4.0.md)
- [Release Notes 0.5.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.5.0.md)
- [Release Notes 0.6.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.6.0.md)
- [Release Notes 0.7.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.7.0.md)
- [Release Notes 0.8.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.8.0.md)
- [Release Notes 0.9.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.9.0.md)
- [Release Notes 0.9.1](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.9.1.md)
- [Command Model Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/COMMAND-MODEL.md)
- [I18N Advanced Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/I18N-ADVANCED.md)
- [Migration Bukkit Raw Commands](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MIGRATION-BUKKIT-RAW-COMMANDS.md)
- [Migration 0.x to 1.0.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MIGRATION-0.x-TO-1.0.0.md)
- [Troubleshooting](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/TROUBLESHOOTING.md)
- [GUI Session Model Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/GUI-SESSION-MODEL.md)
- [GUI Adapter Mapping Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/GUI-ADAPTER-MAPPING.md)
- [HUD Scoreboard Sessions](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/HUD-SCOREBOARD-SESSIONS.md)
- [BossBar Service Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/BOSSBAR-SERVICE.md)
- [HUD Performance Targets](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/HUD-PERFORMANCE-TARGETS.md)
- [Match State Engine Guide](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MATCH-STATE-ENGINE.md)
- [Match Rejoin & Timeout Policy](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MATCH-REJOIN-TIMEOUT-POLICY.md)
- [Arena Reset Strategies](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ARENA-RESET-STRATEGIES.md)
- [Team And Party Service](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/TEAM-PARTY-SERVICE.md)
- [Persistence Ports](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/PERSISTENCE-PORTS.md)
- [Schema Migrations](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/SCHEMA-MIGRATIONS.md)
- [Arena Reset Benchmark Harness](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ARENA-RESET-BENCHMARK-HARNESS.md)
- [Adapter Wave1 Setup](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-WAVE1-SETUP.md)
- [Adapter CommandAPI](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-COMMANDAPI.md)
- [Adapter FastBoard](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FASTBOARD.md)
- [Adapter FancyHolograms](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FANCYHOLOGRAMS.md)
- [Adapter FancyNpcs](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FANCYNPCS.md)
- [Adapter HuskClaims](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-HUSKCLAIMS.md)
- [Adapter WorldEdit](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-WORLDEDIT.md)
- [Adapter FAWE](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-FAWE.md)
- [Adapter BossBar Paper](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-BOSSBAR-PAPER.md)
- [Adapter bStats](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-BSTATS.md)
- [Adapter ProtocolLib](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-PROTOCOLLIB.md)
- [Packets Backend Evaluation](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/PACKETS-BACKEND-EVALUATION.md)
- [External Matrix Tests](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/EXTERNAL-MATRIX-TESTS.md)
- [Paper Dialog Wrapper](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/PAPER-DIALOG-WRAPPER.md)
- [Paper Dialog Model Reference](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/PAPER-DIALOG-MODEL-REFERENCE.md)
- [GUI Fluent DSL](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/GUI-FLUENT-DSL.md)
- [GUI Slot Policies](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/GUI-SLOT-POLICIES.md)
- [GUI Dialog Integration](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/GUI-DIALOG-INTEGRATION.md)
- [Adapter InvUI](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/ADAPTER-INVUI.md)
- [Migration GUI 1.x to 2.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/guides/MIGRATION-GUI-1.x-TO-2.0.md)
- [Adapter License Policy](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/policy/ADAPTER-LICENSE-POLICY.md)
- [Library Design](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/LIB-DESIGN.md)
- [External Libs Research](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/UTILS-EXTERNAL-LIBS-RESEARCH.md)
