# Changelog

## [2.1.0] - 2026-03-04
### Added
- Nuovo Module System pubblico in `api.module`:
  - `CommonModule`, `ModuleState`, `ModuleStatus`, `ModuleRegistry`, `ModulePlanResult`
- Estensioni runtime builder non-breaking:
  - `CommonRuntime.Builder#module(...)`
  - `CommonRuntime.Builder#modules(...)`
  - `CommonRuntime.Builder#enableModuleDiagnostics(...)`
- Nuovo overload bootstrap:
  - `RuntimeBootstrap.build(JavaPlugin, Collection<? extends CommonModule>)`
- Nuova orchestrazione runtime moduli:
  - `DefaultModuleRegistry`
  - `ModuleGraphPlanner`
  - `ModuleLifecycleOrchestrator`
  - `ComponentModuleAdapter`
- Nuovo testing ground:
  - `examples/module-playground`
- `SERVER-TEST` esteso con diagnostica moduli:
  - comando smoke `clibsmoke dumpmodules`
  - parsing `CLIB_SMOKE_MODULES`
  - report `modules.csv` + sezione module status in `report.md/report.json`

### Changed
- `DefaultCommonRuntime` ora esegue lifecycle moduli prima dei componenti in load/enable e dopo i componenti in disable.
- `ServiceRegistry` espone `ModuleRegistry` e `ModulePlanResult`.
- `verifyCoreDependencyPolicy` estesa a `api/module`.

## [2.0.0] - 2026-03-04
### Added
- GUI v2 foundation (`api.gui`) con model tipizzato:
  - `GuiDefinition`, `GuiLayout`, `SlotDefinition`, `SlotInteractionPolicy`, `GuiAction`
  - eventi portabili v2 (`GuiInteractionEvent/*`) e risultati (`GuiInteractionResult`)
  - DTO render adapter-facing: `api.gui.render.GuiRenderModel`, `GuiRenderPatch`
- GUI interaction pack:
  - switch stateful (`ToggleStateAction`, `GuiDsl#switchSlot`)
  - submenu stack navigation (`OpenSubMenuAction`, `BackMenuAction`, `GuiDsl#subMenuSlot`, `GuiDsl#backSlot`)
  - dialog response bindings (`DialogResponseBinding`, `GuiDsl#dialogInputSlot`, `DialogOpenOptionsMapping#responseBindings`)
  - menu registry runtime (`GuiDefinitionRegistry`, `DefaultGuiDefinitionRegistry`)
- Fluent DSL iniziale:
  - `GuiDsl` con builder `chest(key, rows)` e helper slot/policy.
- Modulo adapter GUI:
  - `adapter-invui` con `InvUiGuiPort` e `InvUiAdapterComponent`.
- Test adapter InvUI aggiuntivi:
  - `InvUiInteractionForwardingTest`
  - `InvUiFallbackNoDependencySmokeTest`
- Runtime bridge inventory/player per GUI v2:
  - `GuiPlayerInventoryBridge`
  - `GuiInteractionPolicyRoutedEvent`
- Binding GUI esteso:
  - `PortBindingService#bindGuiPort(...)`
  - `DefaultPortBindingService` + `DelegatingGuiPort`

### Changed
- `GuiSessionService` esteso con API v2:
  - `open(GuiDefinition, UUID, GuiOpenOptions)`
  - `interact(GuiInteractionEvent)`
  - bridge legacy mantenuti (`open(GuiOpenRequest)`, `publish(GuiEvent)`) per migrazione controllata.
- `GuiPort` redesign adapter-facing:
  - `open(GuiRenderModel)`, `render(UUID, GuiRenderPatch)`, `supports(GuiPortFeature)`
  - bridge legacy mantenuti (`open(GuiSession)`, `render(UUID, GuiState)`, `supportsPortableEvents()`).
- `DefaultCommonRuntime` ora registra `GuiPort` delegating + fallback no-op e installa bridge Bukkit GUI.
- `InvUiGuiPort` ora usa backend InvUI reale (`Gui`/`Window`) al posto del fallback Bukkit diretto.
- Build wiring InvUI completato:
  - repo `https://repo.xenondevs.xyz/releases`
  - property `invuiVersion` in `gradle.properties`
  - dependency `xyz.xenondevs.invui:invui-core` in `adapter-invui`.

