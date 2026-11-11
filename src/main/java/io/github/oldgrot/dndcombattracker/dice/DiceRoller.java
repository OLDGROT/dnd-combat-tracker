package io.github.oldgrot.dndcombattracker.dice;

import java.util.Objects;
import java.util.random.RandomGenerator;

public class DiceRoller {

    private final RandomGenerator randomGenerator;

    public DiceRoller(RandomGenerator randomGenerator) {
        this.randomGenerator = Objects.requireNonNull(
                randomGenerator,
                "RandomGenerator cannot be null"
        );
    }

    public int roll(int sides){
        if(sides < 2){
            throw new IllegalArgumentException("");
        }
        return randomGenerator.nextInt(1, sides + 1);
    }
}
