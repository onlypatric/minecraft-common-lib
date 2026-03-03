# API Freeze `0.1.0-rc.1`

- Date: 2026-03-03
- Scope: `v0.1.x` core runtime (embed-first, no adapters in core)
- Status: Frozen for RC validation

## Public API Surface Frozen

### Core contracts
- `dev.patric.commonlib.api.CommonRuntime`
- `dev.patric.commonlib.api.CommonComponent`
- `dev.patric.commonlib.api.CommonContext`
- `dev.patric.commonlib.api.ServiceRegistry`
- `dev.patric.commonlib.api.CommonScheduler`
- `dev.patric.commonlib.api.TaskHandle`

### Core services
- `dev.patric.commonlib.api.ConfigService`
- `dev.patric.commonlib.api.MessageService`
- `dev.patric.commonlib.api.EventRouter`
- `dev.patric.commonlib.api.RuntimeLogger`

### Bootstrap and error wrappers
- `dev.patric.commonlib.api.bootstrap.RuntimeBootstrap`
- `dev.patric.commonlib.api.error.OperationResult`
- `dev.patric.commonlib.api.error.OperationError`
- `dev.patric.commonlib.api.error.ErrorCodes`

### Future ports (interfaces only)
- `dev.patric.commonlib.api.port.CommandPort`
- `dev.patric.commonlib.api.port.GuiPort`
- `dev.patric.commonlib.api.port.ScoreboardPort`
- `dev.patric.commonlib.api.port.ArenaResetPort`

## Compatibility Notes
- Deprecated legacy API remains supported in `v0.1.x`:
  - `dev.patric.commonlib.plugin.PluginLifecycle`
  - `dev.patric.commonlib.scheduler.Tasks`
- No direct NMS support in core.
- No external plugin adapters in core for RC.

## Verification
- Contract test: `PublicApiFreezeContractTest`
- Full gate command: `./gradlew --no-daemon clean test javadoc build`
