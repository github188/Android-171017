package com.maintainproduct.v2.module;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectInfoItem implements Parcelable {

	public String acceptNo;
	public String applyNo;
    public String projectID;
	public String caseNo;
	public String projectName;
	public String projectAdd;
	public String projectDetail;
	public String projectState;
	public String createTime;
	public String isExist;
	public String projectGeometry;
	public String schedule;
	
	/**  管道工程类型（内审、外审）  */
     public String projectType;
     public String projectContent;
     public String bidID;   // 标段 与 工程 关联表 的 主键 ， 注意不是BIDSECTIONID
     public String bidName;  

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(acceptNo);
		dest.writeString(applyNo);
		dest.writeString(projectID);
		dest.writeString(caseNo);
		dest.writeString(projectName);
		dest.writeString(projectAdd);
		dest.writeString(projectDetail);
		dest.writeString(projectState);
		dest.writeString(createTime);
		dest.writeString(isExist);
		dest.writeString(projectGeometry);
		dest.writeString(schedule);
		dest.writeString(projectType);
		dest.writeString(projectContent);
		dest.writeString(bidID);
		dest.writeString(bidName);
	}
	
	public static final Parcelable.Creator<ProjectInfoItem> CREATOR = new Creator<ProjectInfoItem>() {

		@Override
		public ProjectInfoItem createFromParcel(Parcel source) { 
			ProjectInfoItem info = new ProjectInfoItem();
			info.acceptNo = source.readString();
			info.applyNo = source.readString();
			info.projectID = source.readString();
			info.caseNo = source.readString();
			info.projectName = source.readString();
			info.projectAdd = source.readString();
			info.projectDetail = source.readString();
			info.projectState = source.readString();
			info.createTime = source.readString();
			info.isExist = source.readString();
			info.projectGeometry = source.readString();
			info.schedule = source.readString();
			info.projectType = source.readString();
			info.projectContent = source.readString();
			info.bidID = source.readString();
			info.bidName = source.readString();
			return info;
		}

		@Override
		public ProjectInfoItem[] newArray(int size) {
			return new ProjectInfoItem[size];
		}

	};
	
}


