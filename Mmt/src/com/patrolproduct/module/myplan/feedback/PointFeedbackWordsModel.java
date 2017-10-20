package com.patrolproduct.module.myplan.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PointFeedbackWordsModel implements Parcelable {
	public int feedbackID; // 反馈ID
	public String feedbackDescription;// 反馈描述（作为反馈字典整体的描述）
	public String feedbackType;// 反馈值类型
	public String planName;// 计划名
	public String eventDescription;// 对事件进行描述
	public int flowID;// ？？
	public String group;

	public boolean isTrigger = false;// 是否触发
	public String trigCondition = "";// 触发条件
	public boolean isMust = false;

	@Override
	public PointFeedbackWordsModel clone() {
		PointFeedbackWordsModel model = new PointFeedbackWordsModel();
		model.feedbackID = this.feedbackID;
		model.feedbackDescription = this.feedbackDescription;
		model.feedbackType = this.feedbackType;
		model.planName = this.planName;
		model.eventDescription = this.eventDescription;
		model.flowID = this.flowID;
		model.group = this.group;

		model.isTrigger = this.isTrigger;
		model.trigCondition = this.trigCondition;
		model.isMust = this.isMust;

		return model;
	}

	private ArrayList<String> mediaList = new ArrayList<String>();

	public ArrayList<String> getMediaList() {
		return mediaList;
	}

	@Override
	public String toString() {
		return feedbackDescription;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.feedbackID);
		dest.writeString(this.feedbackDescription);
		dest.writeString(this.feedbackType);
		dest.writeString(this.planName);
		dest.writeString(this.eventDescription);
		dest.writeInt(this.flowID);
		dest.writeString(this.group);
		dest.writeByte(this.isTrigger ? (byte) 1 : (byte) 0);
		dest.writeString(this.trigCondition);
		dest.writeByte(this.isMust ? (byte) 1 : (byte) 0);
		dest.writeStringList(this.mediaList);
	}

	public PointFeedbackWordsModel() {
	}

	protected PointFeedbackWordsModel(Parcel in) {
		this.feedbackID = in.readInt();
		this.feedbackDescription = in.readString();
		this.feedbackType = in.readString();
		this.planName = in.readString();
		this.eventDescription = in.readString();
		this.flowID = in.readInt();
		this.group = in.readString();
		this.isTrigger = in.readByte() != 0;
		this.trigCondition = in.readString();
		this.isMust = in.readByte() != 0;
		this.mediaList = in.createStringArrayList();
	}

	public static final Creator<PointFeedbackWordsModel> CREATOR = new Creator<PointFeedbackWordsModel>() {
		@Override
		public PointFeedbackWordsModel createFromParcel(Parcel source) {
			return new PointFeedbackWordsModel(source);
		}

		@Override
		public PointFeedbackWordsModel[] newArray(int size) {
			return new PointFeedbackWordsModel[size];
		}
	};
}
