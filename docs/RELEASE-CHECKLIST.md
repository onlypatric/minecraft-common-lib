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

## Stable Completed Checks (`0.3.0`)
- [x] Command model backend-agnostic completo in `api.command`
- [x] Message service advanced con `MessageRequest` + resolver/fallback/plural rules
- [x] Breaking changes documentate (`CHANGELOG.md`, `docs/releases/0.3.0.md`)
- [x] Guide aggiornate (`COMMAND-MODEL`, `I18N-ADVANCED`, migrazione Bukkit raw commands)
- [x] Checklist `docs/checklist/06-v0.3.0.md` chiusa con evidenze

## RC Completed Checks (`1.0.0-rc.1`)
- [x] Freeze API `1.0.0` pubblicato (`docs/api/API-FREEZE-1.0.0.md`)
- [x] Legacy APIs rimosse prima del freeze (`PluginLifecycle`, `Tasks`, `MiniMessageService`)
- [x] Javadoc quality gate fail-fast attivo (`-Werror`)
- [x] Gate regressione completi verdi (`clean test javadoc build`, `check`, integration/matrix tasks)
- [x] Runbook smoke reale pubblicato (`docs/releases/1.0.0-rc.1.md`)

## GA Completed Checks (`1.0.0`)
- [x] Versione stabile allineata (`build.gradle.kts`, `CommonLib.version()`)
- [x] CHANGELOG aggiornato con sezione GA (`1.0.0`)
- [x] Compatibility policy ufficiale confermata (`Paper 1.21.x`, `Java 21`)
- [x] Checklist `docs/checklist/13-v1.0.0.md` chiusa con evidenze
- [x] Tag stabile creato (`v1.0.0`)
- [x] Smoke reale condiviso documentato (`docs/guides/SERVER-TEST-RUNBOOK.md`, cartella `SERVER-TEST`)

## Stable Completed Checks (`2.1.0`)
- [x] Module System pubblico aggiunto (`api.module`)
- [x] Runtime soft-disable orchestration attiva (`ModuleLifecycleOrchestrator`)
- [x] Compatibilita' componenti legacy preservata (`components(...)` invariato)
- [x] Testing ground interno disponibile (`examples/module-playground`)
- [x] SERVER-TEST esteso con report moduli (`CLIB_SMOKE_MODULES`, `modules.csv`)
