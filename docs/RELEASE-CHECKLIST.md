# Release Readiness Checklist

## Core quality
- [ ] `./gradlew clean test javadoc build` verde
- [ ] Nessun task scheduler orfano in shutdown test
- [ ] API pubblica documentata in README

## Governance
- [ ] CHANGELOG aggiornato
- [ ] ADR aggiornate se ci sono decisioni nuove
- [ ] Compatibility matrix aggiornata

## Packaging
- [ ] Version bump coerente (`alpha/rc/stable`)
- [ ] Tag git creato (`vX.Y.Z`)
- [ ] Artifact verificato localmente

## Post-release
- [ ] Esempio quickstart validato
- [ ] Backlog adapter rivalutato
