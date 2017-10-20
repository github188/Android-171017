package com.mapgis.mmt.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageKeyValuePair extends KeyValuePair {
    public int image;

    public ImageKeyValuePair() {
    }

    public ImageKeyValuePair(int image, String key, String value) {
        super(key, value);

        this.image = image;
    }

    public ImageKeyValuePair(Parcel source) {
        super(source);
        this.image = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(image);
    }

    public static final Parcelable.Creator<ImageKeyValuePair> CREATOR = new Creator<ImageKeyValuePair>() {

        @Override
        public ImageKeyValuePair createFromParcel(Parcel source) {
            return new ImageKeyValuePair(source);
        }

        @Override
        public ImageKeyValuePair[] newArray(int size) {
            return new ImageKeyValuePair[size];
        }
    };
}
