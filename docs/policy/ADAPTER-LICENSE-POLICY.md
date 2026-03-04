# Adapter License Policy

Questa policy definisce come integrare librerie/plugin esterni nella roadmap adapter,
proteggendo la natura embed-first e commercial-friendly del core.

## Principi
- Il core `minecraft-common-lib` non include dipendenze runtime di adapter esterni.
- Gli adapter reali sono opzionali, separati e installabili per progetto/server.
- Nessuna libreria esterna deve entrare in `api`, `runtime`, `services`, `scheduler`, `config`, `message`, `guard`, `lifecycle`.

## Classificazione licenze

### Permissive (preferred)
- MIT
- Apache-2.0
- BSD-like

Policy:
- consentite come dipendenze adapter opzionali.
- valutare comunque stabilità progetto e qualità API.

### Weak/Strong copyleft (caution)
- LGPL
- GPL (es. GPL-2.0, GPL-3.0)

Policy:
- non entrare nel core.
- adapter separato con documentazione esplicita su impatti di distribuzione.
- nessuno shading nel core.
- nessuna dipendenza copyleft transitiva verso modulo root.

### Proprietarie / commerciali
- plugin closed-source o con EULA commerciale.

Policy:
- integrazione solo tramite adapter opzionale separato.
- nessuna redistribuzione di artifact proprietari.
- documentare prerequisiti di installazione e licensing lato server owner.

## Regole operative
- Ogni adapter candidato deve avere una scheda backlog con:
  - licenza
  - compatibilità Paper target
  - rischio lock-in
  - strategia fallback (no-op)
- Se la licenza è non-permissiva, aggiungere nota in release notes/version docs.
- Le capability pubbliche nel core restano neutrali rispetto al backend.

## Enforcement build
- `verifyCoreDependencyPolicy` blocca dipendenze esterne nel core.
- `verifyAdapterDependencyPolicy` blocca cross-link non ammessi tra moduli adapter.
- `verifyAdapterLicensePolicy` applica vincoli ai moduli con rischio licenza:
  - `adapter-fawe`
  - `adapter-protocollib`
- Per questi moduli, `api`/`implementation`/`runtimeOnly` non possono dichiarare dipendenze esterne.
- Le dipendenze esterne ammesse restano solo `compileOnly` nel modulo adapter dedicato.
