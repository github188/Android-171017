package com.mapgis.mmt.global;

public interface Reporter<T> {
    void report(T... t);
}
