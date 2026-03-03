# CommonComponent Production Guidelines

Questa guida definisce lo standard minimo per implementare componenti runtime robusti usando `CommonComponent`.

## Principi
- Ogni componente deve avere `id()` stabile, umano e univoco.
- `onLoad` deve essere idempotente e veloce (no I/O bloccante pesante).
- `onEnable` deve fallire in modo esplicito (`RuntimeException`) quando i prerequisiti mancano.
- `onDisable` deve essere best-effort e non rilanciare eccezioni non gestite.

## Checklist implementativa
- Validare dipendenze usando `context.services().require(...)` in `onLoad` o `onEnable`.
- Usare `context.scheduler().requirePrimaryThread(...)` prima di accessi Bukkit sensibili.
- Registrare task soltanto tramite `CommonScheduler` per cleanup automatico su shutdown.
- Evitare stato globale statico; preferire stato confinato nel componente.
- Loggare eventi lifecycle con prefisso runtime standard.

## Pattern consigliato
```java
public final class ExampleComponent implements CommonComponent {

    @Override
    public String id() {
        return "example";
    }

    @Override
    public void onLoad(CommonContext context) {
        context.services().require(ConfigService.class);
    }

    @Override
    public void onEnable(CommonContext context) {
        context.scheduler().requirePrimaryThread("example-enable");
    }

    @Override
    public void onDisable(CommonContext context) {
        // best-effort cleanup
    }
}
```

## Anti-pattern da evitare
- Accessi Bukkit in thread async senza guard.
- Catch silenziosi che nascondono failure in `onEnable`.
- Task non tracciati fuori da `CommonScheduler`.
- `id()` dinamico o dipendente da configurazione runtime.
