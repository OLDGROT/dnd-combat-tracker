package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.List;
import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.Combat;
import io.github.oldgrot.dndcombattracker.app.domain.constants.CombatStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

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

            assertThat(ReflectionTestUtils.getField(combat, "id")).isEqualTo(id);
            assertThat(ReflectionTestUtils.getField(combat, "name")).isEqualTo(name.strip());
            assertThat(ReflectionTestUtils.getField(combat, "roundNumber")).isEqualTo(0L);
            assertThat(ReflectionTestUtils.getField(combat, "status")).isEqualTo(CombatStatus.PREPARING);
            assertThat(ReflectionTestUtils.getField(combat, "version")).isEqualTo(0L);
            assertThat((List<?>) ReflectionTestUtils.getField(combat, "combatants")).isEmpty();
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
