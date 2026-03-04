# Arena Reset Benchmark Harness

`0.7.0` include due livelli di harness per reset arena.

## 1) Fake harness (sempre in test)
Test:
- `src/test/java/dev/patric/commonlib/arena/ArenaResetBenchmarkHarnessTest.java`

Obiettivo:
- validare contatori/report shape su batch reset;
- confermare pipeline deterministica e mapping risultati.

## 2) Integration harness (opt-in)
Test:
- `src/integrationTest/java/dev/patric/commonlib/arena/ArenaResetIntegrationHarnessTest.java`

Obiettivo:
- validare pipeline runtime reale + strategy `port-backed` con stub adapter.

## Esecuzione
Esegue solo se abilitato esplicitamente:
```bash
./gradlew --no-daemon integrationTest -PrunIntegrationHarness=true
```

Senza property, il task è skipped (nessun impatto su CI standard).

## KPI minimi
- reset richieste completate senza eccezioni runtime inattese;
- throttling per arena in-flight rispettato (`THROTTLED` su richieste concorrenti);
- report fake harness consistente con numero richieste.
