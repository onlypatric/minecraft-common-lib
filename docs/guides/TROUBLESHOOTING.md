# Troubleshooting

Problemi comuni durante integrazione e runtime di `minecraft-common-lib`.

## 1) Capability unavailable per un adapter
Sintomo:
- `CapabilityRegistry` restituisce `unavailable(...)` per capability attesa.

Checklist:
1. Verifica plugin/backend installato sul server.
2. Verifica plugin enabled in startup.
3. Verifica versione minima backend richiesta.
4. Verifica ordine bootstrap componenti adapter.

Ragioni standard tipiche:
- `missing-plugin:<name>`
- `disabled-plugin:<name>`
- `incompatible-version:<name>:<installed><required>`
- `missing-class:<fqcn>`
- `binding-failed:<name>:<exception>`

## 2) Task orfani su disable
Sintomo:
- warning task pendenti in shutdown.

Checklist:
1. Usa sempre `CommonScheduler` invece del Bukkit scheduler raw.
2. Assicurati che runtime venga disabilitato in `onDisable()`.
3. Verifica chiusura servizi session-oriented (`GUI/HUD/Dialog/Match`) prima di `scheduler.cancelAll()`.

## 3) Dialog API non disponibile in ambiente test
Sintomo:
- backend dialog Paper non completo (Mock/test env).

Comportamento atteso:
- Dialog wrapper degrade-safe: niente crash host plugin.
- `StandardCapabilities.DIALOG` puo' passare a unavailable con reason `binding-failed:paper-dialog:<error>`.

## 4) Consumer demo non trova il jar
Sintomo:
- test consumer-demo falliscono per `commonLibJar` mancante.

Comando corretto:
```bash
./gradlew --no-daemon -p examples/consumer-demo clean test -PcommonLibJar=../../build/libs/minecraft-common-lib-<version>.jar
```

## 5) Errori policy dependency build
Sintomo:
- fallimento su `verifyCoreDependencyPolicy` o `verifyAdapterDependencyPolicy`.

Checklist:
1. Nessuna dependency esterna nel core root (`api/implementation/runtimeOnly`).
2. Adapter modules dipendono solo da `project(":")` lato progetto.
3. Moduli con vincoli licenza rispettano `verifyAdapterLicensePolicy`.

## 6) Compatibilita' runtime
Supporto ufficiale:
- Paper `1.21.x`
- Java `21`

Versioni Paper fuori `1.21.x` sono fuori scope ufficiale.