## [1.0.0] - 2026-03-04
### Added
- Release GA `1.0.0` della libreria common con freeze API formalizzato.
- Release notes GA:
  - `docs/releases/1.0.0.md`
- Checklist `v1.0.0` chiusa con evidenze:
  - `docs/checklist/13-v1.0.0.md`

### Changed
- Consolidato candidate `1.0.0-rc.1` senza regressioni aggiuntive.
- Policy compatibilita' ufficiale confermata:
  - Paper `1.21.x`
  - Java `21`
- Documentazione allineata a stato stabile `1.0.0`:
  - `README.md`
  - `docs/COMPATIBILITY-MATRIX.md`
  - `docs/RELEASE-CHECKLIST.md`

## [1.0.0-rc.1] - 2026-03-04
### Added
- Freeze governance ufficiale `1.0.0`:
  - `docs/api/API-FREEZE-1.0.0.md`
  - update policy in `docs/policy/PACKAGE-STABILITY-POLICY.md`
  - update ADR boundaries/versioning:
    - `docs/adr/ADR-002-api-boundaries.md`
    - `docs/adr/ADR-003-versioning-changelog-policy.md`
- Documentazione migrazione/troubleshooting pre-GA:
  - `docs/guides/MIGRATION-0.x-TO-1.0.0.md`
  - `docs/guides/TROUBLESHOOTING.md`
- Regressione pre-GA aggiunta:
  - `HudServiceBootstrapSafetyTest` per garantire che HUD services non schedulino task in constructor (`onLoad`-safe).
- Runbook RC:
  - `docs/releases/1.0.0-rc.1.md`

### Changed
- Rimosse API legacy deprecate prima del freeze `1.0.0`:
  - `dev.patric.commonlib.plugin.PluginLifecycle`
  - `dev.patric.commonlib.scheduler.Tasks`
  - `dev.patric.commonlib.message.MiniMessageService`
- Contract test API aggiornati per assert espliciti di assenza classi legacy.
- Javadoc quality gate hardening:
  - build configurata con `-Werror` (warning zero richiesto su root + subprojects).
- HUD services runtime hardening:
  - `DefaultScoreboardSessionService` e `DefaultBossBarService` avviano il flush loop in modo lazy, evitando scheduling in `onLoad`.
- Consumer demo hardening:
  - jar demo ora embedd-a la common-lib passata via `-PcommonLibJar` (embed-first reale).
  - `DemoConsumerPlugin` gestisce in modo esplicito il caso runtime non costruito.

## [0.9.1] - 2026-03-04
### Added
- Nuovo package `api.dialog` con wrapper Paper Dialog API:
  - services/registry: `DialogService`, `DialogTemplateRegistry`
  - session/event model: `DialogOpenRequest`, `DialogSession`, `DialogEvent`, `DialogSubmission`
  - callback/response model: `DialogCallbacks`, `DialogResponse`
  - full template model: `DialogTemplate`, `DialogBaseSpec`, `DialogBodySpec`, `DialogInputSpec`, `DialogTypeSpec`, `DialogActionSpec`
- Nuove implementazioni runtime dialog:
  - `DefaultDialogService`
  - `DefaultDialogTemplateRegistry`
  - `DialogTemplateCompiler`
  - `DialogTemplateValidator`
  - `DialogPlayerLifecycleBridge`
  - `DialogPolicyRoutedEvent`
- Capability model esteso:
  - `StandardCapabilities.DIALOG`
- Nuovi test dialog:
  - `DialogApiContractTest`
  - `DialogTemplateCompilerTest`
  - `DialogServiceLifecycleTest`
  - `DialogPolicyHookCompatibilityTest`
  - `DialogResponseExtractionTest`
  - `DialogListResolutionTest`
  - `DialogDisableCleanupTest`
- Nuove guide/documentazione:
  - `docs/guides/PAPER-DIALOG-WRAPPER.md`
  - `docs/guides/PAPER-DIALOG-MODEL-REFERENCE.md`
  - `docs/releases/0.9.1.md`

