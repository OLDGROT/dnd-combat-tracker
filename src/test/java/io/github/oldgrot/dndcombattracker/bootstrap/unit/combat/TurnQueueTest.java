package io.github.oldgrot.dndcombattracker.bootstrap.unit.combat;

import java.util.List;
import java.util.UUID;

import io.github.oldgrot.dndcombattracker.app.domain.TurnEntry;
import io.github.oldgrot.dndcombattracker.app.domain.TurnQueue;
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
            assertThatThrownBy(() -> new TurnEntry(null, 10))
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
        return new TurnEntry(id, initiative);
    }
}
