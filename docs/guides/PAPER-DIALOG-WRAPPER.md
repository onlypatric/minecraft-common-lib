# Paper Dialog Wrapper

Guida rapida al wrapper `api.dialog` introdotto in `0.9.1`.

## Obiettivo
- Aprire dialog in modo uniforme tramite `DialogService`.
- Gestire submit/close/timeout con session tracking.
- Applicare policy hooks tramite `EventRouter`.
- Restare degradabile quando backend dialog non è disponibile.

## Servizi runtime
- `DialogTemplateRegistry`: registro template riusabili.
- `DialogService`: lifecycle sessioni (`open/find/close/publish`).

Entrambi sono registrati di default nel runtime.

## Quick usage
```java
DialogTemplateRegistry templates = runtime.services().require(DialogTemplateRegistry.class);
DialogService dialogs = runtime.services().require(DialogService.class);

DialogTemplate template = new DialogTemplate(
        "example.confirm",
        new DialogBaseSpec(
                Component.text("Confirm action"),
                null,
                true,
                false,
                DialogAfterAction.WAIT_FOR_RESPONSE,
                List.of(new PlainMessageBodySpec(Component.text("Proceed?"), 160)),
                List.of(new TextInputSpec("reason", 160, Component.text("Reason"), true, "", 64, null, null))
        ),
        new NoticeTypeSpec(
                new DialogButtonSpec(Component.text("Submit"), null, 120, new CustomActionSpec("submit", Map.of()))
        ),
        Map.of()
);

templates.register(template);

DialogSession session = dialogs.open(new DialogOpenRequest(
        player.getUniqueId(),
        template,
        200L,
        Locale.ENGLISH,
        Map.of("context", "example"),
        new DialogCallbacks() {
            @Override
            public void onSubmit(DialogSession s, DialogSubmission submission) {
                submission.response().text("reason").ifPresent(value -> {
                    // application logic
                });
            }
        }
));
```

## Eventi e policy
- Gli eventi `DialogSubmitEvent`, `DialogCloseEvent`, `DialogTimeoutEvent` passano da `EventRouter`.
- Un `PolicyHook` che nega l’evento produce `DialogEventResult.DENIED_BY_POLICY`.
- In caso deny su submit, la sessione resta aperta.

## Cleanup
- `closeAllByPlayer(..., QUIT)` viene eseguito dal bridge player lifecycle su quit.
- `closeAll(..., PLUGIN_DISABLE)` viene eseguito in `DefaultCommonRuntime.onDisable()`.

## Note operative
- `StandardCapabilities.DIALOG` parte come `available("core-default")`.
- Se backend Paper dialog fallisce a runtime, viene pubblicato `unavailable("binding-failed:paper-dialog:<error>")`.
- Il servizio mantiene comunque behavior deterministic-safe e non lancia eccezioni non gestite sul plugin host.
