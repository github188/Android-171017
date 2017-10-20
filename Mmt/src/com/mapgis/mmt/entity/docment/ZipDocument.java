package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文本文件
 */

public class ZipDocument extends Document {
    public ZipDocument() {
        this(MimeType.Zip);
    }

    public ZipDocument(int iconId) {
        super(iconId);
    }

    public ZipDocument(MimeType mimeType){
        super(mimeType);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected ZipDocument(Parcel in) {
        super(in);
    }

    public static final Creator<ZipDocument> CREATOR = new Creator<ZipDocument>() {
        @Override
        public ZipDocument createFromParcel(Parcel source) {
            return new ZipDocument(source);
        }

        @Override
        public ZipDocument[] newArray(int size) {
            return new ZipDocument[size];
        }
    };
}
