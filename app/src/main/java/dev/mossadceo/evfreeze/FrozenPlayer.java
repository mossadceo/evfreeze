package dev.mossadceo.evfreeze;

import java.util.UUID;

public record FrozenPlayer(UUID uuid, String name, long frozenAtMillis) {
}
