package io.github.oldgrot.dndcombattracker.app.domain;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class TurnQueue {
    private TurnEntry[] entries;
    private int capacity;
    private int size;
    private int turnPosition;
    private long roundNumber;

    private static final double LOAD_FACTOR = 0.75;

    public TurnQueue() {
        this.size = 0;
        this.capacity = 10;
        this.entries = new TurnEntry[capacity];
        this.turnPosition = 0;
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
        saveTurnPosition(size);
    }

    public void addAll(List<TurnEntry> entries) {
        Objects.requireNonNull(entries, "Turn entries cannot be null");
        for (TurnEntry entry : entries) {
            this.add(entry);
        }
    }

    public UUID getTurnEntry(){
        if(entries[turnPosition] == null){
            return null;
        }
        return entries[turnPosition].combatantId();
    }

    public UUID getNextTurnEntry(){
         if(turnPosition == size ){
             turnPosition = 0;
             roundNumber++;
             reloadQueue();
             return getTurnEntry();
         }

        turnPosition++;
        return getTurnEntry();
    }

    private void reloadQueue() {
        for(int i = 0; i < size; i++){
            TurnEntry updated = new TurnEntry(
                    entries[i].combatantId(),
                    entries[i].initiative(),
                    false
            );
            entries[i]= updated;
        }
    }

    public List<TurnEntry> getEntries() {
        return List.copyOf(Arrays.asList(entries).subList(0, size));
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
        for (int j = size; j >= position; j--) {
            if (j == position) {
                entries[j] = owner;
                saveTurnPosition(j);
            } else {
                entries[j] = entries[j - 1];
            }
        }

    }

    private void saveTurnPosition(int position){
        if(turnPosition >= position){
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
