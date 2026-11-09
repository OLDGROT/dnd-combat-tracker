package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Combat {
    private final UUID id;
    private String name;
    private long roundNumber;
    private List<Combatant> combatants;
    private CombatStatus status;
    private int version;


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
        combatants = new ArrayList<>();
        version = 0;
    }
}
