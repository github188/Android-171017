package com.repair.zhoushan.module;

import android.widget.BaseAdapter;

public abstract class SimpleBaseAdapter extends BaseAdapter {

    private int lastClickPos = 0;

    public int getLastClickPos() {
        return lastClickPos;
    }

    public void onItemClick(int position) {
        this.lastClickPos = position;
    }
}

