package com.mapgis.mmt.entity;

import java.io.Serializable;

public class LevelItemBase implements Serializable {
    public int id = 0;
    public String name = "";

    public LevelItemBase() {
    }

    public LevelItemBase(String name) {
        this.name = name;
    }

    public LevelItemBase(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LevelItemBase)) {
            return false;
        }
        LevelItemBase levelItem = (LevelItemBase) o;
        return levelItem.id == id && levelItem.name.equals(name);
    }
}
