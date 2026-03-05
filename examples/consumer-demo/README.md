# Consumer Demo (In-Repo)

Questo progetto verifica che il jar di `minecraft-common-lib` funzioni in un plugin consumer reale.

## Prerequisiti
- Java 21
- Jar locale della libreria common già buildato

## Comandi
Dalla root di `minecraft-common-lib`:

```bash
./gradlew --no-daemon clean build
./gradlew --no-daemon -p examples/consumer-demo clean test -PcommonLibJar=../../build/libs/minecraft-common-lib-2.1.0.jar
```

Il test carica il plugin demo con MockBukkit, verifica bootstrap runtime e conferma che i task vengano cancellati allo shutdown.

Il jar prodotto dal demo include (`embed`) la common-lib locale passata via `-PcommonLibJar`, coerente con il modello embed-first usato in produzione.
