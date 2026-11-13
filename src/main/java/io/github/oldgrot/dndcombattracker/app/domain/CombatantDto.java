package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.UUID;

public record CombatantDto(UUID id, int initiative) {


    public CombatantDto copyOf(){
        return new CombatantDto(this.id, this.initiative);
    }
}
