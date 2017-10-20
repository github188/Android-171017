package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 未知文件类型的文件
 */

public class UnknowDocument extends Document {
    public UnknowDocument() {
        this(MimeType.Unknow);
    }

    public UnknowDocument(int iconId){
        super(iconId);
    }

    public UnknowDocument(MimeType unknow) {
        super(unknow);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected UnknowDocument(Parcel in) {
        super(in);
    }

    public static final Creator<UnknowDocument> CREATOR = new Creator<UnknowDocument>() {
        @Override
        public UnknowDocument createFromParcel(Parcel source) {
            return new UnknowDocument(source);
        }

        @Override
        public UnknowDocument[] newArray(int size) {
            return new UnknowDocument[size];
        }
    };
}
