package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文本文件
 */

public class DocDocument extends Document {
    public DocDocument() {
        this(MimeType.Doc);
    }

    public DocDocument(int iconId) {
        super(iconId);
    }

    public DocDocument(MimeType mimeType){
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

    protected DocDocument(Parcel in) {
        super(in);
    }

    public static final Creator<DocDocument> CREATOR = new Creator<DocDocument>() {
        @Override
        public DocDocument createFromParcel(Parcel source) {
            return new DocDocument(source);
        }

        @Override
        public DocDocument[] newArray(int size) {
            return new DocDocument[size];
        }
    };
}
