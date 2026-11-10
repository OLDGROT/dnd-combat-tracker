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
    private int maxHp;
    private int currentHp;
    private int temporaryHp;
    private boolean surprised;

    public Combatant(UUID id,
                          String name,
                          int maxHp,
                          int currentHp,
                          int temporaryHp){
        this.id = Objects.requireNonNull(id,"");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    ""
            );
        }

        this.name = name.strip();
        this.initiative = 0;
        if (maxHp <= 0) {
            throw new IllegalArgumentException(
                    ""
            );
        }
        this.maxHp = maxHp;

        if (currentHp <= 0 || currentHp > maxHp) {
            throw new IllegalArgumentException(
                    ""
            );
        }
        this.currentHp = currentHp;

        if (temporaryHp < 0) {
            throw new IllegalArgumentException(
                    ""
            );
        }
        this.temporaryHp = temporaryHp;
        this.surprised = false;
    }
}
