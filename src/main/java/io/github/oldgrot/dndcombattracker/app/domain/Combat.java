package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;
import io.github.oldgrot.dndcombattracker.app.domain.exception.InvalidCombatStateException;
import io.github.oldgrot.dndcombattracker.app.domain.exception.InvalidCombatStateException;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;

@Getter
public class Combat {
    private final UUID id;
    private String name;
    private long roundNumber;
    @Getter(AccessLevel.NONE)
    private final Set<Combatant> combatants;
    private CombatStatus status;
    private long version;

    //Конструктор для создания
    public Combat(UUID id, String name){
        this.id = Objects.requireNonNull(id, "Combat ID cannot be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Combat name cannot be null or blank"
            );
        }

        this.name = name.strip();
        roundNumber = 0L;
        status = CombatStatus.PREPARING;
        combatants = new HashSet<>();
        version = 0L;
    }

    public Set<Combatant> getCombatants(){
        return Set.copyOf(combatants);
    }

    public void addCombatant(Combatant combatant){
        Objects.requireNonNull(combatant, "Combatant cannot be null");

        if(status == CombatStatus.FINISHED || status == CombatStatus.ARCHIVED){
            throw  new InvalidCombatStateException("Don't add combatant in " + status.toString() + "combat");
        }

        if (!combatants.add(combatant)) {
            throw new InvalidCombatStateException(
                    "Combatant with ID " + combatant.getId() + " already exists in combat"
            );
        }
    }
}
