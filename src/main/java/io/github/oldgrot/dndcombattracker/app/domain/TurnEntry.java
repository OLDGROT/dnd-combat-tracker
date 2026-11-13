package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Objects;
import java.util.UUID;

public record TurnEntry(UUID id, int initiative) {

    public TurnEntry {
        Objects.requireNonNull(id, "Turn entry ID cannot be null");
    }

    public TurnEntry copyOf() {
        return new TurnEntry(this.id, this.initiative);
    }
}
