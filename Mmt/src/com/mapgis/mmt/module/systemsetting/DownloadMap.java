package com.mapgis.mmt.module.systemsetting;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadMap implements Parcelable {
	public String MapName;
	public String ServerTime;
	public boolean hasNew;

	public DownloadMap(){

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.MapName);
		dest.writeString(this.ServerTime);
		dest.writeByte(this.hasNew ? (byte) 1 : (byte) 0);
	}

	protected DownloadMap(Parcel in) {
		this.MapName = in.readString();
		this.ServerTime = in.readString();
		this.hasNew = in.readByte() != 0;
	}

	public static final Creator<DownloadMap> CREATOR = new Creator<DownloadMap>() {
		@Override
		public DownloadMap createFromParcel(Parcel source) {
			return new DownloadMap(source);
		}

		@Override
		public DownloadMap[] newArray(int size) {
			return new DownloadMap[size];
		}
	};

	@Override
	public String toString() {
		return "DownloadMap{" +
				"MapName='" + MapName + '\'' +
				", ServerTime='" + ServerTime + '\'' +
				", hasNew=" + hasNew +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DownloadMap that = (DownloadMap) o;

		if (hasNew != that.hasNew) return false;
		if (MapName != null ? !MapName.equals(that.MapName) : that.MapName != null) return false;
		return ServerTime != null ? ServerTime.equals(that.ServerTime) : that.ServerTime == null;

	}

	@Override
	public int hashCode() {
		int result = MapName != null ? MapName.hashCode() : 0;
		result = 31 * result + (ServerTime != null ? ServerTime.hashCode() : 0);
		result = 31 * result + (hasNew ? 1 : 0);
		return result;
	}
}
