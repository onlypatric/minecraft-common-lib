# External Matrix Tests

## Task
- `externalMatrixTest` (opt-in)

## Abilitazione
```bash
./gradlew --no-daemon externalMatrixTest -PrunExternalMatrix=true
```

## Copertura
- claims / npc / boss / packets capability matrix on/off
- handshake ProtocolLib strategy (stable + dev regex)
- handshake HuskClaims availability paths
- handshake WorldEdit + FAWE precedence

## Test class
- `ExternalClaimsNpcBossPacketsMatrixTest`
- `ExternalProtocolLibHandshakeTest`
- `ExternalHuskClaimsHandshakeTest`
- `ExternalWorldEditFaweHandshakeTest`