### Changed
- `DefaultCommonRuntime` ora registra di default `DialogService` e `DialogTemplateRegistry`.
- `onDisable()` runtime ora chiude le sessioni dialog con reason `PLUGIN_DISABLE` prima di `scheduler.cancelAll()`.
- `verifyCoreDependencyPolicy` estesa al package `api/dialog`.
- Semantica degrade-safe in ambienti senza dialog registry Paper completa: compiler/runtime mantengono session tracking senza crash del plugin.

## [0.9.0] - 2026-03-04
### Added
- Estensione API binding V2:
  - `PortBindingService#bindClaimsPort`
  - `PortBindingService#bindSchematicPort`
  - `PortBindingService#bindBossBarPort`
  - `PortBindingService#bindMetricsPort`
  - `PortBindingService#bindPacketPort`
- Nuova API metrics:
  - `MetricsPort`
  - `NoopMetricsPort`
- Nuova API packets:
  - `PacketPort`
  - `NoopPacketPort`
  - `api.packet/*` (`PacketDirection`, `PacketListenerPriority`, `PacketListenerOptions`, `PacketEnvelope`, `PacketListenerHandle`)
- Nuovi delegating wrappers/runtime binder V2:
  - `DelegatingClaimsPort`
  - `DelegatingSchematicPort`
  - `DelegatingBossBarPort`
  - `DelegatingMetricsPort`
  - `DelegatingPacketPort`
  - `DefaultPortBindingService` esteso con precedence schematic (`fawe > worldedit`)
- Nuove capability standard:
  - `StandardCapabilities.METRICS`
  - `StandardCapabilities.PACKETS`
- Nuovi moduli adapter wave 2:
  - `adapter-huskclaims`
  - `adapter-worldedit`
  - `adapter-fawe`
  - `adapter-bossbar-paper`
  - `adapter-bstats`
  - `adapter-protocollib`
- Nuovi adapter/component:
  - `HuskClaimsAdapterComponent`, `HuskClaimsClaimsPort`
  - `WorldEditAdapterComponent`, `WorldEditSchematicPort`
  - `FaweAdapterComponent`, `FaweSchematicPort`
  - `PaperBossBarAdapterComponent`, `PaperBossBarPort`
  - `BStatsAdapterComponent`, `BStatsMetricsPort`
  - `ProtocolLibAdapterComponent`, `ProtocolLibPacketPort`
- Nuovi test wave 2:
  - core: `PortBindingServiceV2Test`, `SchematicBindingPriorityTest`, `PacketApiContractTest`, `MetricsPortContractTest`, `AdapterCapabilityTransitionV2Test`, `AdapterFallbackNoopTransparencyV2Test`, `AdapterWave2StartupOrderingTest`
  - adapter modules: `HuskClaimsAdapterComponentTest`, `HuskClaimsClaimsPortTest`, `WorldEditAdapterComponentTest`, `WorldEditSchematicPortTest`, `FaweAdapterComponentTest`, `FaweSchematicPortTest`, `PaperBossBarAdapterComponentTest`, `PaperBossBarPortTest`, `BStatsAdapterComponentTest`, `BStatsMetricsPortTest`, `ProtocolLibAdapterComponentTest`, `ProtocolLibPacketPortTest`, `ProtocolLibVersionFlexibilityTest`
  - matrix/stress: `AdapterWave2NoDependencySmokeTest`, `AdapterWave2WithDependencySmokeTest`, `ExternalClaimsNpcBossPacketsMatrixTest`, `ExternalProtocolLibHandshakeTest`, `ExternalHuskClaimsHandshakeTest`, `ExternalWorldEditFaweHandshakeTest`, `EventRouterOverheadBenchmarkTest`, `ArenaResetAdapterBenchmarkHarnessTest`, `MultiMatchWithAdaptersStressTest`
- Nuove guide/documenti:
  - `docs/guides/ADAPTER-HUSKCLAIMS.md`
  - `docs/guides/ADAPTER-WORLDEDIT.md`
  - `docs/guides/ADAPTER-FAWE.md`
  - `docs/guides/ADAPTER-BOSSBAR-PAPER.md`
  - `docs/guides/ADAPTER-BSTATS.md`
  - `docs/guides/ADAPTER-PROTOCOLLIB.md`
  - `docs/guides/PACKETS-BACKEND-EVALUATION.md`
  - `docs/guides/EXTERNAL-MATRIX-TESTS.md`
  - `docs/releases/0.9.0.md`

