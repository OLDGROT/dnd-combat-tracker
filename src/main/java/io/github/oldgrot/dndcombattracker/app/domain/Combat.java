package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Combat {
    private UUID id;
    private String name;
    private int roundNumber;
    private List<Combatant> combatants;
    private CombatStatus status;
    private long version;


    public Combat(UUID id, String name){
        if(id == null || name == null){
          throw new IllegalArgumentException();
        }

        this.id = id;
        this.name = name;
        roundNumber = 0;
        status = CombatStatus.PREPARING;
        combatants = new ArrayList<>();
    }
}
