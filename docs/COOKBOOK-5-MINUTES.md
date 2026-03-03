# Cookbook: integrazione in 5 minuti

## 1. Costruisci la libreria
```bash
./gradlew clean build
```

## 2. Aggiungi al plugin consumer (embed-first)
- aggiungi dipendenza al jar della libreria
- shada/reloca nel plugin consumer

## 3. Bootstrap runtime nel tuo plugin
```java
public final class MyPlugin extends JavaPlugin {
    private CommonRuntime runtime;

    @Override
    public void onLoad() {
        runtime = CommonLib.runtime(this)
                .component(new MyComponent())
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

## 4. Usa i servizi core
```java
CommonScheduler scheduler = runtime.services().require(CommonScheduler.class);
ConfigService configs = runtime.services().require(ConfigService.class);
MessageService messages = runtime.services().require(MessageService.class);
```

## 5. Thread safety
- usa `CommonScheduler.requirePrimaryThread("operation")` prima di logica Bukkit sensibile.
