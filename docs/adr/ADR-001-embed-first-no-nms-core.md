# ADR-001: Embed-first e No-NMS nel Core

- Data: 2026-03-03
- Stato: Accettata

## Contesto
La libreria deve ridurre boilerplate Bukkit/Paper mantenendo bassa complessità di distribuzione e upgrade.

## Decisione
- Il core è distribuito in modalità embed-first (shading nel plugin consumer).
- Il core non contiene integrazioni NMS dirette.
- Tutte le integrazioni volatili (NMS/packet/plugin esterni) restano future e opzionali via port/adapter.

## Conseguenze
- Meno lock-in e minore rischio su update Paper.
- API core più stabile e testabile.
- Funzionalità avanzate delegate a cicli successivi.

## Allineamento RC1
- Confermato che `0.1.0-rc.1` mantiene `embed-first` e assenza di NMS nel core.
- Confermato che gli adapter restano backlog post `v0.1.x`.
