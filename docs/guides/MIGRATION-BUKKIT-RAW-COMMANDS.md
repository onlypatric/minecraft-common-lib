# Migration from Bukkit Raw Commands

Guida rapida per migrare da `onCommand(...)` Bukkit raw al command model `v0.3.0`.

## Prima (raw Bukkit)
- parsing argomenti manuale;
- permission checks duplicati;
- gestione sync/async distribuita nel comando.

## Dopo (common-lib)
1. Definisci un `CommandModel` con nodi/permission/metadata.
2. Registra il model nel `CommandRegistry`.
3. Valida con `CommandValidator`.
4. Esegui con `CommandExecutions` per handoff sync sicuro.

## Mapping consigliato
- `CommandSender` -> `CommandContext` (`senderId`, `locale`, `args`).
- `sender.hasPermission(...)` -> `CommandPermission` + validator.
- parse argomenti -> `CommandNode` + `ArgumentType` + constraints.
- task async -> `ExecutionMode.ASYNC_IO_THEN_SYNC`.

## Compatibilità
`CommandPort` è intentionally backend-agnostic: in `0.3.0` non include ancora adapter concreti (CommandAPI/Cloud).
