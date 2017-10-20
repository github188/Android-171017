package com.repair.zhoushan.module.devicecare.consumables.entity;

import java.util.LinkedList;
import java.util.List;

public class WuLiaoOrderBean extends HaoCaiBean {

    public List<WuLiaoBean> WuLiaoList;

    public String YuLiuHao;
    public int IsSended;

    public WuLiaoOrderBean() {
        WuLiaoList = new LinkedList<WuLiaoBean>();
        YuLiuHao = "";
        IsSended = 0;
    }

}