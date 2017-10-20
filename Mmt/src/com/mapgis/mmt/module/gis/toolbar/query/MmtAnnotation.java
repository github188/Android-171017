package com.mapgis.mmt.module.gis.toolbar.query;

import android.graphics.Bitmap;

import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.geometry.Dot;

import java.util.LinkedHashMap;
import java.util.UUID;

public class MmtAnnotation extends Annotation {

    private static final long serialVersionUID = -4238299403281958158L;

    public static final int SPATIAL_QUERY = 0;
    public static final int MY_PLAN = 1;
    public static final int POINT_QUERY = 2;

    public int Type = SPATIAL_QUERY;

    public Graphic graphic;
    public OnlineFeature onlineFeature;
    public String info;

    public String uuid = UUID.randomUUID().toString();

    public final boolean isOnline = MobileConfig.MapConfigInstance.IsVectorQueryOnline;

    public final LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

    public MmtAnnotation(Graphic graphic, String title, String description, Dot point, Bitmap bitmap) {
        super(title, description, point, bitmap);

        this.graphic = graphic;
        graphicToMap(graphic);
    }

    public MmtAnnotation(OnlineFeature onlineFeature, String title, String description, Dot point, Bitmap bitmap) {
        super(title, description, point, bitmap);
        this.onlineFeature = onlineFeature;
    }

    public MmtAnnotation(String info, String title, String description, Dot point, Bitmap bitmap) {
        super(title, description, point, bitmap);
        this.info = info;
    }

    private void graphicToMap(Graphic graphic) {
        if (graphic == null) {
            return;
        }

        for (long i = 0; i < graphic.getAttributeNum(); i++) {
            String key = graphic.getAttributeName(i);
            String value = graphic.getAttributeValue(i);
            attrMap.put(key, value);
        }

    }
}
