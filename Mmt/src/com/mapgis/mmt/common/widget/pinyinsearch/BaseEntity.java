package com.mapgis.mmt.common.widget.pinyinsearch;

public abstract class BaseEntity implements Cloneable {

    // field used for searching.
    private String keyField;

    public BaseEntity(String keyField) {
        this.keyField = keyField;
    }

    public String getKeyField() {
        return keyField;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