### Changed
- `DefaultCommonRuntime` ora registra delegate/fallback anche per `ClaimsPort`, `SchematicPort`, `BossBarPort`, `MetricsPort`, `PacketPort`.
- `check` ora include `verifyAdapterLicensePolicy` oltre a `verifyCoreDependencyPolicy` e `verifyAdapterDependencyPolicy`.
- `build.gradle.kts` include source set/task opt-in `externalMatrixTest` (`-PrunExternalMatrix=true`).
- Policy licenze adapter aggiornata con enforcement build esplicito per moduli GPL/proprietari separati.

## [0.8.0] - 2026-03-04
### Added
- Nuovo package API adapter:
  - `api.adapter.PortBindingService`
- Nuovi runtime adapter primitives:
  - `DefaultPortBindingService`
  - `DelegatingCommandPort`
  - `DelegatingScoreboardPort`
  - `DelegatingHologramPort`
  - `DelegatingNpcPort`
  - `BukkitDependencyProbe`
- Estensione capability model:
  - `StandardCapabilities.COMMAND`
- Nuovi moduli adapter wave 1:
  - `adapter-commandapi`
  - `adapter-fastboard`
  - `adapter-fancyholograms`
  - `adapter-fancynpcs`
- Nuovi adapter/component:
  - `CommandApiCommandPort`, `CommandApiAdapterComponent`
  - `FastBoardScoreboardPort`, `FastBoardAdapterComponent`
  - `FancyHologramsPort`, `FancyHologramsAdapterComponent`
  - `FancyNpcsPort`, `FancyNpcsAdapterComponent`
- Nuovi test wave 1:
  - core: `PortBindingServiceTest`, `AdapterCapabilityTransitionTest`, `AdapterFallbackNoopTransparencyTest`, `AdapterStartupOrderingTest`, `BukkitDependencyProbeTest`
  - adapter modules: `CommandApiAdapterComponentTest`, `CommandApiPortRegistrationTest`, `FastBoardScoreboardPortTest`, `FancyHologramsPortTest`, `FancyNpcsPortTest`
  - smoke matrix: `AdapterWave1NoDependencySmokeTest`, `AdapterWave1WithDependencySmokeTest`
- Nuove guide/documenti:
  - `docs/guides/ADAPTER-WAVE1-SETUP.md`
  - `docs/guides/ADAPTER-COMMANDAPI.md`
  - `docs/guides/ADAPTER-FASTBOARD.md`
  - `docs/guides/ADAPTER-FANCYHOLOGRAMS.md`
  - `docs/guides/ADAPTER-FANCYNPCS.md`
  - `docs/releases/0.8.0.md`

### Changed
- `DefaultCommonRuntime` ora registra delegating ports + `PortBindingService` al posto dei no-op diretti per `CommandPort`, `ScoreboardPort`, `HologramPort`, `NpcPort`.
- `verifyCoreDependencyPolicy` estesa a `api/adapter`.
- Nuova `verifyAdapterDependencyPolicy` per bloccare dipendenze cross-adapter.
- Nuovo source set/task opt-in `adapterIntegrationTest` (`-PrunAdapterIntegration=true`).

## [0.7.0] - 2026-03-04
### Added
- Nuovo package `api.arena` con foundation arena:
  - `ArenaInstance`, `ArenaOpenRequest`, `ArenaStatus`
  - `ArenaResetStrategy`, `ArenaResetContext`, `ArenaResetResult`
  - `ArenaService`
- Nuovo package `api.team`:
  - `TeamService`, `TeamDefinition`, `TeamSnapshot`, `FriendlyFirePolicy`
  - `PartyService`, `PartySnapshot`, `PartyActionResult`, `PartyStatus`
