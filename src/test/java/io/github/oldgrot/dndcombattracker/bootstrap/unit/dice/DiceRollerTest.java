package io.github.oldgrot.dndcombattracker.bootstrap.unit.dice;

import java.util.random.RandomGenerator;

import io.github.oldgrot.dndcombattracker.dice.DiceRoller;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DiceRollerTest {

    @Nested
    class Creation {

        @Test
        void rejectsNullRandomGenerator() {
            assertThatThrownBy(() -> new DiceRoller(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("RandomGenerator cannot be null");
        }
    }

    @Nested
    class Roll {

        @ParameterizedTest(name = "бросает d{0} с контролируемым результатом {1}")
        @CsvSource({
                "2, 1",
                "2, 2",
                "6, 4",
                "20, 20",
                "100, 73"
        })
        void returnsControlledValueAndUsesInclusiveDiceRange(int sides, int expectedResult) {
            RandomGenerator randomGenerator = mock(RandomGenerator.class);
            when(randomGenerator.nextInt(1, sides + 1)).thenReturn(expectedResult);
            DiceRoller diceRoller = new DiceRoller(randomGenerator);

            int result = diceRoller.roll(sides);

            assertThat(result).isEqualTo(expectedResult);
            verify(randomGenerator).nextInt(1, sides + 1);
        }

        @ParameterizedTest(name = "отклоняет число граней вне диапазона: {0}")
        @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 101, Integer.MAX_VALUE})
        void rejectsDiceOutsideSupportedRange(int sides) {
            RandomGenerator randomGenerator = mock(RandomGenerator.class);
            DiceRoller diceRoller = new DiceRoller(randomGenerator);

            assertThatThrownBy(() -> diceRoller.roll(sides))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A die must have between 2 and 100 faces");
            verifyNoInteractions(randomGenerator);
        }
    }
}
