# Paper Dialog Model Reference

Riferimento del full model `api.dialog` (`0.9.1`).

## Root model
- `DialogTemplate`
  - `templateKey`
  - `DialogBaseSpec base`
  - `DialogTypeSpec type`
  - `Map<String, String> metadata`

- `DialogBaseSpec`
  - `title`
  - `externalTitle`
  - `canCloseWithEscape`
  - `pause`
  - `DialogAfterAction afterAction`
  - `List<DialogBodySpec> body`
  - `List<DialogInputSpec> inputs`

- `DialogAfterAction`
  - `CLOSE`
  - `NONE`
  - `WAIT_FOR_RESPONSE`

## Body specs
- `DialogBodySpec` (sealed)
  - `PlainMessageBodySpec`
  - `ItemBodySpec`

## Input specs
- `DialogInputSpec` (sealed)
  - `TextInputSpec`
  - `BooleanInputSpec`
  - `NumberRangeInputSpec`
  - `SingleOptionInputSpec`

- `SingleOptionEntrySpec`
  - `id`
  - `display`
  - `initial`

## Type specs
- `DialogTypeSpec` (sealed)
  - `ConfirmationTypeSpec`
  - `NoticeTypeSpec`
  - `MultiActionTypeSpec`
  - `DialogListTypeSpec`
  - `ServerLinksTypeSpec`

- `DialogListTypeSpec`
  - `dialogTemplateKeys`
  - validazione fail-fast su:
    - template mancanti
    - riferimenti ciclici

## Action specs
- `DialogButtonSpec`
  - `label`
  - `tooltip`
  - `width`
  - `DialogActionSpec action`

- `DialogActionSpec` (sealed)
  - `CommandTemplateActionSpec`
  - `StaticClickActionSpec`
  - `CustomActionSpec`

- `StaticClickActionKind`
  - `RUN_COMMAND`
  - `SUGGEST_COMMAND`
  - `OPEN_URL`
  - `COPY_TO_CLIPBOARD`
  - `CHANGE_PAGE`

## Session and events
- `DialogOpenRequest`
- `DialogSession`
- `DialogSessionStatus`: `OPEN`, `CLOSING`, `CLOSED`, `TIMED_OUT`, `ERROR`
- `DialogCloseReason`: `SUBMITTED`, `USER_CLOSE`, `TIMEOUT`, `QUIT`, `PLUGIN_DISABLE`, `REPLACED`, `ERROR`
- `DialogEventResult`: `APPLIED`, `DENIED_BY_POLICY`, `SESSION_NOT_FOUND`, `SESSION_NOT_OPEN`, `INVALID_PAYLOAD`, `HANDLER_ERROR`
- `DialogEvent` (sealed)
  - `DialogSubmitEvent`
  - `DialogTimeoutEvent`
  - `DialogCloseEvent`

## Response extraction
- `DialogResponse`
  - `text(key)`
  - `bool(key)`
  - `number(key)`
  - `rawPayload()`
  - `asMap()`

La conversione tipizzata è delegata a `DefaultDialogResponse`.
