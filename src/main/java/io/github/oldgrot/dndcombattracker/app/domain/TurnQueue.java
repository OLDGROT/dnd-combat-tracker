package io.github.oldgrot.dndcombattracker.app.domain;

import org.springframework.dao.DuplicateKeyException;

import java.util.Objects;

public class TurnQueue<T extends Combatant> {
    private TurnEntry[] deque;
    private int capacity;
    private int size;
    private double KEF = 0.75;

    public TurnQueue(){
        this.size = 0;
        this.capacity = 10;
        this.deque = new TurnEntry[capacity];
    }

    public void add(TurnEntry owner){
        if(owner == null){
            throw new IllegalArgumentException("");
        }

        if(size >= capacity *KEF){
            autoPlus();
        }

        for(int i = 0; i < deque.length; i++){
            if(deque[i] == null){
                deque[i] = owner;
                size++;
                return;
            }

            if(Objects.equals(deque[i].id(),owner.id())){
                throw new IllegalArgumentException("");
            }

            if(deque[i].initiative() > owner.initiative()){
                continue;
            }
            else if(deque[i].initiative() < owner.initiative()){
                shiftArray(owner, i);
                return;
            } else if(deque[i].initiative() == owner.initiative()){
                shiftArray(owner, i + 1);
                return;
            }
        }
    }

    private void shiftArray(TurnEntry owner, int position) {
        TurnEntry temp = owner;
        for(int j = size; j <= position; j--) {
            if(j == position){
                deque[j] = owner;
            } else{
                deque[j] = deque[j-1];
            }
        }
        size++;
    }

    private void autoPlus(){
        TurnEntry[] newArr = new TurnEntry[capacity *2];
        System.arraycopy(deque, 0, newArr, 0, deque.length);
        deque = newArr;
        capacity *= 2;
    }


}
