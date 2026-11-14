package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Objects;
import java.util.UUID;

public record TurnEntry(UUID combatantId, int initiative, boolean hasActedThisRound) {

    public TurnEntry {
        Objects.requireNonNull(combatantId, "Turn entry ID cannot be null");
    }

    public TurnEntry markTurnCompleted() {
        return new TurnEntry(
                combatantId,
                initiative,
                true
        );
    }

}
