# Changelog

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
