package com.repair.zhoushan.module.devicecare.consumables.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class WuLiaoBean extends SAPBean implements Parcelable {

    public WuLiaoBean() {
    }

    public WuLiaoBean(String code, String name, int num, String unit) {
        super(code, name);
        Num = num;
        Unit = unit;
    }

    private int Num;
    private String Unit;

    public int getNum() {
        return Num;
    }

    public void setNum(int num) {
        Num = num;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String HaoCaiID;

    public SAPBean GongSi;

    public SAPBean GongChang;

    public SAPBean ChenBenCenter;

    public SAPBean WuLiaoKu;

    public SAPBean WuLiaoZu;

    //region 添加物料用，服务没有该字段，为本地添加

    private boolean isCheck;
    private int tagValue; // 用于记录临时值的一个辅助字段

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public int getTagValue() {
        return tagValue;
    }

    public void setTagValue(int tagValue) {
        this.tagValue = tagValue;
    }

    //endregion

    /**
     * 公司，工厂，物料库，物料组，物料五个属性都相同，才表示同一个
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj instanceof WuLiaoBean) {

            // 物料
            boolean isEqual = super.equals(obj);
            if (!isEqual)
                return false;

            WuLiaoBean bean = (WuLiaoBean) obj;

            // 公司
            if (this.GongSi == null) {
                isEqual = (bean.GongSi == null);
            } else {
                isEqual = this.GongSi.equals(bean.GongSi);
            }
            if (!isEqual)
                return false;

            // 工厂
            if (this.GongChang == null) {
                isEqual = (bean.GongChang == null);
            } else {
                isEqual = this.GongChang.equals(bean.GongChang);
            }
            if (!isEqual)
                return false;

            // 物料库
            if (this.WuLiaoKu == null) {
                isEqual = (bean.WuLiaoKu == null);
            } else {
                isEqual = this.WuLiaoKu.equals(bean.WuLiaoKu);
            }
            if (!isEqual)
                return false;

            // 物料组
            if (this.WuLiaoZu == null) {
                isEqual = (bean.WuLiaoZu == null);
            } else {
                isEqual = this.WuLiaoZu.equals(bean.WuLiaoZu);
            }

            return isEqual;
        }
        return false;
    }

    protected WuLiaoBean(Parcel in) {
        Num = in.readInt();
        Unit = in.readString();
        isCheck = in.readByte() != 0;
        tagValue = in.readInt();
        setCode(in.readString());
        setName(in.readString());
    }

    public static final Creator<WuLiaoBean> CREATOR = new Creator<WuLiaoBean>() {
        @Override
        public WuLiaoBean createFromParcel(Parcel in) {
            return new WuLiaoBean(in);
        }

        @Override
        public WuLiaoBean[] newArray(int size) {
            return new WuLiaoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Num);
        dest.writeString(Unit);
        dest.writeByte((byte) (isCheck ? 1 : 0));
        dest.writeInt(tagValue);
        dest.writeString(getCode());
        dest.writeString(getName());
    }
}
