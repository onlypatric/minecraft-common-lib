# minecraft-common-lib

Libreria comune per ridurre boilerplate Bukkit/Paper nei plugin Minecraft del workspace.

## Baseline
- Paper API: `1.21.11`
- Java: `21`
- Modello distribuzione: embed-first (shading nei plugin consumer)

## Stable status (`0.1.0`)
- Release stabile core disponibile come `v0.1.0`.
- Public API `0.1.x` congelata in [`docs/api/API-FREEZE-0.1.0-rc.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/api/API-FREEZE-0.1.0-rc.1.md).
- Scope core bloccato: nessun adapter/plugin esterno nel dependency set core.
- Release notes:
  - RC: [`docs/releases/0.1.0-rc.1.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0-rc.1.md)
  - Stable: [`docs/releases/0.1.0.md`](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0.md)

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
        var built = RuntimeBootstrap.build(this, builder ->
                builder.component(new MyComponent())
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

## How To Verify As New Consumer
1. Add the library to your plugin project and shade it in your final jar.
2. Create a minimal plugin using the quickstart pattern with `RuntimeBootstrap`.
3. Start a Paper `1.21.11` test server and confirm no startup exceptions.
4. Trigger one command/event that touches at least one registered `CommonComponent`.
5. Stop the server and confirm clean disable with no task warnings.
6. Run local quality gates:
   - `./gradlew --no-daemon test`
   - `./gradlew --no-daemon clean test javadoc build`
7. Run the in-repo consumer validation project:
   - `./gradlew --no-daemon -p examples/consumer-demo clean test -PcommonLibJar=../../build/libs/minecraft-common-lib-0.1.0.jar`

## Documentazione
- [ADR-001](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-001-embed-first-no-nms-core.md)
- [ADR-002](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-002-api-boundaries.md)
- [ADR-003](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/adr/ADR-003-versioning-changelog-policy.md)
- [Cookbook 5 minuti](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COOKBOOK-5-MINUTES.md)
- [Compatibility Matrix](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/COMPATIBILITY-MATRIX.md)
- [Adapter Backlog](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/ADAPTER-BACKLOG.md)
- [Checklist Versioni 0.x -> 1.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/checklist/README.md)
- [Release Checklist](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/RELEASE-CHECKLIST.md)
- [Release Notes 0.1.0-rc.1](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0-rc.1.md)
- [Release Notes 0.1.0](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/releases/0.1.0.md)
- [Library Design](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/LIB-DESIGN.md)
- [External Libs Research](/Users/patric/Documents/Minecraft/minecraft-common-lib/docs/UTILS-EXTERNAL-LIBS-RESEARCH.md)
