package com.repair.zhoushan.module.devicecare.consumables.entity;

import java.util.List;

public class WuLiaoKuBean extends SAPBean {

    private List<WuLiaoZuBean> WuLiaoZuList;

    public List<WuLiaoZuBean> getWuLiaoZuList() {
        return WuLiaoZuList;
    }

    public void setWuLiaoZuList(List<WuLiaoZuBean> wuLiaoZuList) {
        WuLiaoZuList = wuLiaoZuList;
    }
}