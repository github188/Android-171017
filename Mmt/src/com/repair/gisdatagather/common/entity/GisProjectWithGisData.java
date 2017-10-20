package com.repair.gisdatagather.common.entity;

import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/3/30.
 * 服务端工程实体
 */
public class GisProjectWithGisData extends GisProjectWithoutGisData {
    public List<GISDataBeanBase> gisDatas = new ArrayList<>();

    public GISDataProject convert2GisDataProject(MapView mapView) {
        return new GISDataProject(mapView, GisProjectWithGisData.this);
    }
}