- Nuovo package `api.persistence`:
  - `PersistenceRecord`, `PersistenceWriteResult`
  - `YamlPersistencePort` + implementazione `DefaultYamlPersistencePort`
  - `SqlPersistencePort` + implementazione no-op `NoopSqlPersistencePort`
  - `SchemaMigration`, `SchemaMigrationContext`, `SchemaMigrationService`
- Nuove implementazioni/runtime helpers:
  - `DefaultArenaService`, `NoopArenaResetStrategy`, `PortBackedArenaResetStrategy`
  - `DefaultTeamService`, `DefaultPartyService`
  - `MatchFoundationHooks` per cleanup roster su match end
- Nuovi no-op ufficiali:
  - `NoopArenaResetPort`
- Nuove capability:
  - `ARENA_RESET`
  - `PERSISTENCE_YAML`
  - `PERSISTENCE_SQL`
  - `TEAMS`
  - `PARTIES`
- Nuove guide:
  - `docs/guides/ARENA-RESET-STRATEGIES.md`
  - `docs/guides/TEAM-PARTY-SERVICE.md`
  - `docs/guides/PERSISTENCE-PORTS.md`
  - `docs/guides/SCHEMA-MIGRATIONS.md`
  - `docs/guides/ARENA-RESET-BENCHMARK-HARNESS.md`
- Release notes `0.7.0`: `docs/releases/0.7.0.md`.

### Changed
- Runtime wiring esteso in `DefaultCommonRuntime` con registrazione default di:
  - `ArenaService`
  - `TeamService`
  - `PartyService`
  - `YamlPersistencePort`
  - `SqlPersistencePort`
  - `SchemaMigrationService`
- Policy build `verifyCoreDependencyPolicy` estesa ai package:
  - `api/arena`
  - `api/team`
  - `api/persistence`
- Introdotto source set/task `integrationTest` opt-in (`-PrunIntegrationHarness=true`) per harness integration arena reset.

## [0.6.0] - 2026-03-04
### Added
- Nuovo package `api.match` con match/state engine plugin-generic:
  - lifecycle model: `MatchState`, `MatchSessionStatus`, `EndReason`
  - policy model: `MatchTimingPolicy`, `RejoinPolicy`, `MatchPolicy`
  - session model: `MatchOpenRequest`, `MatchSession`
  - operation results: `MatchTransitionResult`, `JoinResult`, `DisconnectResult`, `RejoinResult`
  - hooks/contracts: `MatchCallbacks`, `MatchCleanup`, `MatchEngineService`
- Nuovo servizio runtime `DefaultMatchEngineService` con:
  - loop deterministico single-engine
  - startup lazy e idle shutdown automatico
  - transizioni timer-driven (`COUNTDOWN -> RUNNING`, `RUNNING -> ENDING`, `ENDING -> RESET`)
  - cleanup terminale idempotente
- Bridge built-in player lifecycle:
  - `PlayerQuitEvent` -> `MatchEngineService#onPlayerQuit`
  - `PlayerChangedWorldEvent` -> `MatchEngineService#onPlayerWorldChange`
- Capability model esteso:
  - `StandardCapabilities.MATCH_ENGINE` (`available(\"core-default\")`)
- Guide nuove:
  - `docs/guides/MATCH-STATE-ENGINE.md`
  - `docs/guides/MATCH-REJOIN-TIMEOUT-POLICY.md`
- Release notes `0.6.0`: `docs/releases/0.6.0.md`.

### Changed
- Runtime wiring esteso con registrazione default `MatchEngineService`.
- Runtime disable cleanup esteso con `matchEngine.closeAll(PLUGIN_DISABLE)` prima di `scheduler.cancelAll()`.
- Policy build `verifyCoreDependencyPolicy` estesa al package `api/match`.

## [0.5.0] - 2026-03-04
### Added
- Nuovo package `api.hud` con primitive HUD plugin-generic:
  - `ScoreboardSessionService`, `ScoreboardSession`, `ScoreboardSnapshot`, `HudUpdatePolicy`
  - `BossBarService`, `BossBarSession`, `BossBarState`
  - enum/shared models (`HudAudienceCloseReason`, `HudBarColor`, `HudBarStyle`, ...)
- Nuova porta `BossBarPort`.
- Nuovi no-op ufficiali:
  - `NoopScoreboardPort`
  - `NoopBossBarPort`
