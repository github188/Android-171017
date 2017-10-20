package com.mapgis.mmt.entity.docment;

import android.os.Parcel;

/**
 * Created by Comclay on 2016/12/15.
 * 音频文件
 */

public class AudioDocument extends Document {
    public AudioDocument() {
        this(MimeType.Audio);
    }

    public AudioDocument(int iconId){
        super(iconId);
    }

    public AudioDocument(MimeType audio) {
        super(audio);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected AudioDocument(Parcel in) {
        super(in);
    }

    public static final Creator<AudioDocument> CREATOR = new Creator<AudioDocument>() {
        @Override
        public AudioDocument createFromParcel(Parcel source) {
            return new AudioDocument(source);
        }

        @Override
        public AudioDocument[] newArray(int size) {
            return new AudioDocument[size];
        }
    };
}
