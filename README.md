# EVFreeze

A Java plugin for Paper/Folia that allows freezing players.

## Features

- `/freeze <player>` - freeze a player.
- `/unfreeze <player>` - unfreeze a player.
- `/freezelist` - show a list of frozen players and their freeze duration.
- Blocks movement, commands, block placing/breaking, inventory access, interactions, and other actions.
- Chat remains available.
- MiniMessage support for messages and titles.
- Stores frozen players in `data.yml`.
- Paper and Folia support without NMS.
- PlaceholderAPI: `%evfreeze_frozen%` returns the value of `placeholders.frozen` from `messages.yml` if the player is frozen, otherwise an empty string.

## Configuration

After the first launch, EVFreeze creates:

```text
plugins/EVFreeze/messages.yml
plugins/EVFreeze/data.yml
```

Messages, titles, sounds, localization, and PlaceholderAPI responses can be changed in `messages.yml`.

## Permissions

- `evfreeze.admin` - grants all plugin permissions, OP-only by default.
- `evfreeze.freeze` - allows using `/freeze`.
- `evfreeze.unfreeze` - allows using `/unfreeze`.
- `evfreeze.list` - allows using `/freezelist`.

## Build

```bash
./gradlew build
```

The jar will be created at:

```text
app/build/libs/EVFreeze-1.0.0.jar
```
