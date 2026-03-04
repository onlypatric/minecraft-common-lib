# ADR-004: GUI v2 Fluent DSL and Slot Policy Engine

- Stato: Accettata
- Data: 2026-03-04

## Contesto
Il modello GUI precedente era session-oriented ma troppo basilare per casi reali (policy slot tipizzate, integrazione dialog, handling uniforme click/drag/transfer).

## Decisione
1. Introdurre GUI v2 con API tipizzate (`GuiDefinition`, `SlotDefinition`, `GuiAction`, `GuiInteractionEvent`).
2. Introdurre DSL fluente Java (`GuiDsl`) come authoring principale.
3. Introdurre policy slot native (`BUTTON_ONLY`, `INPUT_DIALOG`, `TAKE_ONLY`, `DEPOSIT_ONLY`, `TAKE_DEPOSIT`, `LOCKED`).
4. Integrare `DialogService` come action nativa (`OpenDialogAction`).
5. Adottare `InvUI` come primo adapter GUI in modulo separato.

## Conseguenze
- Superficie GUI più espressiva e testabile.
- Riduzione boilerplate lato plugin consumer.
- Maggior controllo su race/policy e semantica inventory.
- Breaking changes confinate al ciclo major 2.0.
