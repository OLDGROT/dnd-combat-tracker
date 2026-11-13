package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TurnQueue {
    private TurnEntry[] entries;
    private int capacity;
    private int size;
    private int turn_position;
    private long roundNumber;

    private static final double LOAD_FACTOR = 0.75;

    public TurnQueue() {
        this.size = 0;
        this.capacity = 10;
        this.entries = new TurnEntry[capacity];
        this.turn_position = 0;
        this.roundNumber = 1L;
    }

    public void add(TurnEntry owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Turn entry cannot be null");
        }

        if (size >= capacity * LOAD_FACTOR) {
            autoPlus();
        }

        ensureCombatantNotPresent(owner);

        for (int i = 0; i < size; i++) {
            if (entries[i].initiative() > owner.initiative()) {
                continue;
            } else if (entries[i].initiative() < owner.initiative()) {
                shiftArray(owner, i);
                return;
            } else {
                shiftArray(owner, i + 1);
                return;
            }
        }

        entries[size] = owner;
        size++;
    }

    public void addAll(List<TurnEntry> entries) {
        Objects.requireNonNull(entries, "Turn entries cannot be null");
        for (TurnEntry entry : entries) {
            this.add(entry);
        }
    }

    public UUID getTurnEntry(){
        return entries[turn_position].id();
    }

    public UUID getNextTurnEntry(){
         if(turn_position == size){
             turn_position = 0;
             roundNumber++;
             return getTurnEntry();
         }

        turn_position++;
        return getTurnEntry();
    }

    public List<TurnEntry> getEntries() {
        return List.copyOf(Arrays.asList(entries).subList(0, size));
    }

    private void ensureCombatantNotPresent(TurnEntry turnEntry) {
        if (Arrays.stream(entries, 0, size)
                .anyMatch(entry -> entry.id().equals(turnEntry.id()))) {
            throw new IllegalArgumentException(
                    "Combatant already exists in turn queue: " + turnEntry.id()
            );
        }
    }

    private void shiftArray(TurnEntry owner, int position) {
        for (int j = size; j >= position; j--) {
            if (j == position) {
                entries[j] = owner;
            } else {
                entries[j] = entries[j - 1];
            }
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
