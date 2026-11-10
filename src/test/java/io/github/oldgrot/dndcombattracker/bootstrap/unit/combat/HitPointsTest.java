package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import io.github.oldgrot.dndcombattracker.app.domain.HitPoints;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HitPointsTest {

    @Nested
    class Creation {

        @ParameterizedTest(name = "создаёт HitPoints: max={0}, current={1}, temporary={2}")
        @CsvSource({
                "1, 0, 0",
                "1, 1, 0",
                "10, 5, 3",
                "2147483647, 2147483647, 2147483647"
        })
        void happyPathCreatesHitPoints(int max, int current, int temporary) {
            HitPoints hitPoints = new HitPoints(max, current, temporary);

            assertThat(hitPoints.max()).isEqualTo(max);
            assertThat(hitPoints.current()).isEqualTo(current);
            assertThat(hitPoints.temporary()).isEqualTo(temporary);
        }

        @ParameterizedTest(name = "отклоняет неположительный max HP: {0}")
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void rejectsNonPositiveMaxHp(int max) {
            assertThatThrownBy(() -> new HitPoints(max, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Max HP must be greater than zero");
        }

        @Test
        void rejectsNegativeCurrentHp() {
            assertThatThrownBy(() -> new HitPoints(10, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current HP must be between zero and max HP");
        }

        @Test
        void rejectsCurrentHpAboveMaxHp() {
            assertThatThrownBy(() -> new HitPoints(10, 11, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current HP must be between zero and max HP");
        }

        @Test
        void rejectsNegativeTemporaryHp() {
            assertThatThrownBy(() -> new HitPoints(10, 10, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Temporary HP cannot be negative");
        }

        @Test
        void validatesMaxBeforeCurrentAndTemporaryHp() {
            assertThatThrownBy(() -> new HitPoints(0, -1, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Max HP must be greater than zero");
        }

        @Test
        void validatesCurrentBeforeTemporaryHp() {
            assertThatThrownBy(() -> new HitPoints(10, -1, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current HP must be between zero and max HP");
        }
    }
}
