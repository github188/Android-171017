package com.mapgis.mmt.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class LevelItem extends LevelItemBase implements Nullable, Parcelable {
    public boolean isSingle;
    public boolean isChecked;

    public LevelItemBase parent;
    public ArrayList<LevelItem> children;

    public LevelItem() {
        this(-1, "");
    }

    public LevelItem(String name) {
        this(-1, name);
    }

    public LevelItem(int id, String name) {
        super(id, name);

        this.isSingle = true;
        this.children = new ArrayList<>();
    }

    protected LevelItem(Parcel in) {
        isSingle = in.readByte() != 0;
        isChecked = in.readByte() != 0;
        children = in.createTypedArrayList(LevelItem.CREATOR);
    }

    public static final Creator<LevelItem> CREATOR = new Creator<LevelItem>() {
        @Override
        public LevelItem createFromParcel(Parcel in) {
            return new LevelItem(in);
        }

        @Override
        public LevelItem[] newArray(int size) {
            return new LevelItem[size];
        }
    };

    @Override
    public String toString() {
        String result = "{" + name + ":";

        for (LevelItem li : children) {
            if (!li.isChecked)
                continue;

            result += li.name + ",";
        }

        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        return result + "}";
    }

    public LevelItem getCheckedItem() {
        for (LevelItem li : children) {
            if (li.isChecked)
                return li;
        }

        return new NullLevelItem();
    }

    public void setCheckedItem(String name) {
        for (LevelItem item : this.children) {
            if (item.name.equals(name)) {
                item.isChecked = true;
            } else if (isSingle || name.equals("全部") || item.name.equals("全部")) {
                item.isChecked = false;
            }
        }
    }

    public void setUnChecked() {
        this.isChecked = false;

        if (this.children != null && this.children.size() > 0) {
            for (LevelItem li : this.children)
                li.isChecked = false;
        }
    }

    public boolean isNull() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSingle ? 1 : 0));
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeTypedList(children);
    }

    private class NullLevelItem extends LevelItem {
        public NullLevelItem() {
            super();
        }

        @Override
        public boolean isNull() {
            return true;
        }
    }
}
