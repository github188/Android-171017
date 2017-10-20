package com.repair.shaoxin.water.hotlinetask;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class HotlineTaskEntity implements Parcelable {

    /** 详细地址 */
    @SerializedName("AddressTablet")
    public String addressTablet;

    /** 附件信息 */
    @SerializedName("AddFileSave")
    public String addFileSave;

    /** 联系人 */
    @SerializedName("ComplainPerson")
    public String complainPerson;

    /** 反映类别 */
    @SerializedName("ComplainTypeName")
    public String complainTypeName;

    /** 反映内容 */
    @SerializedName("ComplainTypeinfoName")
    public String complainTypeinfoName;

    /** 用户号 */
    @SerializedName("CustomerId")
    public String customerId;

    /** 处理超时 */
    @SerializedName("DealtimeLongdate")
    public String dealtimeLongdate;

    /** 备注 */
    @SerializedName("Memo")
    public String memo;

    /** 受理内容 */
    @SerializedName("QuestionMemo")
    public String questionMemo;

    /** 联系电话 */
    @SerializedName("RelationTel")
    public String relationTel;

    /** 下单时间 */
    @SerializedName("SaveDate")
    public String saveDate;

    /** 20分/30分 */
    @SerializedName("ServiceTime")
    public String serviceTime;

    /** 工单状态 */
    @SerializedName("StateName")
    public String stateName;

    /** 工单编号 */
    @SerializedName("WorkTaskSeq")
    public String workTaskSeq;

    protected HotlineTaskEntity(Parcel in) {
        addressTablet = in.readString();
        addFileSave = in.readString();
        complainPerson = in.readString();
        complainTypeName = in.readString();
        complainTypeinfoName = in.readString();
        customerId = in.readString();
        dealtimeLongdate = in.readString();
        memo = in.readString();
        questionMemo = in.readString();
        relationTel = in.readString();
        saveDate = in.readString();
        serviceTime = in.readString();
        stateName = in.readString();
        workTaskSeq = in.readString();
    }

    public static final Creator<HotlineTaskEntity> CREATOR = new Creator<HotlineTaskEntity>() {
        @Override
        public HotlineTaskEntity createFromParcel(Parcel in) {
            return new HotlineTaskEntity(in);
        }

        @Override
        public HotlineTaskEntity[] newArray(int size) {
            return new HotlineTaskEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addressTablet);
        dest.writeString(addFileSave);
        dest.writeString(complainPerson);
        dest.writeString(complainTypeName);
        dest.writeString(complainTypeinfoName);
        dest.writeString(customerId);
        dest.writeString(dealtimeLongdate);
        dest.writeString(memo);
        dest.writeString(questionMemo);
        dest.writeString(relationTel);
        dest.writeString(saveDate);
        dest.writeString(serviceTime);
        dest.writeString(stateName);
        dest.writeString(workTaskSeq);
    }
}
