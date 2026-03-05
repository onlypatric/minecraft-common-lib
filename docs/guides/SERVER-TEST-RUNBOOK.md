# SERVER-TEST Runbook

Questo runbook definisce lo smoke reale locale con server Paper per validare `minecraft-common-lib` fuori da MockBukkit.

## Posizione
- `/Users/patric/Documents/Minecraft/SERVER-TEST`

## Prerequisiti
- Java 21
- `bash`, `curl`, `python3`
- rete disponibile

## Scenari
1. `fallback-no-external`
- installa solo `smoke-host`.
- valida fallback capability con reason `unavailable(...)`.

2. `wave-full-best-effort`
- tenta download plugin esterni wave adapter completa.
- in caso di fallimento download/handshake il run continua e chiude con `WARN`.

## Esecuzione
```bash
cd /Users/patric/Documents/Minecraft/SERVER-TEST
./scripts/setup.sh
./scripts/run-smoke.sh --scenario fallback-no-external
./scripts/run-smoke.sh --scenario wave-full-best-effort
```

## Output report
- `reports/<run-id>/report.md`
- `reports/<run-id>/report.json`
- `reports/<run-id>/capabilities.csv`
- `reports/<run-id>/modules.csv`
- `reports/<run-id>/downloads.csv`

## Criteri minimi
1. Il server arriva a `Done (...)`.
2. Il log contiene dump capability `CLIB_SMOKE_CAPS`.
3. Il log contiene dump moduli `CLIB_SMOKE_MODULES`.
4. Lo shutdown è pulito (`stop` senza crash host plugin).
5. `wave-full-best-effort` non interrompe il run al primo download fallito.

## Note
- Runtime/cache/report sono ignorati da git in `SERVER-TEST/.gitignore`.
- Nessuna modifica API core è richiesta da questo flusso.
