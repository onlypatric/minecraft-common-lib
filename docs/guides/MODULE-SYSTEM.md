# Module System Guide

Il Module System (`2.1.0`) permette di dichiarare blocchi lifecycle con dipendenze esplicite, evitando wiring manuale fragile.

## API
- `CommonModule`: modulo dichiarativo (`id`, `dependsOn`, lifecycle methods)
- `ModuleRegistry`: stato runtime dei moduli
- `ModuleStatus`: snapshot (`state`, `reason`, `dependsOn`)
- `ModuleState`: stati planner/lifecycle (`REGISTERED`, `ENABLED`, `FAILED_*`, `SKIPPED_*`, `DISABLED`)

## Builder usage
```java
CommonRuntime runtime = CommonRuntime.builder(plugin)
    .module(new CoreModule())
    .module(new EconomyModule())
    .enableModuleDiagnostics(true)
    .build();
```

## Policy
- Orchestrazione deterministica: topological ordering + tie-break lessicografico.
- Missing dependency: `SKIPPED_MISSING_DEPENDENCY`.
- Cycle detection: `SKIPPED_CYCLE`.
- Failure isolation: `FAILED_LOAD` / `FAILED_ENABLE` con propagation `SKIPPED_DEPENDENCY_INACTIVE` sui dipendenti.
- Soft-disable: failure modulo non interrompe il runtime globale.

## Runtime order con componenti legacy
- `onLoad`: moduli -> componenti
- `onEnable`: moduli -> componenti
- `onDisable`: componenti -> moduli

## Diagnostica
Con `enableModuleDiagnostics(true)` il runtime logga transizioni:
- `module:<id>:<state>:<reason>`

Per stato machine-readable:
```java
ModuleRegistry registry = runtime.services().require(ModuleRegistry.class);
registry.all().forEach(status -> ...);
```
