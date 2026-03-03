# Migration Checklist for Legacy Consumers

Questa checklist guida la migrazione di plugin legacy verso `minecraft-common-lib`.

## Prerequisiti
- [ ] Java 21 abilitato nel plugin consumer.
- [ ] Paper target allineato a `1.21.11` o compatibile `1.21.x`.
- [ ] Strategia embed-first (shading) configurata.

## Step migrazione lifecycle
- [ ] Sostituire bootstrap custom con `CommonRuntime`.
- [ ] Spostare init da `onEnable` a `onLoad` quando necessario.
- [ ] Delegare shutdown a `runtime.onDisable()`.

## Step migrazione scheduler
- [ ] Reindirizzare task Bukkit raw a `CommonScheduler`.
- [ ] Aggiungere guard `requirePrimaryThread` su accessi Bukkit sensibili.
- [ ] Rimuovere task non tracciati fuori runtime scheduler.

## Step migrazione config/messages
- [ ] Spostare lettura config su `ConfigService`.
- [ ] Spostare rendering messaggi su `MessageService`.
- [ ] Definire fallback locale/chiavi mancanti.

## Compatibilità legacy minima
- [ ] Se presente codice legacy, mantenere ponte temporaneo su API deprecate.
- [ ] Pianificare rimozione graduale di `PluginLifecycle` e `Tasks` nel consumer.

## Verifica finale
- [ ] `./gradlew clean test javadoc build` verde nel plugin consumer.
- [ ] Smoke test join/quit/reload/shutdown senza errori runtime.