- Nuovi servizi runtime:
  - `DefaultScoreboardSessionService`
  - `DefaultBossBarService`
- Capability model esteso:
  - `StandardCapabilities.SCOREBOARD`
  - `StandardCapabilities.BOSSBAR`
- Guide nuove:
  - `docs/guides/HUD-SCOREBOARD-SESSIONS.md`
  - `docs/guides/BOSSBAR-SERVICE.md`
  - `docs/guides/HUD-PERFORMANCE-TARGETS.md`
- Release notes `0.5.0`: `docs/releases/0.5.0.md`.

### Changed
- Breaking: `ScoreboardPort` ridisegnata in modello session-oriented (`open/render/close`).
- Runtime disable cleanup esteso: chiusura HUD services (`ScoreboardSessionService`, `BossBarService`) con reason `PLUGIN_DISABLE`.
- Policy build `verifyCoreDependencyPolicy` estesa al package `api/hud`.

## [0.4.0] - 2026-03-03
### Added
- Nuovo package `api.gui` con modello session-oriented completo:
  - `GuiSessionService`, `GuiSession`, `GuiOpenRequest`, `GuiState`
  - `GuiEvent` (`GuiClickEvent`, `GuiCloseEvent`, `GuiTimeoutEvent`, `GuiDisconnectEvent`)
  - enum di supporto (`GuiCloseReason`, `GuiSessionStatus`, `ClickAction`, `GuiUpdateResult`, `GuiEventResult`)
- Nuovo servizio runtime `DefaultGuiSessionService` con gestione timeout/race-condition.
- Bridge policy `GuiPolicyRoutedEvent` per integrare eventi GUI portabili con `EventRouter`.
- Nuovo no-op ufficiale `NoopGuiPort`.
- Capability model esteso: `StandardCapabilities.GUI`.
- Guide nuove:
  - `docs/guides/GUI-SESSION-MODEL.md`
  - `docs/guides/GUI-ADAPTER-MAPPING.md`
- Release notes `0.4.0`: `docs/releases/0.4.0.md`.

### Changed
- Breaking: `GuiPort` ridisegnata (`open(GuiSession)`, `render(UUID, GuiState)`, `close(UUID, GuiCloseReason)`, `supportsPortableEvents()`).
- Runtime wiring aggiornato con registrazione default di `GuiSessionService` e `GuiPort`.

## [0.3.0] - 2026-03-03
### Added
- Nuovo command model backend-agnostic in `api.command` (`CommandModel`, `CommandNode`, `CommandExecution`, `CommandContext`, `CommandValidator`, `CommandResult`, `CommandRegistry`).
- Nuovo blocco i18n advanced in `api.message` (`MessageRequest`, `PlaceholderResolver`, `FallbackChain`, `PluralRules`).
- Nuove implementazioni runtime: `DefaultCommandRegistry`, `DefaultCommandValidator`, `AdvancedMiniMessageService`, `DefaultFallbackChain`, `DefaultPluralRules`.
- Guide dedicate:
  - `docs/guides/COMMAND-MODEL.md`
  - `docs/guides/I18N-ADVANCED.md`
  - `docs/guides/MIGRATION-BUKKIT-RAW-COMMANDS.md`
- Release notes `0.3.0`: `docs/releases/0.3.0.md`.

### Changed
- `CommandPort` redesign (breaking): ora usa `register(CommandModel)`, `unregister(String)`, `supportsSuggestions()`.
- `MessageService` redesign (breaking): nuovo entrypoint `render(MessageRequest)` con fallback locale multiplo e pluralizzazione base.
- Runtime wiring aggiornato con registrazione default command/i18n services.

## [0.2.0] - 2026-03-03
### Added
- Nuove porte plugin-generic ricche: `NpcPort`, `HologramPort`, `ClaimsPort`, `SchematicPort`.
- Nuovo modello capability tipizzato: `CapabilityKey`, `CapabilityStatus`, `CapabilityRegistry`, `StandardCapabilities`.
- Implementazioni no-op ufficiali per le nuove porte (`api.port.noop`).
- Nuova policy licenze adapter: `docs/policy/ADAPTER-LICENSE-POLICY.md`.
- Release notes `0.2.0`: `docs/releases/0.2.0.md`.

