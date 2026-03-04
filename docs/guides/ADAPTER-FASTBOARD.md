# Adapter FastBoard

## Obiettivo
Bindare `ScoreboardPort` su backend FastBoard.

## Modulo
- Gradle: `:adapter-fastboard`
- Dependency principale: `fr.mrmicky:fastboard:2.1.5` (`implementation`)

## Component
- `FastBoardAdapterComponent`
  - probe classpath (`fr.mrmicky.fastboard.FastBoard`)
  - bind `ScoreboardPort` con backend id `fastboard`

## Port implementation
- `FastBoardScoreboardPort`
  - gestisce mapping `sessionId -> FastBoard`
  - `open/render/close` safe e no-throw
  - in caso di errori backend ritorna `false` senza rompere il runtime

## Capability behavior
- available: `fastboard:2.1.5`
- unavailable reason tipici:
  - `missing-class:fr.mrmicky.fastboard.FastBoard`
  - `binding-failed:fastboard:<Exception>`

## Server requirement
Nessun plugin server-side obbligatorio: FastBoard è usato come libreria embedded.
