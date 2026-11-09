package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.Combat;
import io.github.oldgrot.dndcombattracker.app.domain.Combatant;
import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;
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
        void combatantsReturnedByGetterCannotBeModified() {
            Combat combat = new Combat(UUID.randomUUID(), "Battle");

            assertThatThrownBy(() -> combat.getCombatants().add(new Combatant()))
                    .isInstanceOf(UnsupportedOperationException.class);
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
}
