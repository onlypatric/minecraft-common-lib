# Cookbook: integrazione in 5 minuti

## 1. Costruisci la libreria
```bash
./gradlew clean build
```

## 2. Aggiungi al plugin consumer (embed-first)
- aggiungi dipendenza al jar della libreria
- shade/reloca nel plugin consumer

## 3. Bootstrap runtime nel tuo plugin
```java
public final class MyPlugin extends JavaPlugin {
    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        OperationResult<CommonRuntime> built = RuntimeBootstrap.build(this, builder ->
                builder.component(new MyComponent())
        );
        if (built.isFailure()) {
            throw new IllegalStateException(built.errorOrNull().message(), built.errorOrNull().cause());
        }
        runtime = built.valueOrNull();

        OperationResult<Void> loaded = RuntimeBootstrap.safeLoad(runtime);
        if (loaded.isFailure()) {
            throw new IllegalStateException(loaded.errorOrNull().message(), loaded.errorOrNull().cause());
        }
    }

    @Override
    public void onEnable() {
        OperationResult<Void> enabled = RuntimeBootstrap.safeEnable(runtime);
        if (enabled.isFailure()) {
            throw new IllegalStateException(enabled.errorOrNull().message(), enabled.errorOrNull().cause());
        }
    }

    @Override
    public void onDisable() {
        RuntimeBootstrap.safeDisable(runtime);
    }
}
```

## 4. Usa i servizi core
```java
CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
ConfigService configs = runtime.services().require(ConfigService.class);
MessageService messages = runtime.services().require(MessageService.class);
```

## 5. Thread safety
- usa `CommonScheduler.requirePrimaryThread("operation")` prima di logica Bukkit sensibile.

## 6. Breaking changes verso `1.0.0`
API legacy rimosse:
- `PluginLifecycle` -> usa `CommonComponent` + `CommonRuntime`
- `Tasks` -> usa `CommonScheduler` direttamente
- `MiniMessageService` -> usa `MessageService` (runtime default: `AdvancedMiniMessageService`)

Guida completa:
- `docs/guides/MIGRATION-0.x-TO-1.0.0.md`
