package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.List;
import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.TurnEntry;
import io.github.oldgrot.dndcombattracker.app.domain.TurnQueue;
import io.github.oldgrot.dndcombattracker.app.domain.exception.CombatantNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TurnQueue")
class TurnQueueTest {

    private static final UUID FIRST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID THIRD_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Nested
    @DisplayName("Добавление участников")
    class AddingEntries {

        @Test
        @DisplayName("добавляет первую запись в пустую очередь")
        void addsFirstEntryToEmptyQueue() {
            TurnQueue queue = new TurnQueue();
            TurnEntry entry = entry(FIRST_ID, 10);

            queue.add(entry);

            assertThat(queue.getEntries()).containsExactly(entry);
        }

        @Test
        @DisplayName("сортирует записи по инициативе от большей к меньшей")
        void sortsEntriesByInitiativeDescending() {
            TurnQueue queue = new TurnQueue();
            TurnEntry middle = entry(FIRST_ID, 10);
            TurnEntry lowest = entry(SECOND_ID, -5);
            TurnEntry highest = entry(THIRD_ID, 25);

            queue.add(middle);
            queue.add(lowest);
            queue.add(highest);

            assertThat(queue.getEntries()).containsExactly(highest, middle, lowest);
        }

        @Test
        @DisplayName("сортирует граничные значения инициативы")
        void sortsMinimumAndMaximumInitiativeValues() {
            TurnQueue queue = new TurnQueue();
            TurnEntry minimum = entry(FIRST_ID, Integer.MIN_VALUE);
            TurnEntry maximum = entry(SECOND_ID, Integer.MAX_VALUE);

            queue.add(minimum);
            queue.add(maximum);

            assertThat(queue.getEntries()).containsExactly(maximum, minimum);
            assertThat(queue.getTurnEntry()).isEqualTo(SECOND_ID);
        }

        @Test
        @DisplayName("добавляет запись, когда внутренний массив содержит объекты и null-ячейки")
        void addsWhenBackingArrayContainsEntriesAndNullSlots() {
            TurnQueue queue = new TurnQueue();
            TurnEntry first = entry(FIRST_ID, 20);
            TurnEntry second = entry(SECOND_ID, 10);

            queue.add(first);
            queue.add(second);

            assertThat(queue.getEntries()).containsExactly(first, second);
        }

        @Test
        @DisplayName("при равной инициативе вставляет новую запись после первой совпавшей")
        void insertsEqualInitiativeAfterFirstMatchingEntry() {
            TurnQueue queue = new TurnQueue();
            TurnEntry first = entry(FIRST_ID, 18);
            TurnEntry second = entry(SECOND_ID, 18);
            TurnEntry third = entry(THIRD_ID, 18);

            queue.add(first);
            queue.add(second);
            queue.add(third);

            assertThat(queue.getEntries()).containsExactly(first, third, second);
        }

