package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 文件对象
 */

public class FileDocument extends Document {
    public FileDocument(){
        this(MimeType.File);
    }

    public FileDocument(MimeType mimeType){
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

    protected FileDocument(Parcel in) {
        super(in);
    }

    public static final Creator<FileDocument> CREATOR = new Creator<FileDocument>() {
        @Override
        public FileDocument createFromParcel(Parcel source) {
            return new FileDocument(source);
        }

        @Override
        public FileDocument[] newArray(int size) {
            return new FileDocument[size];
        }
    };
}
