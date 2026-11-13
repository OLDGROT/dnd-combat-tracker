package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.Arrays;

public class TurnQueue {
    private TurnEntry[] entries;
    private int capacity;
    private int size;
    private static final double KEF = 0.75;

    public TurnQueue(){
        this.size = 0;
        this.capacity = 10;
        this.entries = new TurnEntry[capacity];
    }

    public void add(TurnEntry owner){
        if(owner == null){
            throw new IllegalArgumentException("");
        }

        if(size >= capacity *KEF){
            autoPlus();
        }

        for(int i = 0; i < entries.length; i++){
            if(entries[i] == null){
                entries[i] = owner;
                size++;
                return;
            }

            if (Arrays.stream(entries, 0, size)
                    .anyMatch(entry -> entry.id().equals(owner.id()))) {
                throw new IllegalArgumentException(
                        "Combatant already exists in turn queue: " + owner.id()
                );
            }

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

    private void shiftArray(TurnEntry owner, int position) {
        TurnEntry temp = owner;
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
