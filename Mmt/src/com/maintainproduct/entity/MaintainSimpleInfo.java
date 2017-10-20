package com.maintainproduct.entity;

import android.os.Parcel;
import android.os.Parcelable;

/** 维修养护工单列表项信息 */
public class MaintainSimpleInfo implements Parcelable {
	public int ActiveID;
	public String ActiveName;
	public String CaseName;
	public String CaseNo;
	public String FlowName;
	public int ID0;
	public String Opinion;
	public String PreStepUnderTakenManName;

	public String Position;
	public int ID;

    public String nextActiveID;
	public MaintainSimpleInfo() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(ActiveID);
		out.writeString(ActiveName);
		out.writeString(CaseName);
		out.writeString(CaseNo);
		out.writeString(FlowName);
		out.writeInt(ID0);
		out.writeString(Opinion);
		out.writeString(PreStepUnderTakenManName);
		out.writeString(Position);
		out.writeInt(ID);
        out.writeString(nextActiveID);
	}

	public static final Parcelable.Creator<MaintainSimpleInfo> CREATOR = new Parcelable.Creator<MaintainSimpleInfo>() {
		@Override
		public MaintainSimpleInfo createFromParcel(Parcel in) {
			return new MaintainSimpleInfo(in);
		}

		@Override
		public MaintainSimpleInfo[] newArray(int size) {
			return new MaintainSimpleInfo[size];
		}
	};

	private MaintainSimpleInfo(Parcel in) {
		ActiveID = in.readInt();
		ActiveName = in.readString();
		CaseName = in.readString();
		CaseNo = in.readString();
		FlowName = in.readString();
		ID0 = in.readInt();
		Opinion = in.readString();
		PreStepUnderTakenManName = in.readString();
		Position = in.readString();
		ID = in.readInt();
        nextActiveID=in.readString();
	}
}