package io.github.oldgrot.dndcombattracker.app.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor // пока так, потом решим как создаётся
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

    public void Combatant(UUID id,
                          String name,
                          int maxHp,
                          int currentHp,
                          int temporaryHp){
        id = Objects.requireNonNull(id,"");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    ""
            );
        }

        this.initiative = 0;
        this.maxHp = Integer.max(0, maxHp);
        this.currentHp = Integer.min(currentHp, this.maxHp);
        this.temporaryHp = Integer.max(0, temporaryHp);
        this.surprised = false;
    }
}
