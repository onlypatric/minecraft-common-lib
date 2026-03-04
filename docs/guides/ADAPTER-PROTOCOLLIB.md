# Adapter ProtocolLib

## Modulo
- `adapter-protocollib`

## Scopo
Esporre `PacketPort` wrapper-oriented senza riscrivere protocol translation layer.

## Component
- `ProtocolLibAdapterComponent`
- `ProtocolLibPacketPort`
- capability: `StandardCapabilities.PACKETS`

## Version strategy
Supporta due path:
- stable semver minima (`5.3.0`)
- dev build abilitate via regex configurabile (`commonlib.adapters.protocollib.devVersionRegex`)

## Reason codes
- `missing-plugin:ProtocolLib`
- `disabled-plugin:ProtocolLib`
- `incompatible-version:ProtocolLib:<installed><required>`
- `missing-class:com.comphenix.protocol.ProtocolLibrary`
- `binding-failed:protocollib:<Exception>`

## Coordinate override
Il modulo accetta override Gradle:
```bash
-PprotocolLibCoordinate=com.comphenix.protocol:ProtocolLib:<version-or-dev-build>
```

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-protocollib:test
```
