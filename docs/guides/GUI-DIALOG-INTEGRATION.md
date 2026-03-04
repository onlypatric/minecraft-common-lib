# GUI + Dialog Integration

Con `OpenDialogAction` puoi aprire un template dialog da uno slot GUI e applicare le risposte nello stato della sessione.

```java
GuiDsl.chest("menu.profile", 6)
        .dialogInputSlot(
                20,
                GuiItemView.of("NAME_TAG", "<yellow>Rinomina"),
                "dialog.profile.edit",
                List.of(new DialogResponseBinding("name", "profile.name", true))
        )
        .build();
```

Il runtime:
1. recupera il template dal `DialogTemplateRegistry`;
2. costruisce placeholders da GUI state/open options;
3. apre la dialog via `DialogService`;
4. su submit applica i binding `responseKey -> stateKey`;
5. aggiorna revision e re-render della GUI.

Il comportamento è identico sia con backend InvUI attivo sia in fallback no-op (nessun crash plugin in failure path).

Se il template non esiste o il servizio non è disponibile: ritorna `GuiInteractionResult.INVALID_ACTION` senza crash.

Se una response `required` non è presente, il submit non fa crashare il plugin: viene loggato warning e lo stato GUI non viene aggiornato.
