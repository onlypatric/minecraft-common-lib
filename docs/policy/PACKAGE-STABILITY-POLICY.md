# Package Stability Policy

Questa policy definisce stabilita', support lifecycle e regole di compatibilita' post-`1.0.0`.

## Livelli di stabilita'

### Stable API (contractual)
- `dev.patric.commonlib.api`
- `dev.patric.commonlib.api.port`
- `dev.patric.commonlib.api.module`

Regole:
- SemVer standard dopo `1.0.0`.
- Breaking changes solo in major (`2.0.0+`).
- Patch/minor non devono rompere firme o semantiche pubbliche documentate.

### Supported but non-contractual runtime
- `dev.patric.commonlib.runtime`
- `dev.patric.commonlib.scheduler`
- `dev.patric.commonlib.services`
- `dev.patric.commonlib.config`
- `dev.patric.commonlib.message`
- `dev.patric.commonlib.guard`
- `dev.patric.commonlib.lifecycle`

Regole:
- Usabili dai consumer avanzati ma senza garanzia di stabilita' binaria/firmata.
- Refactor consentiti in patch/minor se non violano API contractual.
- Ogni cambiamento significativo deve essere tracciato in changelog.

### Internal / implementation detail
- `dev.patric.commonlib.internal`
- `dev.patric.commonlib.runtime.adapter`
- classi concrete nei moduli `adapter-*`

Regole:
- Nessuna garanzia di compatibilita'.
- Cambiamenti liberi in patch/minor.

## Naming conventions
- API pubbliche: nomi descrittivi, backend-agnostic.
- Implementazioni default: `Default*` nei package runtime.
- No-op pubblici: `api.port.noop.*` (contrattuali come utility stabili).
- Helper bootstrap/error: `api.bootstrap`, `api.error`.

## Compatibilita' ufficiale
- Baseline runtime: Paper `1.21.x`, Java `21`.
- Fuori scope ufficiale: minor Paper precedenti/non `1.21.x`.

## Enforcement operativo
- Freeze ufficiale: `docs/api/API-FREEZE-1.0.0.md`.
- Contratto verificato da `PublicApiFreezeContractTest`.
- Policy dependency guard verificata da:
  - `verifyCoreDependencyPolicy`
  - `verifyAdapterDependencyPolicy`
  - `verifyAdapterLicensePolicy`
