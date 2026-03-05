# Module Playground

Esempio minimale per provare il Module System (`v2.1.0`) senza toccare plugin consumer esterni.

## Cosa dimostra
- modulo `core-services`
- modulo `economy-hooks` dipendente da `core-services`
- modulo `failing-demo` abilitabile da config per simulare `FAILED_ENABLE`
- comando `/clibmodule dump` per dump stato moduli

## Build
Dalla root `minecraft-common-lib`:

```bash
./gradlew --no-daemon clean build
./gradlew --no-daemon -p examples/module-playground clean build -PcommonLibJar=../../build/libs/minecraft-common-lib-2.1.0.jar
```

## Esecuzione
- copia il jar prodotto in un server Paper `1.21.x`
- lancia `/clibmodule dump`
- verifica nel log le righe `CLIB_PLAYGROUND_MODULES {...}`

## Failure demo
Imposta in `config.yml`:

```yaml
failingDemo:
  enabled: true
```

Con questa opzione, il modulo `failing-demo` va in `FAILED_ENABLE` ma il plugin continua a funzionare (policy soft-disable).
