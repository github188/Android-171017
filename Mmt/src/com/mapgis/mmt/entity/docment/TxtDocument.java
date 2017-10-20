package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文本文件
 */

public class TxtDocument extends Document {
    public TxtDocument() {
        this(MimeType.Txt);
    }

    public TxtDocument(int iconId) {
        super(iconId);
    }

    public TxtDocument(MimeType txt) {
        super(txt);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected TxtDocument(Parcel in) {
        super(in);
    }

    public static final Creator<TxtDocument> CREATOR = new Creator<TxtDocument>() {
        @Override
        public TxtDocument createFromParcel(Parcel source) {
            return new TxtDocument(source);
        }

        @Override
        public TxtDocument[] newArray(int size) {
            return new TxtDocument[size];
        }
    };
}
