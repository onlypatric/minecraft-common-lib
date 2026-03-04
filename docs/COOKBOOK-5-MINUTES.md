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

## 5. Crea una GUI chest 9x6 in poche righe (GUI v2)
```java
GuiDefinitionRegistry menus = runtime.services().require(GuiDefinitionRegistry.class);

GuiDefinition menu = GuiDsl.chest("shop.main", 6)
        .title("<gold>Shop")
        .button(10, GuiItemView.of("STONE", "<green>Compra"), List.of(
                new RunCommandAction("shop buy stone", true)
        ))
        .dialogInputSlot(
                13,
                GuiItemView.of("NAME_TAG", "<yellow>Rinomina"),
                "shop.rename",
                List.of(new DialogResponseBinding("name", "shop.displayName", true))
        )
        .switchSlot(
                16,
                "shop.enabled",
                GuiItemView.of("LIME_DYE", "<green>ON"),
                GuiItemView.of("GRAY_DYE", "<red>OFF"),
                true
        )
        .subMenuSlot(31, GuiItemView.of("CHEST", "<aqua>Categorie"), "shop.categories")
        .backSlot(45, GuiItemView.of("ARROW", "<gray>Back"))
        .transferSlot(49, GuiItemView.of("CHEST", "<aqua>Deposita/Preleva"), SlotInteractionPolicy.TAKE_DEPOSIT)
        .build();

menus.register(menu);

GuiSessionService gui = runtime.services().require(GuiSessionService.class);
gui.open(menu, player.getUniqueId(), GuiOpenOptions.defaults());
```

Per usare backend GUI reale InvUI, registra anche `new InvUiAdapterComponent()` nei componenti runtime del plugin host.

## 6. Thread safety
- usa `CommonScheduler.requirePrimaryThread("operation")` prima di logica Bukkit sensibile.

## 7. Breaking changes verso `1.0.0`
API legacy rimosse:
- `PluginLifecycle` -> usa `CommonComponent` + `CommonRuntime`
- `Tasks` -> usa `CommonScheduler` direttamente
- `MiniMessageService` -> usa `MessageService` (runtime default: `AdvancedMiniMessageService`)

Guida completa:
- `docs/guides/MIGRATION-0.x-TO-1.0.0.md`
