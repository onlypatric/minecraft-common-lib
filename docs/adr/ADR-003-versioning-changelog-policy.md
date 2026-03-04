# ADR-003: Versioning e Changelog Policy

- Data: 2026-03-03
- Stato: Accettata (aggiornata 2026-03-04 per policy post-`1.0.0`)

## Contesto
In `0.x` il progetto ha evoluto rapidamente con breaking changes controllate. Con `1.0.0` serve una policy SemVer piena e stabile.

## Decisione

### Pre-1.0 (`0.x`)
- Versionamento SemVer best-effort.
- Breaking consentite con obbligo di:
  - entry in `CHANGELOG.md`
  - nota di migrazione.

### Post-1.0 (`>=1.0.0`)
- SemVer rigoroso:
  - `MAJOR`: breaking changes pubbliche.
  - `MINOR`: funzionalita' backward-compatible.
  - `PATCH`: bugfix/hardening backward-compatible.
- Nessuna breaking su `api/*` + `api/port/*` in patch/minor.
- Ogni release aggiorna:
  - `CHANGELOG.md`
  - `docs/releases/<version>.md`
  - `docs/COMPATIBILITY-MATRIX.md` (se cambia compatibilita').

## Release flow ufficiale
- Candidate: `x.y.z-rc.n` su branch release dedicata.
- GA: `x.y.z` con tag annotato.
- Post-GA: riapertura `main` su snapshot successivo.

## Conseguenze
- Governance chiara per plugin consumer multipli.
- Riduzione regressioni in adozione embed-first.
- Tracciabilita' completa delle decisioni di compatibilita'.
