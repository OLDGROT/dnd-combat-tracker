package io.github.oldgrot.dndcombattracker.app.domain;

import io.github.oldgrot.dndcombattracker.app.domain.exception.CombatantNotFound;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TurnQueue {
    private Combatant[] entries;
    private int capacity;
    private int size;
    private int turnPosition;
    private long roundNumber;

    private static final double LOAD_FACTOR = 0.75;

    public TurnQueue() {
        this.size = 0;
        this.capacity = 10;
        this.entries = new Combatant[capacity];
        this.turnPosition = -1;
        this.roundNumber = 1L;
    }

    public void add(Combatant owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Turn entry cannot be null");
        }

        if (size >= capacity * LOAD_FACTOR) {
            autoPlus();
        }

        ensureCombatantNotPresent(owner);


        for (int i = 0; i < size; i++) {
            if (entries[i].getInitiative() > owner.getInitiative()) {
                continue;
            }

            int position = entries[i].getInitiative() < owner.getInitiative() ? i : i + 1;
            shiftArray(owner, position);
            return;
        }

        entries[size] = owner;
        size++;
    }

    public void addAll(List<Combatant> entries) {
        Objects.requireNonNull(entries, "Turn entries cannot be null");
        for (Combatant entry : entries) {
            this.add(entry);
        }
    }

    public UUID getTurnEntry() {
        ensureQueueNotEmpty();
        turnPosition = Math.max(turnPosition, 0);
        return entries[turnPosition].getId();
    }

    public UUID getNextTurnEntry() {
        ensureQueueNotEmpty();
        turnPosition = Math.max(turnPosition, 0);
        entries[turnPosition].markTurnCompleted();

        for (int offset = 1; offset < size; offset++) {
            int candidatePosition = (turnPosition + offset) % size;
            if (!entries[candidatePosition].isHasActedThisRound()) {
                turnPosition = candidatePosition;
                return entries[turnPosition].getId();
            }
        }

        roundNumber++;
        reloadQueue();
        turnPosition = 0;
        return entries[turnPosition].getId();
    }

    public long getRoundNumber() {
        return roundNumber;
    }

    public List<Combatant> getEntries() {
        return List.copyOf(Arrays.asList(entries).subList(0, size));
    }

    private void ensureQueueNotEmpty() {
        if (size == 0) {
            throw new CombatantNotFound("Turn queue does not contain combatants");
        }
    }

    private void reloadQueue() {
        for (int i = 0; i < size; i++) {
            entries[i].reloadTurnCompleted();
        }
    }

    private void ensureCombatantNotPresent(Combatant turnEntry) {
        if (Arrays.stream(entries, 0, size)
                .anyMatch(entry -> entry.getId().equals(turnEntry.getId()))) {
            throw new IllegalArgumentException(
                    "Combatant already exists in turn queue: " + turnEntry.getId()
            );
        }
    }

    private void shiftArray(Combatant owner, int position) {
        for (int j = size; j > position; j--) {
            entries[j] = entries[j - 1];
        }
        entries[position] = owner;
        if (turnPosition >= 0 && position <= turnPosition) {
            turnPosition++;
        }
        size++;
    }

    private void autoPlus() {
        Combatant[] newArr = new Combatant[capacity * 2];
        System.arraycopy(entries, 0, newArr, 0, entries.length);
        entries = newArr;
        capacity *= 2;
    }
}
