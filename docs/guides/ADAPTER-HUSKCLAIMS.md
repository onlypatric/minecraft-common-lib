# Adapter HuskClaims

## Modulo
- `adapter-huskclaims`

## Scopo
Fornire binding di `ClaimsPort` verso backend HuskClaims con policy deny-safe:
- mai throw verso il consumer;
- fallback deterministico (`false` / `Optional.empty()`) quando claim non risolvibile.

## Component
- `HuskClaimsAdapterComponent`
- bind capability: `StandardCapabilities.CLAIMS`
- reason codes principali:
  - `missing-plugin:HuskClaims`
  - `disabled-plugin:HuskClaims`
  - `incompatible-version:HuskClaims:<installed><required>`
  - `binding-failed:huskclaims:<Exception>`

## Runtime behavior
- Se disponibile: `bindClaimsPort(..., "huskclaims", <version>)`.
- Se non disponibile: `markUnavailable(CLAIMS, reason)`.

## Verifica rapida
```bash
./gradlew --no-daemon :adapter-huskclaims:test
```
