package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文本文件
 */

public class ExcelDocument extends Document {
    public ExcelDocument() {
        this(MimeType.Excel);
    }

    public ExcelDocument(int iconId) {
        super(iconId);
    }

    public ExcelDocument(MimeType mimeType){
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

    protected ExcelDocument(Parcel in) {
        super(in);
    }

    public static final Creator<ExcelDocument> CREATOR = new Creator<ExcelDocument>() {
        @Override
        public ExcelDocument createFromParcel(Parcel source) {
            return new ExcelDocument(source);
        }

        @Override
        public ExcelDocument[] newArray(int size) {
            return new ExcelDocument[size];
        }
    };
}
