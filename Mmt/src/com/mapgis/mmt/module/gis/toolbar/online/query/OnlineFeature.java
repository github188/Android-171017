package com.mapgis.mmt.module.gis.toolbar.online.query;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 在线数据属性信息模型
 */
public class OnlineFeature implements Parcelable {
    public String layerId;
    public String layerName;
    public String value;
    public String displayFieldName;
    public boolean hasMediaFile;
    public LinkedHashMap<String, String> attributes;
    public String geometryType;
    public OnlineGermetry geometry;

    public MmtAnnotation showAnnotationOnMap(MapView mapView, String highlight, String args, Bitmap bitmap) {
        Dot point = geometry.toDot();

        MmtAnnotation annotation;

        if (BaseClassUtil.isNullOrEmptyString(layerName)) {
            annotation = new MmtAnnotation(this, highlight, null, point, null);
        } else {
            annotation = new MmtAnnotation(this, layerName, highlight, point, null);
        }

        annotation.setImage(bitmap);
        annotation.attrMap.putAll(attributes);

        //增加 编号 和 $图层名称$ 供属性编辑使用

        //说明：
        //1:
        //对于在线查询，displayFieldName（设备标志）为"OID"， value为OID的值
        //这里用编号存储OID的值，是为了和离线地图一样处理==>oid和编号是二个不同的概念，不能这么使用 by Zoro
        if (!annotation.attrMap.containsKey("编号")) {
            annotation.attrMap.put("编号", "");
        }
        annotation.attrMap.put("$图层名称$", layerName);
        annotation.attrMap.put("$geometryType$", geometryType);

        mapView.getAnnotationLayer().addAnnotation(annotation);

        return annotation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(layerId);
        out.writeString(layerName);
        out.writeString(value);
        out.writeString(displayFieldName);
        out.writeByte((byte) (hasMediaFile ? 1 : 0));
        out.writeMap(attributes);
        out.writeString(geometryType);
        out.writeParcelable(geometry, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
    }

    public static final Parcelable.Creator<OnlineFeature> CREATOR = new Parcelable.Creator<OnlineFeature>() {
        @Override
        public OnlineFeature createFromParcel(Parcel in) {
            return new OnlineFeature(in);
        }

        @Override
        public OnlineFeature[] newArray(int size) {
            return new OnlineFeature[size];
        }
    };

    @SuppressWarnings("unchecked")
    private OnlineFeature(Parcel in) {
        layerId = in.readString();
        layerName = in.readString();
        value = in.readString();
        displayFieldName = in.readString();
        hasMediaFile = in.readByte() != 0;

        HashMap<String, String> map = in.readHashMap(HashMap.class.getClassLoader());
        if (attributes == null) {
            attributes = new LinkedHashMap<String, String>();
        }
        attributes.putAll(map);

        geometryType = in.readString();
        geometry = in.readParcelable(OnlineGermetry.class.getClassLoader());
    }
}
