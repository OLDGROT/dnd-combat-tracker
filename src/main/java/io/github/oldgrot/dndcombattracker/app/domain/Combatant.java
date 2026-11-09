package io.github.oldgrot.dndcombattracker.app.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor // пока так, потом решим как создаётся
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Combatant {
    @EqualsAndHashCode.Include
    private final UUID id;
    private String name;
    private int initiative;
    private int positionX;
    private int positionY;
    private int maxHp;
    private int currentHp;
    private int temporaryHp;
    private boolean surprised;


}
