package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.Combat;
import io.github.oldgrot.dndcombattracker.app.domain.Combatant;
import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;
import io.github.oldgrot.dndcombattracker.app.domain.exception.InvalidCombatStateException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombatTest {

    @Nested
    class Creation {

        @ParameterizedTest(name = "создаёт Combat и нормализует имя: [{0}]")
        @ValueSource(strings = {
                "   Battle",
                "    Battle   ",
                "Battle of Earth",
                "  Battle of Earth  ",
                "    Battle of Earth",
                "Battle of Earth   "
        })
        void happyPathCreatesCombatWithNormalizedNameAndInitialState(String name) {
            UUID id = UUID.randomUUID();

            Combat combat = new Combat(id, name);

            assertThat(combat.getId()).isEqualTo(id);
            assertThat(combat.getName()).isEqualTo(name.strip());
            assertThat(combat.getRoundNumber()).isZero();
            assertThat(combat.getStatus()).isEqualTo(CombatStatus.PREPARING);
            assertThat(combat.getVersion()).isZero();
            assertThat(combat.getCombatants()).isEmpty();
        }

        @Test
        void rejectsNullId() {
            assertThatThrownBy(() -> new Combat(null, "Battle"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combat ID cannot be null");
        }

        @Test
        void rejectsNullName() {
            assertThatThrownBy(() -> new Combat(UUID.randomUUID(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combat name cannot be null or blank");
        }

        @Test
        void rejectsNullIdAndNullNameByIdInvariantFirst() {
            assertThatThrownBy(() -> new Combat(null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combat ID cannot be null");
        }

        @Test
        void rejectsNullIdAndBlankNameByIdInvariantFirst() {
            assertThatThrownBy(() -> new Combat(null, "     "))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combat ID cannot be null");
        }

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> new Combat(UUID.randomUUID(), "     "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combat name cannot be null or blank");
        }
    }

    @Nested
    class AddingCombatants {

        @Test
        void addsCombatantToPreparingCombat() {
            Combat combat = new Combat(UUID.randomUUID(), "Battle");
            Combatant combatant = createCombatant(UUID.randomUUID(), "Fighter");

            combat.addCombatant(combatant);

            assertThat(combat.getCombatants()).containsExactly(combatant);
        }

        @Test
        void rejectsNullCombatant() {
            Combat combat = new Combat(UUID.randomUUID(), "Battle");

            assertThatThrownBy(() -> combat.addCombatant(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combatant cannot be null");
            assertThat(combat.getCombatants()).isEmpty();
        }

        @Test
        void rejectsDuplicateCombatantId() {
            Combat combat = new Combat(UUID.randomUUID(), "Battle");
            UUID combatantId = UUID.randomUUID();
            Combatant first = createCombatant(combatantId, "Fighter");
            Combatant duplicate = createCombatant(combatantId, "Wizard");
            combat.addCombatant(first);

            assertThatThrownBy(() -> combat.addCombatant(duplicate))
                    .isInstanceOf(InvalidCombatStateException.class)
                    .hasMessageContaining(combatantId.toString());
            assertThat(combat.getCombatants()).containsExactly(first);
        }

        @Test
        void combatantsReturnedByGetterCannotBeModified() {
            Combat combat = new Combat(UUID.randomUUID(), "Battle");

            assertThatThrownBy(() -> combat.getCombatants().add(createCombatant(UUID.randomUUID(), "Fighter")))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThat(combat.getCombatants()).isEmpty();
        }
    }

    private Combatant createCombatant(UUID id, String name) {
        return new Combatant(id, name, 10, 0, 0, 10, 10, 0, false);
    }
}