### Changed
- Runtime wiring esteso: registrazione default no-op ports + capability unavailable (`No adapter installed`).
- Backlog adapter aggiornato con priorità e criteri di adozione.
- Checklist `v0.2.0` completata con evidenze test/build.

## [0.1.0] - 2026-03-03
### Added
- Validazione consumer reale in-repo con modulo standalone `examples/consumer-demo`.
- Release notes GA `docs/releases/0.1.0.md`.

### Changed
- Checklist GA `v0.1.0` completata con evidenze e gate finali.
- README/compatibility matrix/release checklist riallineati allo stato stabile.

## [0.1.0-rc.1] - 2026-03-03
### Added
- Freeze API `0.1.x` documentato (`API-FREEZE-0.1.0-rc.1`) con contract test dedicato.
- Review formale thread-safety/lifecycle con mappa failure mode e limiti noti.
- Smoke test RC su `RuntimeBootstrap` in ambiente MockBukkit.
- Release notes candidate con limiti espliciti della versione.

### Changed
- Introdotta policy build `verifyCoreDependencyPolicy` per bloccare dipendenze adapter esterne nel core.
- Riallineata documentazione di governance (`README`, ADR, matrix, release checklist, checklist RC).

## [0.1.10-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-10): criteri DX verificabili per nuovi consumer documentati in README e checklist.

## [0.1.9-SNAPSHOT] - Unreleased
### Changed
- alpha3(issue-9): verificata build completa obbligatoria (`clean test javadoc build`).

## [0.1.8-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-8): test di backward compatibility minima per API deprecate (`PluginLifecycle`, `Tasks`).

## [0.1.7-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-7): contract tests per API pubbliche (`CommonRuntime`, `CommonScheduler`, `ServiceRegistry`).

## [0.1.6-SNAPSHOT] - Unreleased
### Changed
- alpha3(issue-6): pattern host lifecycle documentato con `RuntimeBootstrap`.

## [0.1.5-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-5): checklist migrazione per plugin consumer legacy.

## [0.1.4-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-4): policy formale naming/package stability (`api` vs `internal`).

## [0.1.3-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-3): helper plugin-generic `RuntimeBootstrap` e wrapper `OperationResult`/`OperationError`.

## [0.1.2-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-2): standard logging runtime con `RuntimeLogger` e `DefaultRuntimeLogger`.

## [0.1.1-SNAPSHOT] - Unreleased
### Added
- alpha3(issue-1): linee guida production-ready per `CommonComponent`.

## [0.1.0-alpha.3-SNAPSHOT] - Unreleased
### Changed
- Opened next development cycle after tagging `v0.1.0-alpha.2`.

## [0.1.0-alpha.2] - 2026-03-03
### Added
- Checklist roadmap versionata `0.x -> 1.0` in `docs/checklist/`.
- Checklist `v0.1.0-alpha.2` completata con evidenze file/test/comandi.

### Changed
- Consolidata baseline API `0.1.x` (`dev.patric.commonlib.api`).
- Confermata compatibilità legacy con API deprecate (`PluginLifecycle`, `Tasks`).
- Verifica release completa eseguita con `clean test javadoc build`.

## [0.1.0-alpha.1] - 2026-03-03
### Added
- Runtime kernel con builder composizionale (`CommonRuntime`).
- Lifecycle orchestration con rollback fail-fast.
- Service registry type-safe con duplicate guard.
- Scheduler facade (`CommonScheduler`) con cancellazione scope runtime.
- Event router base con policy hooks (`PolicyDecision`, `PolicyHook`).
- Config service YAML (`ConfigService`) con reload e path safety.
- Message service MiniMessage (`MessageService`) con fallback locale.
- Future ports placeholders: command/gui/scoreboard/arena reset.
- Test suite estesa (unit + MockBukkit plugin-like tests).
- ADR iniziali (001/002/003), compatibility matrix e cookbook.

### Changed
- `Tasks` spostata verso uso `CommonScheduler` (deprecazione graduale).
- `PluginLifecycle` marcata come legacy.
- README riallineato a boundary policy e quickstart runtime.
