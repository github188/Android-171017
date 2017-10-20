package com.mapgis.mmt.common.util;

import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

/**
 * Created by Comclay on 2017/1/21.
 * 矩形节点
 */
public class RectNode implements Comparable<RectNode> {
    // 聚集条件
    private Rect mRect;
    // 包含的所有的坐标的原始集合中的索引
    private ArrayList<Integer> mIndexList;

    public RectNode(Rect mRect, ArrayList<Integer> mIndexList) {
        this.mRect = mRect;
        this.mIndexList = mIndexList;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    public ArrayList<Integer> getIndexList() {
        return mIndexList;
    }

    public void setIndexList(ArrayList<Integer> mIndexList) {
        this.mIndexList = mIndexList;
    }

    public void addIndex(int index){
        this.mIndexList.add(index);
    }

    public int indexOf(int value){
        return mIndexList.indexOf(value);
    }

    public int getIndex(int index){
        if (index < 0 || index >= mIndexList.size()){
            return -1;
        }
        return mIndexList.get(index);
    }

    /**
     * 判断一个坐标是否在改矩形内
     * @param dot   坐标
     * @return      true在该矩形内，false不在该矩形内
     */
    public boolean isInRect(Dot dot){
        return dot.getX() >= mRect.getXMin() && dot.getX() <= mRect.getXMax()
                && dot.getY() >= mRect.getYMin() && dot.getY() <= mRect.getYMax();
    }

    public boolean containIndex(int index){
        if (index < mIndexList.get(0) || index > mIndexList.get(mIndexList.size() - 1)){
            return false;
        }

        return mIndexList.contains(index);
    }

    @Override
    public String toString() {
        return "NodeEntity{" +
                "mRect=" + mRect +
                ", mIndexList=" + mIndexList +
                '}';
    }

    @Override
    public int compareTo(RectNode nodeEntity) {
        int result = (int) (this.mRect.getXMin() - nodeEntity.getMinX());
        if (result > 0) return result;

        result = (int) (this.mRect.getYMin() - nodeEntity.getMinY());
        if (result > 0) return result;

        result = (int) (this.mRect.getXMax() - nodeEntity.getMaxX());
        if (result > 0) return result;

        result = (int) (this.mRect.getYMax() - nodeEntity.getMaxY());
        if (result > 0) return result;

        return 0;
    }

    public double getMinX(){
        return mRect.getXMin();
    }

    public double getMinY(){
        return mRect.getYMin();
    }

    public double getMaxX() {
        return mRect.getXMax();
    }

    public double getMaxY() {
        return mRect.getYMax();
    }
}
