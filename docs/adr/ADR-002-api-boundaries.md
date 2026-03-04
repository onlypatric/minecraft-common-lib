# ADR-002: Policy API pubblica vs internal

- Data: 2026-03-03
- Stato: Accettata (aggiornata 2026-03-04 per freeze `1.0.0`)

## Contesto
Servono confini chiari per evitare rotture involontarie del contratto pubblico, soprattutto al passaggio da `0.x` a `1.0.0`.

## Decisione
- API pubblica contrattuale: `dev.patric.commonlib.api` e `dev.patric.commonlib.api.port`.
- Freeze ufficiale `1.0.0`: `docs/api/API-FREEZE-1.0.0.md`.
- Package supportati ma non contrattuali: `runtime`, `scheduler`, `services`, `config`, `message`, `guard`, `lifecycle`.
- Package instabili: `dev.patric.commonlib.internal`, `runtime.adapter`, implementazioni concrete nei moduli `adapter-*`.

## Conseguenze
- Consumer guidati su superfici API stabili e versionabili con SemVer.
- Refactor interni consentiti senza promessa di backward compatibility fuori dalla superficie contrattuale.
- Riduzione del rischio regressione nei plugin embed-first multi-consumer.

## Enforcement
- Contract test freeze: `PublicApiFreezeContractTest`.
- Policy package/dependency via Gradle verification tasks.
- Ogni PR che modifica i confini deve aggiornare:
  - `docs/api/API-FREEZE-1.0.0.md`
  - `docs/policy/PACKAGE-STABILITY-POLICY.md`
  - changelog/release notes.

## Note storiche
- Alpha/RC: policy iniziale e freeze RC1 formalizzati in `API-FREEZE-0.1.0-rc.1`.
- GA `1.0.0`: boundary finali consolidati su scope contrattuale `api/*` + `api/port/*`.
