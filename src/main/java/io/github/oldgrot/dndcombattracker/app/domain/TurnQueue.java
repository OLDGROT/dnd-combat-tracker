package io.github.oldgrot.dndcombattracker.app.domain;

import org.springframework.dao.DuplicateKeyException;

public class TurnQueue<T extends Combatant> {
    private CombatantDto[] deque;
    private int capacity;
    private int size;
    private double KEF = 0.75;

    public TurnQueue(){
        this.size = 0;
        this.capacity = 10;
        this.deque = new CombatantDto[capacity];
    }

    public void add(CombatantDto owner){
        if(owner == null){
            throw new IllegalArgumentException("");
        }

        if(size >= capacity *KEF){
            autoPlus();
        }

        for(int i = 0; i < deque.length; i++){
            if(owner.id() == deque[i].id()){
                throw new DuplicateKeyException("");
            }
            if(deque[i] == null){
                deque[i] = owner;
                size++;
                return;
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

    private void shiftArray(CombatantDto owner, int position) {
        CombatantDto temp = owner.copyOf();
        for(int j = position; j < deque.length + 1; j++) {
            deque[j] = temp;
            temp = deque[j +1];
        }
        size++;
    }

    private void autoPlus(){
        CombatantDto[] newArr = new CombatantDto[capacity *2];
        System.arraycopy(deque, 0, newArr, 0, deque.length);
        deque = newArr;
        capacity *= 2;
    }


}
