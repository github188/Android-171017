package com.repair.zhoushan.module.devicecare.consumables;

import com.mapgis.mmt.common.widget.pinyinsearch.BaseEntity;

public class CharacterEntity extends BaseEntity implements Cloneable {

    private final String key;

    public CharacterEntity(String key) {
        super(key);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
