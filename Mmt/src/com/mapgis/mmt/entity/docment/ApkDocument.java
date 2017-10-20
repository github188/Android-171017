package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * apk文件对象
 */

public class ApkDocument extends Document {
    public ApkDocument() {
        this(MimeType.Apk);
    }

    public ApkDocument(int iconId) {
        super(iconId);
    }

    public ApkDocument(MimeType mimeType){
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

    protected ApkDocument(Parcel in) {
        super(in);
    }

    public static final Creator<ApkDocument> CREATOR = new Creator<ApkDocument>() {
        @Override
        public ApkDocument createFromParcel(Parcel source) {
            return new ApkDocument(source);
        }

        @Override
        public ApkDocument[] newArray(int size) {
            return new ApkDocument[size];
        }
    };
}
