# ADR-002: Policy API pubblica vs internal

- Data: 2026-03-03
- Stato: Accettata

## Contesto
Servono confini chiari per evitare rotture involontarie del contratto pubblico.

## Decisione
- API pubblica primaria: `dev.patric.commonlib.api`.
- Package stabili secondari documentati: `runtime`, `scheduler`, `config`, `message`, `guard`, `lifecycle`, `services`.
- Tutto `dev.patric.commonlib.internal` è esplicitamente instabile.

## Conseguenze
- Consumatori guidati su superfici API affidabili.
- Refactor interni liberi senza promesse di backward compatibility su `internal`.
