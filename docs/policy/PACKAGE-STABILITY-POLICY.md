# Package Stability Policy

Questa policy definisce stabilità e naming dei package in `minecraft-common-lib`.

## Livelli di stabilità

### Stable API
- `dev.patric.commonlib.api`
- `dev.patric.commonlib.api.port`

Regole:
- backward compatibility best-effort in `0.1.x`.
- breaking change solo con changelog e nota migrazione.

### Runtime Stable (soft)
- `dev.patric.commonlib.runtime`
- `dev.patric.commonlib.scheduler`
- `dev.patric.commonlib.services`
- `dev.patric.commonlib.config`
- `dev.patric.commonlib.message`
- `dev.patric.commonlib.guard`
- `dev.patric.commonlib.lifecycle`

Regole:
- usabili dai consumer avanzati, ma con maggiore probabilità di refactor.
- ogni modifica significativa va documentata.

### Internal (unstable)
- `dev.patric.commonlib.internal`

Regole:
- nessuna garanzia di compatibilità.
- uso esterno sconsigliato.

## Naming conventions
- API pubbliche: nomi descrittivi e neutrali rispetto al backend.
- Implementazioni default: prefisso/suffisso `Default*` nel package runtime.
- Helper: package dedicati (`api.bootstrap`, `api.error`) con classi final utility/POJO.

## Enforcement operativo
- Ogni nuova classe deve dichiarare package target coerente con questa policy.
- PR che spostano classi tra livelli devono aggiornare changelog + ADR-002.
