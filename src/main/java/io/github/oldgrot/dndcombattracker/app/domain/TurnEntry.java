package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.UUID;

public record TurnEntry(UUID id, int initiative) {


    public TurnEntry copyOf(){
        return new TurnEntry(this.id, this.initiative);
    }
}
