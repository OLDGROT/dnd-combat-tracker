package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.constats.CombatStatus;

import java.util.List;
import java.util.UUID;

public class Combat {
    private UUID id;
    private String name;
    private int roundNumber;
    private List<Combatant> combatants;
    private CombatStatus status;
    private int version;

}
