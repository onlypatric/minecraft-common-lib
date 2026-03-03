# Minimal Plugin Example

Questo esempio mostra il wiring minimo di `CommonRuntime` dentro un plugin Paper
usando il pattern canonicale con `RuntimeBootstrap`.

```java
public final class ExamplePlugin extends JavaPlugin {
    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        var built = RuntimeBootstrap.build(this, builder ->
                builder.component(new ExampleComponent())
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
