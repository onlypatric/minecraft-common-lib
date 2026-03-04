# GUI Fluent DSL (v2)

## Obiettivo
Definire GUI chest in poche righe, con policy slot esplicite.

## Esempio `9x6` completo (button + dialog input + switch + submenu)
```java
GuiDefinition shop = GuiDsl.chest("menu.shop", 6)
        .title("<gold>Shop")
        .button(10, GuiItemView.of("DIAMOND", "<green>Compra"), List.of(
                new RunCommandAction("shop buy", false)
        ))
        .dialogInputSlot(
                13,
                GuiItemView.of("PAPER", "<yellow>Rinomina"),
                "dialog.shop.rename",
                List.of(new DialogResponseBinding("name", "shop.name", true))
        )
        .switchSlot(
                16,
                "shop.enabled",
                GuiItemView.of("LIME_DYE", "<green>Abilitato"),
                GuiItemView.of("GRAY_DYE", "<red>Disabilitato"),
                true
        )
        .subMenuSlot(31, GuiItemView.of("CHEST", "<aqua>Categorie"), "menu.shop.categories")
        .backSlot(45, GuiItemView.of("ARROW", "<gray>Indietro"))
        .transferSlot(49, GuiItemView.of("BARREL", "<gold>Deposita/Preleva"), SlotInteractionPolicy.TAKE_DEPOSIT)
        .build();
```

## Apertura sessione
```java
GuiSession session = guiService.open(
        shop,
        player.getUniqueId(),
        GuiOpenOptions.defaults()
);
```

Con `adapter-invui` attivo, questa apertura viene renderizzata dal backend InvUI reale; senza adapter resta attivo il fallback no-op in modo safe.

## Prerequisito submenu
Per usare `subMenuSlot(...)`, registra prima le definizioni nel runtime:

```java
GuiDefinitionRegistry menus = runtime.services().require(GuiDefinitionRegistry.class);
menus.register(shop);
menus.register(categories);
```
