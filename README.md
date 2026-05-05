# EVFreeze

Paper/Folia плагин на Java для заморозки игроков.

## Возможности

- `/freeze <player>` - заморозить игрока.
- `/unfreeze <player>` - разморозить игрока.
- `/freezelist` - список замороженных игроков и время заморозки.
- Блокировка движения, команд, установки и ломания блоков, инвентаря, взаимодействий и других действий.
- Чат остается доступен.
- MiniMessage для сообщений и title.
- SQLite база данных вместо JSON/YAML.
- Поддержка Paper и Folia без NMS.
- PlaceholderAPI: `%evfreeze_frozen%` возвращает значение `placeholders.frozen` из `messages.yml`, если игрок заморожен, иначе пустую строку.

## Сборка

```bash
./gradlew build
```

Готовый jar будет в:

```text
app/build/libs/EVFreeze-1.0.0.jar
```

## Настройка

После первого запуска появится файл:

```text
plugins/EVFreeze/messages.yml
```

В нем можно менять все сообщения, title и звуки.

## Права

- `evfreeze.admin` - все права плагина, по умолчанию только OP.
- `evfreeze.freeze` - `/freeze`.
- `evfreeze.unfreeze` - `/unfreeze`.
- `evfreeze.list` - `/freezelist`.
