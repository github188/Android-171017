package com.repair.gisdatagather.common.entity;

/**
 * Created by lyunfan on 16/11/25.
 */

public enum TextLineState {
    ADD(0),
    EDIT(1);
    private int state;

    TextLineState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
