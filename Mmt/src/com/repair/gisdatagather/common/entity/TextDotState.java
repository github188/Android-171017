package com.repair.gisdatagather.common.entity;

/**
 * Created by lyunfan on 16/11/25.
 */

public enum TextDotState {
    //对当前工程而言，已经不存在的
    ADD(0),
    //对当前工程而言，已经存在的
    EDIT(1),
    OHTER(2);//构造textline时，需要构造TextDot，other为此状态，无实际意义

    private int state;

    TextDotState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
