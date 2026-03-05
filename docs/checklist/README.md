# Checklist di Versione

Questa cartella contiene la roadmap operativa in formato checklist per ogni versione della libreria `minecraft-common-lib`.

## Ordine di esecuzione
1. [v0.1.0-alpha.2](./01-v0.1.0-alpha.2.md)
2. [v0.1.0-alpha.3](./02-v0.1.0-alpha.3.md)
3. [v0.1.0-rc.1](./03-v0.1.0-rc.1.md)
4. [v0.1.0](./04-v0.1.0.md)
5. [v0.2.0](./05-v0.2.0.md)
6. [v0.3.0](./06-v0.3.0.md)
7. [v0.4.0](./07-v0.4.0.md)
8. [v0.5.0](./08-v0.5.0.md)
9. [v0.6.0](./09-v0.6.0.md)
10. [v0.7.0](./10-v0.7.0.md)
11. [v0.8.0](./11-v0.8.0.md)
12. [v0.9.0](./12-v0.9.0.md)
13. [v1.0.0](./13-v1.0.0.md)
14. [v2.0.0 GUI](./14-v2.0.0-gui.md)
15. [v2.1.0 Module System](./15-v2.1.0-module-system.md)

## Regole comuni
- Core sempre plugin-generic first, adapter esterni opzionali.
- Nessun hard dependency su plugin terzi nel core.
- Thread safety: accesso Bukkit/Paper in sync thread.
- Ogni versione deve chiudere con build verde: `clean test javadoc build`.
