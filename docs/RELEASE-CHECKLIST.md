# Release Readiness Checklist

## RC Completed Checks (`0.1.0-rc.1`)
- [x] Freeze API documentato (`docs/api/API-FREEZE-0.1.0-rc.1.md`)
- [x] Policy dipendenze core verificata (`verifyCoreDependencyPolicy`)
- [x] Review thread-safety/lifecycle formalizzata (`docs/reviews/RC1-THREAD-LIFECYCLE-REVIEW.md`)
- [x] Smoke test runtime bootstrap disponibile (`RcRuntimeBootstrapSmokeTest`)
- [x] Release notes RC con limiti espliciti (`docs/releases/0.1.0-rc.1.md`)

## GA Completed Checks (`0.1.0`)
- [x] `./gradlew --no-daemon clean test javadoc build` verde su commit finale GA
- [x] CHANGELOG aggiornato con sezione release stabile
- [x] Tag stabile creato (`v0.1.0`)
- [x] Validazione jar in consumer demo reale (`examples/consumer-demo`)
- [x] Checklist `docs/checklist/04-v0.1.0.md` chiusa
