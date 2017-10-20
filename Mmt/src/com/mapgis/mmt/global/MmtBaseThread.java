package com.mapgis.mmt.global;

import com.mapgis.mmt.AppManager;

public class MmtBaseThread extends Thread {
    protected volatile boolean isExit = false;

    @Override
    public synchronized void start() {
        if (getState() != State.NEW) {
            return;
        }

        isExit = false;
        this.setName(this.getClass().getName());

        super.start();

        AppManager.addThread(this);
    }

    public void abort() {
        isExit = true;

        this.interrupt();
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
