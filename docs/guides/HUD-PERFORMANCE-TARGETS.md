# HUD Performance Targets (`v0.5.0`)

## Target misurabili
1. Per audience/sessione: non più di 1 render ogni 5 tick con policy default.
2. Payload invariato: dedupe senza render backend aggiuntivo.
3. Scenario 100 audience con update ogni tick: numero render limitato dal throttle (assert su contatori, non su tempo wall-clock).

## Metodo di verifica
- Usare test con fake ports e render counters.
- Simulare tick con MockBukkit scheduler (`performTicks`).
- Verificare risultati API (`THROTTLED`, `DEDUPED`, `APPLIED`) e contatori render.

## Scope
Questi target validano comportamento funzionale del rate limiting/dedup.
Non rappresentano benchmark di latenza server reale o profiling JVM completo.
