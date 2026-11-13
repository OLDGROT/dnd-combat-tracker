package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Objects;
import java.util.UUID;

public record TurnEntry(UUID id, int initiative, boolean isActive) {

    public TurnEntry {
        Objects.requireNonNull(id, "Turn entry ID cannot be null");
    }
}
