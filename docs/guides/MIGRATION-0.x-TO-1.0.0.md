# Migration Guide: `0.x` -> `1.0.0`

Questa guida copre i cambi necessari per migrare plugin consumer da versioni `0.x` a `1.0.0`.

## Target compatibility
- Paper: `1.21.x`
- Java: `21`
- Distribution model: embed-first (shading nel plugin consumer)

## Breaking changes introdotte pre-`1.0.0`

### Legacy API rimosse
Le seguenti API deprecate sono state rimosse prima del freeze `1.0.0`:
- `dev.patric.commonlib.plugin.PluginLifecycle`
- `dev.patric.commonlib.scheduler.Tasks`
- `dev.patric.commonlib.message.MiniMessageService`

### Replacement mapping
- `PluginLifecycle` -> `CommonComponent` + `CommonRuntime`
- `Tasks.runNextTick/runAsync` -> `CommonScheduler.runSync/runAsync`
- `MiniMessageService` -> `MessageService` (`AdvancedMiniMessageService` runtime default)

## Migration checklist
1. Aggiorna dipendenza libreria a `1.0.0`.
2. Rimuovi import legacy (`PluginLifecycle`, `Tasks`, `MiniMessageService`).
3. Migra bootstrap al pattern `RuntimeBootstrap`.
4. Usa `CommonScheduler` direttamente per scheduling.
5. Usa `MessageService` via `runtime.services().require(MessageService.class)`.
6. Esegui test locali:
- `./gradlew --no-daemon test`
- `./gradlew --no-daemon clean test javadoc build`

## Example: scheduler migration

Prima (`0.x` legacy):
```java
TaskHandle handle = Tasks.runNextTick(scheduler, () -> doWork());
```

Dopo (`1.0.0`):
```java
TaskHandle handle = scheduler.runSync(() -> doWork());
```

## Example: lifecycle migration

Prima (`0.x` legacy):
```java
public final class MyLifecycle implements PluginLifecycle {
    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
```

Dopo (`1.0.0`):
```java
public final class MyComponent implements CommonComponent {
    @Override
    public String id() {
        return "my-component";
    }

    @Override
    public void onEnable(CommonContext ctx) {
        // enable logic
    }

    @Override
    public void onDisable(CommonContext ctx) {
        // disable logic
    }
}
```

## API freeze note
La superficie pubblica contrattuale `1.0.0` e' definita in:
- `docs/api/API-FREEZE-1.0.0.md`

Fuori da questa superficie, la compatibilita' non e' garantita tra minor/patch.
