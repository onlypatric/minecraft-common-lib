# Minimal Plugin Example

Questo esempio mostra il wiring minimo di `CommonRuntime` dentro un plugin Paper.

```java
public final class ExamplePlugin extends JavaPlugin {
    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        runtime = CommonLib.runtime(this)
                .component(new ExampleComponent())
                .build();
        runtime.onLoad();
    }

    @Override
    public void onEnable() {
        runtime.onEnable();
    }

    @Override
    public void onDisable() {
        runtime.onDisable();
    }
}
```
