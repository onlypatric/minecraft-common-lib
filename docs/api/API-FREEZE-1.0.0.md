# API Freeze `1.0.0`

- Date: 2026-03-04
- Scope: `dev.patric.commonlib.api` + `dev.patric.commonlib.api.port`
- Status: Frozen for `1.0.0-rc.1` and `1.0.0` GA

## Stable Public Surface (frozen)

`1.0.0` congela l'intera superficie pubblica nel package `api/*` (inclusi sottopackage) e `api/port/*`.

Regola di freeze metodo/firma:
- per ogni tipo elencato sotto, sono congelate tutte le firme pubbliche dichiarate al tag `v1.0.0`;
- modifiche breaking (rename, remove, type change) consentite solo in major successiva (`2.0.0+`).

### Type inventory frozen

```text
$(find src/main/java/dev/patric/commonlib/api -name '*.java' ! -name 'package-info.java' | sed 's#src/main/java/##; s#/#.#g; s#\.java$##' | sort)
```

## Explicit non-contractual areas (not frozen)

- `dev.patric.commonlib.runtime.*`
- `dev.patric.commonlib.internal.*`
- `dev.patric.commonlib.scheduler.*`
- `dev.patric.commonlib.services.*`
- `dev.patric.commonlib.lifecycle.*`
- `dev.patric.commonlib.config.*`
- `dev.patric.commonlib.message.*` (implementazioni concrete)
- `dev.patric.commonlib.runtime.adapter.*`
- Moduli adapter concreti: `adapter-*` (`CommandApiCommandPort`, `FastBoardScoreboardPort`, ecc.)

Nota: le interfacce pubbliche in `api.adapter` e `api.port` restano freeze, ma le classi concrete di backend no.

## Legacy removals before freeze

Rimosse prima di `1.0.0`:
- `dev.patric.commonlib.plugin.PluginLifecycle`
- `dev.patric.commonlib.scheduler.Tasks`
- `dev.patric.commonlib.message.MiniMessageService`

## Verification

- Contract freeze test: `src/test/java/dev/patric/commonlib/api/PublicApiFreezeContractTest.java`
- Core smoke/compat test:
  - `src/test/java/dev/patric/commonlib/CommonLibSmokeTest.java`
  - `src/test/java/dev/patric/commonlib/api/CommonRuntimeContractTest.java`
- Full release gate:
  - `./gradlew --no-daemon clean test javadoc build`
  - `./gradlew --no-daemon check`
