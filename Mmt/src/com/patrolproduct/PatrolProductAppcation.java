package com.patrolproduct;

import com.mapgis.mmt.MyApplication;

public class PatrolProductAppcation extends MyApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static PatrolProductAppcation getInstance() {
        return (PatrolProductAppcation) instance;
    }
}