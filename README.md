# minecraft-common-lib

Libreria comune per ridurre boilerplate Bukkit/Paper nei plugin Minecraft del workspace.

## Baseline
- Paper API: `1.21.11`
- Java: `21`
- Modello distribuzione: embed-first (shading nei plugin consumer)

## Boundary policy
- API pubblica principale: `dev.patric.commonlib.api`
- Package `dev.patric.commonlib.internal` non stabili e non contrattuali
- NMS non incluso nel core (`v0.1.x`)

## Componenti core disponibili
- Runtime composizionale: `CommonRuntime`
- Component lifecycle: `CommonComponent`
- Service registry: `ServiceRegistry`
- Scheduler facade: `CommonScheduler`
- Config service YAML: `ConfigService`
- Message service MiniMessage: `MessageService`
- Event router + policy hooks: `EventRouter`, `PolicyDecision`, `PolicyHook`

## Build e test
```bash
./gradlew clean test javadoc build
```

## Quickstart
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

## Documentazione
- [ADR-001](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-001-embed-first-no-nms-core.md)
- [ADR-002](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-002-api-boundaries.md)
- [ADR-003](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-003-versioning-changelog-policy.md)
- [Cookbook 5 minuti](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COOKBOOK-5-MINUTES.md)
- [Compatibility Matrix](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COMPATIBILITY-MATRIX.md)
- [Adapter Backlog](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/ADAPTER-BACKLOG.md)
- [Checklist Versioni 0.x -> 1.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/checklist/README.md)
- [Release Checklist](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/RELEASE-CHECKLIST.md)
- [Library Design](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/LIB-DESIGN.md)
- [External Libs Research](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/UTILS-EXTERNAL-LIBS-RESEARCH.md)
