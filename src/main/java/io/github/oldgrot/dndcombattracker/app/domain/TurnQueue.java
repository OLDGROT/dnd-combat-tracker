package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.exception.CombatantNotFound;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TurnQueue {
    private TurnEntry[] entries;
    private int capacity;
    private int size;
    private int turnPosition;
    private long roundNumber;
    private boolean turnStarted;

    private static final double LOAD_FACTOR = 0.75;

    public TurnQueue() {
        this.size = 0;
        this.capacity = 10;
        this.entries = new TurnEntry[capacity];
        this.turnPosition = 0;
        this.roundNumber = 1L;
        this.turnStarted = false;
    }

    public void add(TurnEntry owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Turn entry cannot be null");
        }

        if (size >= capacity * LOAD_FACTOR) {
            autoPlus();
        }

        ensureCombatantNotPresent(owner);
        TurnEntry entryToAdd = turnStarted ? owner.markTurnCompleted() : owner;

        for (int i = 0; i < size; i++) {
            if (entries[i].initiative() > entryToAdd.initiative()) {
                continue;
            }

            int position = entries[i].initiative() < entryToAdd.initiative() ? i : i + 1;
            shiftArray(entryToAdd, position);
            return;
        }

        entries[size] = entryToAdd;
        size++;
    }

    public void addAll(List<TurnEntry> entries) {
        Objects.requireNonNull(entries, "Turn entries cannot be null");
        for (TurnEntry entry : entries) {
            this.add(entry);
        }
    }

    public UUID getTurnEntry() {
        ensureQueueNotEmpty();
        turnStarted = true;
        return entries[turnPosition].combatantId();
    }

    public UUID getNextTurnEntry() {
        ensureQueueNotEmpty();
        turnStarted = true;
        entries[turnPosition] = entries[turnPosition].markTurnCompleted();

        for (int offset = 1; offset < size; offset++) {
            int candidatePosition = (turnPosition + offset) % size;
            if (!entries[candidatePosition].hasActedThisRound()) {
                turnPosition = candidatePosition;
                return entries[turnPosition].combatantId();
            }
        }

        roundNumber++;
        reloadQueue();
        turnPosition = 0;
        return entries[turnPosition].combatantId();
    }

    public long getRoundNumber() {
        return roundNumber;
    }

    public List<TurnEntry> getEntries() {
        return List.copyOf(Arrays.asList(entries).subList(0, size));
    }

    private void ensureQueueNotEmpty() {
        if (size == 0) {
            throw new CombatantNotFound("Turn queue does not contain combatants");
        }
    }

    private void reloadQueue() {
        for (int i = 0; i < size; i++) {
            entries[i] = new TurnEntry(
                    entries[i].combatantId(),
                    entries[i].initiative(),
                    false
            );
        }
    }

    private void ensureCombatantNotPresent(TurnEntry turnEntry) {
        if (Arrays.stream(entries, 0, size)
                .anyMatch(entry -> entry.combatantId().equals(turnEntry.combatantId()))) {
            throw new IllegalArgumentException(
                    "Combatant already exists in turn queue: " + turnEntry.combatantId()
            );
        }
    }

    private void shiftArray(TurnEntry owner, int position) {
        for (int j = size; j > position; j--) {
            entries[j] = entries[j - 1];
        }
        entries[position] = owner;
        if (turnStarted && position <= turnPosition) {
            turnPosition++;
        }
        size++;
    }

    private void autoPlus() {
        TurnEntry[] newArr = new TurnEntry[capacity * 2];
        System.arraycopy(entries, 0, newArr, 0, entries.length);
        entries = newArr;
        capacity *= 2;
    }
}
