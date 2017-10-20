package com.mapgis.mmt.module.gis.onliemap.dynamic;

import android.text.TextUtils;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.List;

public class MmtDynamicMapServer extends BaseDynamicMapServer {
    private MapServiceInfo info;

    public MmtDynamicMapServer(MapServiceInfo info) {
        this.info = info;
    }

    public void setInfo(MapServiceInfo info) {
        this.info = info;
    }

    public MapServiceInfo getInfo() {
        return info;
    }

    @Override
    public Rect getEntireExtent() {
        if (info == null || info.getFullExtent() == null){
            return new Rect();
        }
        return info.getFullExtent().getRect();
    }

    @Override
    public String getName() {
        return info.getMapName();
    }

    @Override
    public String getURL() {
        return info.getUrl();
    }

    @Override
    public byte[] getVectorImage(long imgWidth, long imgHeight, double dispRectXmin, double dispRectYmin, double dispRectXmax,
                                 double dispRectYmax, String strShowLayers, int imageType) {
        try {
            if (BaseClassUtil.isNullOrEmptyString(strShowLayers) || !strShowLayers.contains("show")) {

                List<String> showLayerIds = new ArrayList<>();

                OnlineLayerInfo[] appointShowLayers = info.getLayers();
                if (appointShowLayers != null && appointShowLayers.length > 0) {

                    for (OnlineLayerInfo item : appointShowLayers) {
                        if (!item.defaultVisibility) {
                            continue;
                        }
                        showLayerIds.add(item.id);
                    }

                }

                if (showLayerIds.size() > 0) {
                    strShowLayers = "show:" + TextUtils.join(",", showLayerIds);
                }
            }


            String url = this.getURL().replace("{width}", String.valueOf(imgWidth)).replace("{height}", String.valueOf(imgHeight))
                    .replace("{xmin}", String.valueOf(dispRectXmin)).replace("{ymin}", String.valueOf(dispRectYmin))
                    .replace("{xmax}", String.valueOf(dispRectXmax)).replace("{ymax}", String.valueOf(dispRectYmax))
                    .replace("{layers}", String.valueOf(strShowLayers)).replace("{layers}", String.valueOf(strShowLayers));

            return NetUtil.executeHttpGetBytes(30, url);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
