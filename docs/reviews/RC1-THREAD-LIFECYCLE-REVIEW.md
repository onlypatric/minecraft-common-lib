# RC1 Thread-Safety & Lifecycle Failure Modes Review

- Date: 2026-03-03
- Scope: `v0.1.0-rc.1` core runtime
- Reviewer: internal release checklist

## Failure mode matrix

| Failure mode | Coverage | Result | Known limit |
| --- | --- | --- | --- |
| Component throws during `onEnable` | `CommonRuntimeLifecycleTest#enableFailureRollsBackEnabledComponents` | PASS: previously enabled components are disabled in reverse order | Rollback is limited to components that successfully completed enable |
| Scheduled tasks survive plugin disable | `BukkitCommonSchedulerTest#repeatingTaskIsCancelledOnRuntimeDisable` | PASS: tracked tasks are cancelled on runtime disable | Cancellation is best-effort on scheduled handles only |
| Bukkit sync-only operation called from async thread | `BukkitCommonSchedulerTest#requirePrimaryThreadDetectsAsyncUsage` | PASS: `IllegalStateException` is thrown | Guardrail requires explicit `requirePrimaryThread` usage by caller code |
| Legacy API regression (`PluginLifecycle`, `Tasks`) | `LegacyApiCompatibilityTest` | PASS: deprecated contracts still callable and delegating | Legacy API remains deprecated and may be removed after `1.x` policy update |
| Event policy deny path not cancelling event | `SimpleEventRouterTest#denyDecisionCancelsCancellableEvent` | PASS: deny decision propagates and cancels cancellable event | Router does not auto-register Bukkit listeners; host plugin wiring still required |

## RC conclusions
- Thread-safety guardrails are working as designed for sync/async boundaries.
- Lifecycle orchestration behavior is deterministic and fail-fast with rollback.
- No blocker emerged from current test matrix for `v0.1.0-rc.1`.

## Explicit non-goals / constraints
- No NMS integration in core.
- No external adapter dependencies in core.
- No preemptive cancellation of already-running async work; cancellation applies to tracked scheduler tasks.
