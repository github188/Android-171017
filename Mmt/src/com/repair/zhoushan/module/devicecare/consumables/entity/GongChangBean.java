package com.repair.zhoushan.module.devicecare.consumables.entity;

import java.util.List;

public class GongChangBean extends SAPBean {

    public List<WuLiaoKuBean> WuLiaoKuList;

    public List<SAPBean> ChenBenCenterList;

    //region 本地添加的字段

    private String gongSiCode;

    public String getGongSiCode() {
        return gongSiCode;
    }

    public void setGongSiCode(String gongSiCode) {
        this.gongSiCode = gongSiCode;
    }

    //endregion
}
