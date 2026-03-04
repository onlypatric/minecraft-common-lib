# Packets Backend Evaluation

## Scope `0.9.0`
- Adapter reale implementato: ProtocolLib.
- PacketEvents rimane valutazione tecnica (no adapter production in questo ciclo).

## Criteri go/no-go PacketEvents (ciclo `1.0.x`)
1. Stabilità API e compatibilità Paper target.
2. Costo manutenzione mapping packet types/versioning.
3. Sicurezza mutate/cancel path su listener multipli.
4. DX lato consumer (registration, lifecycle, debugability).

## Decisione attuale
- mantenere un singolo backend packets operativo (`ProtocolLib`) in `0.9.0`.
- riesaminare PacketEvents dopo freeze API 1.0.
