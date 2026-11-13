package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Arrays;
import java.util.List;

public class TurnQueue {
    private TurnEntry[] entries;
    private int capacity;
    private int size;
    private static final double LOAD_FACTOR = 0.75;

    public TurnQueue(){
        this.size = 0;
        this.capacity = 10;
        this.entries = new TurnEntry[capacity];
    }

    public void add(TurnEntry owner){
        if(owner == null){
            throw new IllegalArgumentException("");
        }

        if(size >= capacity * LOAD_FACTOR){
            autoPlus();
        }

        for(int i = 0; i < entries.length; i++){
            if(entries[i] == null){
                entries[i] = owner;
                size++;
                return;
            }

            containsCombatant(owner);

            if(entries[i].initiative() > owner.initiative()){
                continue;
            }
            else if(entries[i].initiative() < owner.initiative()){
                shiftArray(owner, i);
                return;
            } else if(entries[i].initiative() == owner.initiative()){
                shiftArray(owner, i + 1);
                return;
            }
        }
    }

    public void addAll(List<TurnEntry> entries){
        for (TurnEntry entry : entries) {
            this.add(entry);
        }
    }

    private void containsCombatant(TurnEntry turnEntry) {
        if (Arrays.stream(entries, 0, size)
                .anyMatch(entry -> entry.id().equals(turnEntry.id()))) {
            throw new IllegalArgumentException(
                    "Combatant already exists in turn queue: " + turnEntry.id()
            );
        }
    }

    private void shiftArray(TurnEntry owner, int position) {
        for(int j = size; j >= position; j--) {
            if(j == position){
                entries[j] = owner;
            } else{
                entries[j] = entries[j-1];
            }
        }
        size++;
    }

    private void autoPlus(){
        TurnEntry[] newArr = new TurnEntry[capacity *2];
        System.arraycopy(entries, 0, newArr, 0, entries.length);
        entries = newArr;
        capacity *= 2;
    }


}
