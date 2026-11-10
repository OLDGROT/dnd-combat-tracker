package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.Combatant;
import io.github.oldgrot.dndcombattracker.app.domain.HitPoints;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombatantTest {

    private static final HitPoints VALID_HIT_POINTS = new HitPoints(10, 10, 0);

    @Nested
    class Creation {

        @ParameterizedTest(name = "создаёт Combatant и нормализует имя: [{0}]")
        @ValueSource(strings = {
                "   Fighter",
                "    Fighter   ",
                "Fighter of Earth",
                "  Fighter of Earth  ",
                "    Fighter of Earth",
                "Fighter of Earth   "
        })
        void happyPathCreatesCombatantWithNormalizedNameAndInitialState(String name) {
            UUID id = UUID.randomUUID();

            Combatant combatant = new Combatant(id, name, VALID_HIT_POINTS);

            assertThat(combatant.getId()).isEqualTo(id);
            assertThat(combatant.getName()).isEqualTo(name.strip());
            assertThat(combatant.getHitPoints()).isSameAs(VALID_HIT_POINTS);
            assertThat(combatant.getInitiative()).isZero();
            assertThat(combatant.isSurprised()).isFalse();
        }

        @Test
        void rejectsNullId() {
            assertThatThrownBy(() -> new Combatant(null, "Fighter", VALID_HIT_POINTS))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combatant ID cannot be null");
        }

        @Test
        void rejectsNullName() {
            assertThatThrownBy(() -> new Combatant(UUID.randomUUID(), null, VALID_HIT_POINTS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combatant name cannot be null or blank");
        }

        @Test
        void rejectsBlankName() {
            assertThatThrownBy(() -> new Combatant(UUID.randomUUID(), "     ", VALID_HIT_POINTS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combatant name cannot be null or blank");
        }

        @Test
        void rejectsNullHitPoints() {
            assertThatThrownBy(() -> new Combatant(UUID.randomUUID(), "Fighter", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combatant hit points cannot be null");
        }

        @Test
        void validatesIdBeforeNameAndHitPoints() {
            assertThatThrownBy(() -> new Combatant(null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Combatant ID cannot be null");
        }

        @Test
        void validatesNameBeforeHitPoints() {
            assertThatThrownBy(() -> new Combatant(UUID.randomUUID(), null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combatant name cannot be null or blank");
        }
    }
}
