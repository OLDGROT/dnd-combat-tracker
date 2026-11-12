package io.github.oldgrot.dndcombattracker.app.domain;

import java.util.ArrayList;
import java.util.Set;

public class TurnQueue<T extends Combatant> {
    private T[] deque;
    private int size;
    private int capacity;
    private double KEF = 0.75;

    public TurnQueue(){
        this.capacity = 0;
        this.size = 10;
        this.deque = (T[]) new Object[size];
    }

    public void add(T owner){
        if(owner == null){
            throw new IllegalArgumentException("");
        }

        if(capacity >= size*KEF){
            autoPlus();
        }

        for(int i = 0; i < deque.length; i++){
            if(deque[i] == null){
                deque[i] = owner;
                return;
            }

            if(deque[i].getInitiative() > owner.getInitiative()){
                continue;
            }
            else if(deque[i].getInitiative() < owner.getInitiative()){

            }
        }
    }

    private void autoPlus(){
        T[] newArr = (T[])new Object[size*2];
        if (deque.length >= 0) System.arraycopy(deque, 0, newArr, 0, deque.length);
        deque = newArr;
    }


}
