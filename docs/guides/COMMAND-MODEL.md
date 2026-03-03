# Command Model Guide

`v0.3.0` introduce un command model backend-agnostic in `dev.patric.commonlib.api.command`.

## Obiettivo
Separare contratto di comando e validazione dal backend registrante (Bukkit raw, CommandAPI, Cloud).

## Building blocks
- `CommandModel`: root, nodi argomento, permission, metadata, execution.
- `CommandNode`: definisce argomento (`ArgumentType`), required e constraints.
- `CommandValidator`: valida richieste + permission policy.
- `CommandRegistry`: registro runtime dei modelli.
- `CommandExecutions`: pipeline mode-aware (`SYNC` / `ASYNC_IO_THEN_SYNC`).

## Esempio minimo
```java
CommandModel model = new CommandModel(
        "arena",
        List.of(new CommandNode("name", ArgumentType.STRING, true, List.of())),
        new CommandExecution() {
            @Override
            public ExecutionMode mode() {
                return ExecutionMode.SYNC;
            }

            @Override
            public CompletionStage<CommandResult> run(CommandContext ctx) {
                return CompletableFuture.completedFuture(CommandResult.success());
            }
        },
        new CommandPermission("arena.use", PermissionPolicy.REQUIRE),
        new CommandMetadata("Arena command", List.of("a"))
);

services.require(CommandRegistry.class).register(model);
```

## Validation policy baseline
- errore se argomento required manca;
- errore se tipo argomento non combacia;
- errore permission quando policy `REQUIRE` fallisce;
- errore hard quando policy `DISABLE_COMMAND` è attiva.

## Execution mode
- `SYNC`: esecuzione diretta.
- `ASYNC_IO_THEN_SYNC`: fase async I/O seguita da handoff sync tramite `CommonScheduler`.

## Note
`CommandPort` in `v0.3.0` è stato ridisegnato: ora accetta `CommandModel` e supporta unregister.
