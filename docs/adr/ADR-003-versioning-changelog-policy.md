# ADR-003: Versioning e Changelog Policy

- Data: 2026-03-03
- Stato: Accettata

## Contesto
Il progetto è in fase `0.x` ma richiede tracciabilità delle rotture e delle scelte.

## Decisione
- Versionamento semantico best effort durante `0.x`.
- Breaking changes consentite ma obbligano:
  - entry in `CHANGELOG.md`
  - nota di migrazione se impatta API pubblica.
- Ogni release significativa aggiorna matrice compatibilità.

## Conseguenze
- Evoluzione veloce senza perdere governance.
- Riduzione rischio per plugin consumer in adozione iniziale.

## Allineamento RC1
- Workflow RC adottato: branch candidato, gate completi locali, tag annotato.
- Dopo tagging RC, `main` viene riaperta su versione snapshot successiva.
