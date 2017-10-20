package com.maintainproduct.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class GDButton implements Parcelable {
	public String ID;
	public String Name;
	public String Type;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(ID);
		out.writeString(Name);
		out.writeString(Type);
	}

	public static final Parcelable.Creator<GDButton> CREATOR = new Parcelable.Creator<GDButton>() {
		@Override
		public GDButton createFromParcel(Parcel in) {
			return new GDButton(in);
		}

		@Override
		public GDButton[] newArray(int size) {
			return new GDButton[size];
		}
	};

	private GDButton(Parcel in) {
		ID = in.readString();
		Name = in.readString();
		Type = in.readString();
	}

}
