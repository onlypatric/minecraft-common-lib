# ADR-005: Module System Fluent Soft-Disable

- Stato: Accettata
- Data: 2026-03-04

## Contesto
Con la crescita del core, il wiring manuale via `components(...)` rende fragile la gestione dipendenze e l'orchestrazione lifecycle.

## Decisione
1. Introdurre un Module System first-class in `api.module` con dichiarazione fluent Java (`CommonModule`).
2. Pianificare i moduli con dependency graph deterministico (topological order + tie-break lessicografico).
3. Adottare policy errori `soft-disable`: failure di un modulo non blocca il runtime globale.
4. Esportare diagnostica runtime tramite `ModuleRegistry` e status machine-readable.
5. Mantenere compatibilita' con `CommonComponent` legacy senza breaking.

## Conseguenze
- Wiring plugin consumer piu' prevedibile e meno manuale.
- Failure isolation migliore durante load/enable.
- Diagnostica moduli disponibile per smoke reali (`SERVER-TEST`).
- Nessuna dipendenza esterna aggiuntiva nel core.
