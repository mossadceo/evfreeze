# EVFreeze

A Java plugin for Paper/Folia that allows freezing players.

## Features

- `/freeze <player>` - freeze a player.
- `/unfreeze <player>` - unfreeze a player.
- `/freezelist` - show a list of frozen players and their freeze duration.
- Blocks movement, commands, block placing/breaking, inventory access, interactions, and other actions.
- Chat remains available.
- MiniMessage support for messages and titles.
- SQLite database
- Paper and Folia support without NMS.
- PlaceholderAPI: `%evfreeze_frozen%` returns the value of `placeholders.frozen` from `messages.yml` if the player is frozen, otherwise an empty string.

All messages, titles, and sounds can be changed.

## Permissions

- `evfreeze.admin` - grants all plugin permissions, OP-only by default.
- `evfreeze.freeze` - allows using `/freeze`.
- `evfreeze.unfreeze` - allows using `/unfreeze`.
- `evfreeze.list` - allows using `/freezelist`.

## Build

```bash
./gradlew build
