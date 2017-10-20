package com.patrol.entity;

import java.util.ArrayList;

/**
 * Created by Comclay on 2017/6/13.
 * 上报的到位实体类
 */

public class ReportArriveEntity {
    // 任务id
    public int id = -1;
    // 任务id所对应的到位的关键点集合
    public ArrayList<Integer> keyIdList = new ArrayList<>();

    public ReportArriveEntity(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ReportArriveEntity{id=" + id + ", keyIdList=" + keyIdList + '}';
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof ReportArriveEntity) && ((ReportArriveEntity) obj).id == id;
    }
}
