# minecraft-common-lib

Libreria comune per ridurre il boilerplate Bukkit/Paper nei plugin Minecraft del workspace.

## Requisiti

- Java 21
- Gradle Wrapper (incluso nel progetto)

## Build e test

```bash
./gradlew build
./gradlew test
```

## Obiettivo

Fornire utility e astrazioni riusabili per:

- lifecycle plugin
- scheduling task Bukkit/Paper
- helper comuni orientati a plugin Paper 1.21.11

## Roadmap breve

- Modulo config helper (YAML / typed access)
- Modulo command utilities
- Modulo inventory/UI helpers