        @Test
        @DisplayName("расширяет внутренний массив и сохраняет порядок")
        void growsBackingArrayAndPreservesOrder() {
            TurnQueue queue = new TurnQueue();

            for (int initiative = 1; initiative <= 9; initiative++) {
                queue.add(entry(new UUID(0, initiative), initiative));
            }

            assertThat(queue.getEntries())
                    .extracting(TurnEntry::initiative)
                    .containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("возвращает неизменяемый снимок очереди")
        void returnsImmutableQueueSnapshot() {
            TurnQueue queue = new TurnQueue();
            queue.add(entry(FIRST_ID, 10));

            assertThatThrownBy(() -> queue.getEntries().add(entry(SECOND_ID, 5)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Переход хода и раунда")
    class AdvancingTurnsAndRounds {

        @Test
        @DisplayName("начинает первый раунд с участника с максимальной инициативой")
        void startsFirstRoundWithHighestInitiative() {
            TurnQueue queue = new TurnQueue();
            queue.add(entry(FIRST_ID, 10));
            queue.add(entry(SECOND_ID, 20));

            assertThat(queue.getRoundNumber()).isOne();
            assertThat(queue.getTurnEntry()).isEqualTo(SECOND_ID);
        }

        @Test
        @DisplayName("последовательно передаёт ход всем участникам")
        void advancesThroughAllEntriesInInitiativeOrder() {
            TurnQueue queue = queueWithThreeEntries();

            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(SECOND_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(THIRD_ID);
            assertThat(queue.getRoundNumber()).isOne();
        }

        @Test
        @DisplayName("после последнего участника начинает новый раунд с начала очереди")
        void startsNextRoundAfterLastEntry() {
            TurnQueue queue = queueWithThreeEntries();
            queue.getTurnEntry();
            queue.getNextTurnEntry();
            queue.getNextTurnEntry();

            UUID nextRoundEntry = queue.getNextTurnEntry();

            assertThat(nextRoundEntry).isEqualTo(FIRST_ID);
            assertThat(queue.getRoundNumber()).isEqualTo(2);
            assertThat(queue.getEntries())
                    .extracting(TurnEntry::hasActedThisRound)
                    .containsOnly(false);
        }

        @Test
        @DisplayName("для единственного участника каждый следующий ход начинает новый раунд")
        void advancesRoundForSingleEntryQueue() {
            TurnQueue queue = new TurnQueue();
            queue.add(entry(FIRST_ID, 10));

            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getRoundNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("отмечает завершённый ход до перехода к следующему участнику")
        void marksCurrentEntryAsActedBeforeAdvancing() {
            TurnQueue queue = queueWithThreeEntries();
            queue.getTurnEntry();

            queue.getNextTurnEntry();

            assertThat(queue.getEntries())
                    .extracting(TurnEntry::hasActedThisRound)
                    .containsExactly(true, false, false);
        }

        @Test
        @DisplayName("откладывает добавленных во время раунда участников до следующего раунда")
        void defersEntriesAddedDuringRoundUntilNextRound() {
            TurnQueue queue = new TurnQueue();
            queue.add(entry(FIRST_ID, 20));
            queue.add(entry(SECOND_ID, 10));
            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);

            UUID higherNewId = UUID.fromString("00000000-0000-0000-0000-000000000004");
            UUID lowerNewId = UUID.fromString("00000000-0000-0000-0000-000000000005");
            queue.add(entry(higherNewId, 30));
            queue.add(entry(lowerNewId, 5));

            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(SECOND_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(higherNewId);
            assertThat(queue.getRoundNumber()).isEqualTo(2);
            assertThat(queue.getEntries())
                    .extracting(TurnEntry::combatantId)
                    .containsExactly(higherNewId, FIRST_ID, SECOND_ID, lowerNewId);
        }

        @Test
        @DisplayName("сохраняет текущий ход при вставке участника после текущей позиции")
        void preservesCurrentTurnWhenAddingEntryAfterCurrentPosition() {
            TurnQueue queue = new TurnQueue();
            queue.add(entry(FIRST_ID, 20));
            queue.add(entry(SECOND_ID, 10));
            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);

            queue.add(entry(THIRD_ID, 15));

            assertThat(queue.getTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(SECOND_ID);
            assertThat(queue.getNextTurnEntry()).isEqualTo(FIRST_ID);
            assertThat(queue.getRoundNumber()).isEqualTo(2);
            assertThat(queue.getEntries())
                    .extracting(TurnEntry::combatantId)
                    .containsExactly(FIRST_ID, THIRD_ID, SECOND_ID);
        }

        @Test
        @DisplayName("отклоняет получение текущего участника из пустой очереди")
        void rejectsCurrentEntryLookupInEmptyQueue() {
            TurnQueue queue = new TurnQueue();

            assertThatThrownBy(queue::getTurnEntry)
                    .isInstanceOf(CombatantNotFound.class)
                    .hasMessage("Turn queue does not contain combatants");
        }

        @Test
        @DisplayName("отклоняет переход хода в пустой очереди")
        void rejectsTurnAdvanceInEmptyQueue() {
            TurnQueue queue = new TurnQueue();

            assertThatThrownBy(queue::getNextTurnEntry)
                    .isInstanceOf(CombatantNotFound.class)
                    .hasMessage("Turn queue does not contain combatants");
        }
    }

    @Nested
    @DisplayName("Состояние TurnEntry")
    class TurnEntryState {

        @Test
        @DisplayName("создаёт новую запись с отметкой о завершённом ходе")
        void marksTurnAsCompletedWithoutChangingIdentityOrInitiative() {
            TurnEntry original = entry(FIRST_ID, 15);

            TurnEntry completed = original.markTurnCompleted();

            assertThat(completed).isNotSameAs(original);
            assertThat(completed.combatantId()).isEqualTo(FIRST_ID);
            assertThat(completed.initiative()).isEqualTo(15);
            assertThat(completed.hasActedThisRound()).isTrue();
            assertThat(original.hasActedThisRound()).isFalse();
        }
    }

    @Nested
    @DisplayName("Пакетное добавление")
    class AddingAllEntries {

        @Test
        @DisplayName("добавляет список записей и сортирует его по инициативе")
        void addsAllEntriesAndSortsByInitiative() {
            TurnQueue queue = new TurnQueue();
            TurnEntry lowest = entry(FIRST_ID, 5);
            TurnEntry highest = entry(SECOND_ID, 20);
            TurnEntry middle = entry(THIRD_ID, 10);

            queue.addAll(List.of(lowest, highest, middle));

            assertThat(queue.getEntries()).containsExactly(highest, middle, lowest);
        }

        @Test
        @DisplayName("принимает пустой список без изменения очереди")
        void acceptsEmptyListWithoutChangingQueue() {
            TurnQueue queue = new TurnQueue();

            queue.addAll(List.of());

            assertThat(queue.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("отклоняет null вместо списка")
        void rejectsNullList() {
            TurnQueue queue = new TurnQueue();

            assertThatThrownBy(() -> queue.addAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Turn entries cannot be null");
        }

        @Test
        @DisplayName("отклоняет null среди обычных записей и сохраняет добавленные ранее")
        void rejectsNullAmongNonNullEntries() {
            TurnQueue queue = new TurnQueue();
            TurnEntry first = entry(FIRST_ID, 20);
            TurnEntry notProcessed = entry(SECOND_ID, 10);

            assertThatThrownBy(() -> queue.addAll(java.util.Arrays.asList(first, null, notProcessed)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Turn entry cannot be null");
            assertThat(queue.getEntries()).containsExactly(first);
        }

        @Test
        @DisplayName("отклоняет совпадающий ID среди обычных записей и сохраняет добавленные ранее")
        void rejectsDuplicateIdAmongNonDuplicateEntries() {
            TurnQueue queue = new TurnQueue();
            TurnEntry first = entry(FIRST_ID, 20);
            TurnEntry second = entry(SECOND_ID, 10);
            TurnEntry duplicateFirst = entry(FIRST_ID, 30);

            assertThatThrownBy(() -> queue.addAll(List.of(first, second, duplicateFirst)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combatant already exists in turn queue: " + FIRST_ID);
            assertThat(queue.getEntries()).containsExactly(first, second);
        }
    }

    @Nested
    @DisplayName("Ошибки добавления")
    class AddingErrors {

        @Test
        @DisplayName("отклоняет null вместо записи")
        void rejectsNullEntry() {
            TurnQueue queue = new TurnQueue();

            assertThatThrownBy(() -> queue.add(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Turn entry cannot be null");
        }

        @Test
        @DisplayName("отклоняет null ID записи")
        void rejectsNullEntryId() {
            assertThatThrownBy(() -> new TurnEntry(null, 10, false))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Turn entry ID cannot be null");
        }

        @Test
        @DisplayName("находит повторяющийся ID среди совпадающих и несовпадающих записей")
        void rejectsDuplicateAmongMatchingAndNonMatchingIds() {
            TurnQueue queue = new TurnQueue();
            TurnEntry first = entry(FIRST_ID, 20);
            TurnEntry second = entry(SECOND_ID, 10);
            TurnEntry duplicateSecond = entry(SECOND_ID, 30);
            queue.add(first);
            queue.add(second);

            assertThatThrownBy(() -> queue.add(duplicateSecond))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Combatant already exists in turn queue: " + SECOND_ID);
            assertThat(queue.getEntries()).containsExactly(first, second);
        }
    }


    private TurnEntry entry(UUID id, int initiative) {
        return new TurnEntry(id, initiative, false);
    }

    private TurnQueue queueWithThreeEntries() {
        TurnQueue queue = new TurnQueue();
        queue.add(entry(THIRD_ID, 5));
        queue.add(entry(FIRST_ID, 20));
        queue.add(entry(SECOND_ID, 10));
        return queue;
    }
}
