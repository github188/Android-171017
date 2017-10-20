package com.maintainproduct.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class GDGroup implements Parcelable {
	public String Name;
	public String Icon;
	public String Url;

	public GDControl[] Controls;

	public GDGroup() {
	}

	/** 根据Control的Name属性查找指定的Control */
	public GDControl findControlByControlName(String name) {
		if (Controls != null) {
			for (GDControl control : Controls) {
				if (control.Name.equals(name)) {
					return control;
				}
			}
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(Name);
		out.writeString(Icon);
		out.writeString(Url);
		out.writeParcelableArray(Controls, flags);
	}

	public static final Parcelable.Creator<GDGroup> CREATOR = new Parcelable.Creator<GDGroup>() {
		@Override
		public GDGroup createFromParcel(Parcel in) {
			return new GDGroup(in);
		}

		@Override
		public GDGroup[] newArray(int size) {
			return new GDGroup[size];
		}
	};

	private GDGroup(Parcel in) {
		Name = in.readString();
		Icon = in.readString();
		Url = in.readString();
		Parcelable[] pars = in.readParcelableArray(GDControl.class.getClassLoader());
		Controls = Arrays.asList(pars).toArray(new GDControl[pars.length]);
	}
}
