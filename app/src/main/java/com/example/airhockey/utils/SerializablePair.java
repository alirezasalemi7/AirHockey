package com.example.airhockey.utils;

import java.io.Serializable;

public class SerializablePair<T,V> implements Serializable {

    public T first;
    public V second;

    public SerializablePair(T first,V second){
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "SerializablePair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
