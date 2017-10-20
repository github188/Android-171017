package com.mapgis.mmt.common.util;

import android.graphics.PointF;
import android.util.Log;

import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/2/6.
 * 距离集合的工具类
 *
 * 目前只实现了Dotor接口的聚集算法
 * Dotable接口还未实现，不可用
 */

public class CollectUtil<T> {
    private static final String TAG = "CollectUtil";

    private ArrayList<RectNode> mRectNodes;
    private MapView mMapView;
    private List<T> mSrcDatas;
    private float viewWidth;
    private Dotor<T> mDotor;
    private double mMapDistance;

    public CollectUtil(MapView mapView, List<T> srcDatas) {
        this(mapView, srcDatas,50f,null);
    }

    public CollectUtil(MapView mapView, List<T> srcDatas, float viewWidth) {
        this(mapView, srcDatas,viewWidth,null);
    }

    public CollectUtil(MapView mapView, List<T> srcDatas, float viewWidth, Dotor<T> mDotor) {
//        this(mMapView, mSrcDatas, viewWidth);
        this.mMapView = mapView;
        this.mSrcDatas = srcDatas;
        this.viewWidth = viewWidth;
        this.mDotor = mDotor;
    }

    public void reset(){
        // 重置数据
        this.mRectNodes = null;
    }

    /**
     * 根据节点的索引获取聚集点中数据的索引集合
     * @param index     聚集点索引
     * @return          索引集合
     */
    public List<Integer> getCollectIndexs(int index){
        return this.mRectNodes.get(index).getIndexList();
    }

    /**
     * 聚集方法
     */
    public List<CollectDot<T>> collect(boolean isOrder) {
        ArrayList<CollectDot<T>> collectDots = new ArrayList<>();

        if (mMapView.getMap() == null) {
            throw new RuntimeException("地图没有加载！");
        }

        if (mSrcDatas == null || mSrcDatas.size() == 0) {
            return collectDots;
        }

        this.mMapDistance = viewDistToMapDist(mMapView, viewWidth);

        collectRectNodes();

        if (isOrder) {
            // 有序集合
            return collectWithOrder();
        } else {
            // 无序集合
            return collectWithUnorder();
        }
    }

    /**
     * 网格算法对有序坐标集合进行优化
     */
    private ArrayList<CollectDot<T>> collectWithOrder() {
        ArrayList<CollectDot<T>> collectDots = new ArrayList<>();
        if (mSrcDatas == null || mSrcDatas.size() == 0) {
            return collectDots;
        }

        // 遍历所有的坐标和矩形得到优化结果
        int preValue;
        int indexOf;
        RectNode nodeEntity;

        for (int i = 0; i < mSrcDatas.size(); i++) {
            for (int j = 0; j < mRectNodes.size(); j++) {
                nodeEntity = mRectNodes.get(j);
                indexOf = nodeEntity.indexOf(i);
                if (indexOf < 0) {
                    continue;
                }

                if (indexOf - 1 < 0) {
                    collectDots.add(createCollectDot(nodeEntity));
                    continue;
                }
                preValue = nodeEntity.getIndex(indexOf - 1);
                if (i - preValue != 1) {
                    // 需要重新创建聚集点
                    collectDots.add(createCollectDot(nodeEntity));
                }
            }
        }
        return collectDots;
    }

    /**
     * 无序集合优化
     */
    private List<CollectDot<T>> collectWithUnorder() {
        List<CollectDot<T>> collectDots = new ArrayList<>();
        for (RectNode nodeEntity : mRectNodes) {
            collectDots.add(createCollectDot(nodeEntity));
        }
        return collectDots;
    }

    private List<RectNode> collectRectNodes() {
        if (mRectNodes != null)return mRectNodes;
        mRectNodes = new ArrayList<>();
        Dot firstDot = mDotor.getDot(mSrcDatas.get(0));
        double halfWidth = mMapDistance / 2;

        // 遍历时产生的中间结果集
        Rect firstRect = new Rect(firstDot.getX() - halfWidth
                , firstDot.getY() - halfWidth
                , firstDot.getX() + halfWidth
                , firstDot.getY() + halfWidth);
        ArrayList<Integer> firstIndexs = new ArrayList<>();
        firstIndexs.add(0);
        RectNode firstNode = new RectNode(firstRect, firstIndexs);
        mRectNodes.add(firstNode);
//        Log.i(TAG, firstNode.toString());
        // 将第一个点作为默认的起始点进行绘制正方形并逐渐往下递归

        boolean isRectExists = false;
        for (int i = 1; i < mSrcDatas.size(); i++) {
            Dot dot = mDotor.getDot(mSrcDatas.get(i));
            for (int j = 0; j < mRectNodes.size(); j++) {
                RectNode nodeEntity = mRectNodes.get(j);
                isRectExists = nodeEntity.isInRect(dot);
                if (isRectExists) {
                    // 该点在矩形内，如果在矩形内就将对应的点的原始索引加到节点中
                    nodeEntity.addIndex(i);
                    break;
                }
            }

            if (!isRectExists) {
                // 该坐标不在已有的矩形框中，则重新添加一个矩形框
                double minX = Math.floor((dot.getX() - firstNode.getMinX()) / mMapDistance) * mMapDistance + firstNode.getMinX();
                double minY = Math.floor((dot.getY() - firstNode.getMinY()) / mMapDistance) * mMapDistance + firstNode.getMinY();
                double maxX = minX + mMapDistance;
                double maxY = minY + mMapDistance;
                Rect rect = new Rect(minX, minY, maxX, maxY);
                ArrayList<Integer> indexs = new ArrayList<>();
                indexs.add(i);
                RectNode nodeEntity = new RectNode(rect, indexs);
                mRectNodes.add(nodeEntity);
                continue;
            }

            isRectExists = false;
        }

        for (int i = 0; i < mRectNodes.size(); i++) {
            RectNode nodeEntity = mRectNodes.get(i);
            Log.i(TAG, i + "   " + nodeEntity.toString());
        }
        return mRectNodes;
    }

    private CollectDot<T> createCollectDot(RectNode nodeEntity) {
        CollectDot<T> collectDot = new CollectDot<>();
        for (int i : nodeEntity.getIndexList()) {
            collectDot.add(mSrcDatas.get(i));
        }
        return collectDot;
    }

    public double viewDistToMapDist(MapView mapView, float viewWidth) {
        Dot centerPoint = mapView.getCenterPoint();
        PointF firstPointF = mapView.mapPointToViewPoint(centerPoint);
        float x = firstPointF.x + viewWidth;
        Dot dotX = mapView.viewPointToMapPoint(new PointF(x, firstPointF.y));
        return Math.abs(dotX.getX() - centerPoint.getX());
    }
}
