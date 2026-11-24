package io.github.oldgrot.dndcombattracker.app.domain;


import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Combatant {
    @EqualsAndHashCode.Include
    private final UUID id;
    private String name;
    private final HitPoints hitPoints;
    private boolean surprised;
    private int initiative;
    private boolean hasActedThisRound;

    public Combatant(UUID id, String name, HitPoints hitPoints, int initiative){
        this.id = Objects.requireNonNull(id, "Combatant ID cannot be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Combatant name cannot be null or blank"
            );
        }
        this.name = name.strip();
        this.surprised = false;
        if(initiative < 0) {
            throw new IllegalArgumentException("");
        }
        this.initiative = initiative;
        this.hasActedThisRound = false;

        this.hitPoints = Objects.requireNonNull(hitPoints, "Combatant hit points cannot be null");

    }

    public void markTurnCompleted(){
        this.hasActedThisRound = true;
    }
}
