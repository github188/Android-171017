package com.mapgis.mmt.common.util;

import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;

/**
 * Created by Comclay on 2017/2/6.
 * 聚集实体，包含所有聚集对象
 */

public class CollectDot<T> {
    private ArrayList<T> mDataList;
    public CollectDot() {
        mDataList = new ArrayList<>();
    }
    public void add(T t){
        mDataList.add(t);
    }

    @Override
    public String toString() {
        return mDataList.toString();
    }

    /**
     * 计算出聚集点集合的质点中心
     *
     * @return 质点中心
     */
    public Dot getCenterDot(Dotor<T> dotor) {
        double centerX = 0;
        double centerY = 0;
        Dot dot;
        for (T t : mDataList) {
            dot = dotor.getDot(t);
            centerX += dot.getX();
            centerY += dot.getY();
        }
        return new Dot(centerX / mDataList.size(), centerY / mDataList.size());
    }

    public int getSize(){
        return this.mDataList.size();
    }
}
