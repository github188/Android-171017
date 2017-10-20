package com.mapgis.mmt.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class KeyValuePair implements Parcelable {
	public String Key;
	public String Value;

	public KeyValuePair() {
	}

	public KeyValuePair(String key, String value) {
		this.Key = key;
		this.Value = value;
	}

	public KeyValuePair(Parcel source) {
		this.Key = source.readString();
		this.Value = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(Key);
		dest.writeString(Value);
	}

	public static final Parcelable.Creator<KeyValuePair> CREATOR = new Creator<KeyValuePair>() {

		@Override
		public KeyValuePair createFromParcel(Parcel source) {
			return new KeyValuePair(source);
		}

		@Override
		public KeyValuePair[] newArray(int size) {
			return new KeyValuePair[size];
		}
	};
}