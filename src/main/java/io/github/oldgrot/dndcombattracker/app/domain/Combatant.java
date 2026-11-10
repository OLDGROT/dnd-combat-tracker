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
    private int initiative;
    private HitPoints hitPoints;
    private boolean surprised;

    public Combatant(UUID id, String name, HitPoints hitPoints){
        this.id = Objects.requireNonNull(id,"");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    ""
            );
        }
        this.name = name.strip();
        this.initiative = 0;
        this.surprised = false;

        this.hitPoints = Objects.requireNonNull(hitPoints, "");
    }
}
