# I18N Advanced Guide

`v0.3.0` evolve `MessageService` con richieste strutturate, resolver estendibili e fallback locale multiplo.

## API principali
- `MessageRequest`: key, locale, placeholders, count, contextTags.
- `PlaceholderResolver`: resolver custom registrabili a runtime.
- `FallbackChain`: risoluzione locale (`it_IT -> it -> en -> default`).
- `PluralRules`: selezione forma (`one`, `other`).

## Service behavior
`AdvancedMiniMessageService` applica:
1. fallback chain locale;
2. risoluzione placeholders statici + resolver custom;
3. plural form key lookup: `<locale>.<key>.one|other`, fallback su `<locale>.<key>`.

## Esempio
```java
MessageService messages = services.require(MessageService.class);
messages.registerResolver(new PlaceholderResolver() {
    @Override
    public boolean supports(String placeholderKey) {
        return placeholderKey.equals("server_name");
    }

    @Override
    public String resolve(String placeholderKey, MessageRequest request) {
        return "MyServer";
    }
});

Component rendered = messages.render(new MessageRequest(
        "greeting",
        Locale.ITALIAN,
        Map.of("player", "Patric"),
        1,
        Map.of("channel", "chat")
));
```

## Compat helper
Resta disponibile l'overload:
- `render(String key, Locale locale)`
- `render(String key, Map<String, String> placeholders, Locale locale)`
