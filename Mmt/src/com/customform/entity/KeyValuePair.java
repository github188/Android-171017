package com.customform.entity;

/**
 * Created by zoro at 2017/9/5.
 */
public class KeyValuePair<K, V> {
    public K key;
    public V value;//mao

    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
