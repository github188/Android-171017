package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文本文件
 */

public class PdfDocument extends Document {
    public PdfDocument() {
        this(MimeType.Pdf);
    }

    public PdfDocument(int iconId) {
        super(iconId);
    }

    public PdfDocument(MimeType mimeType){
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

    protected PdfDocument(Parcel in) {
        super(in);
    }

    public static final Creator<PdfDocument> CREATOR = new Creator<PdfDocument>() {
        @Override
        public PdfDocument createFromParcel(Parcel source) {
            return new PdfDocument(source);
        }

        @Override
        public PdfDocument[] newArray(int size) {
            return new PdfDocument[size];
        }
    };
}
