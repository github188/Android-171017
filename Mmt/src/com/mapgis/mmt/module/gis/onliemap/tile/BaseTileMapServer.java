package com.mapgis.mmt.module.gis.onliemap.tile;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.onliemap.Extent;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.LongUser;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.MapServerBrowseType;
import com.zondy.mapgis.map.MapServerType;
import com.zondy.mapgis.map.TileMapServer;
import com.zondy.mapgis.srs.SRefData;

public class BaseTileMapServer extends TileMapServer {

    private static final long serialVersionUID = 1L;

    private MapServiceInfo info;

    public MapServiceInfo getInfo() {
        return info;
    }

    public void setMapServiceInfo(MapServiceInfo info) {
        this.info = info;
    }

    public void setMapServiceInfo(String path) {
        try {
            if (path.endsWith(".xml"))
                info = MapServiceInfo.fromXML(path);
            else
                info = MapServiceInfo.fromNetwork(path);

            if (info == null)
                info = new MapServiceInfo();

            if (info.getFullExtent() == null)
                info.setFullExtent(new Extent());

            BaseClassUtil.logd(this, new Gson().toJson(info));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean getTileMatrix(long l, LongUser long1, LongUser long2, LongUser long3, LongUser long4) {

        Rect rect = info.getFullExtent().getRect();

        Dot dot = getTileOriginXY();

        double xmin = rect.xMin, ymin = rect.yMin, xmax = rect.xMax, ymax = rect.yMax, x0 = dot.x, y0 = dot.y;

        double rw = getTileResolution(l) * info.getTileInfo().rows;

        long1.setValue((long) Math.floor((y0 - ymax) / rw));
        long2.setValue((long) Math.floor((xmin - x0) / rw));
        long3.setValue((long) Math.ceil((y0 - ymin) / rw) - 1);
        long4.setValue((long) Math.ceil((xmax - x0) / rw) - 1);

        return true;
    }

    @Override
    public Dot getTileOriginXY() {
        return info.getTileInfo().origin;
    }

    @Override
    public double getTileResolution(long l) {
        return info.getTileInfo().lods.get((int) l).getResolution();
    }

    @Override
    public boolean getTileSize(LongUser long1, LongUser long2) {
        long1.setValue(info.getTileInfo().rows);
        long2.setValue(info.getTileInfo().cols);

        return true;
    }

    @Override
    public long getMaxZoom() {
        return this.info.getTileInfo().lods.size() - 1;
    }

    @Override
    public long getMinZoom() {
        return 0;
    }

    @Override
    public String getName() {
        return info.getMapName();
    }

    @Override
    public double getScale(int i, double d) {
        return this.info.getTileInfo().lods.get(i).getScale();
    }

    @Override
    public boolean getIsValid() {
        return true;
    }

    @Override
    public Rect getEntireExtent() {
        if (info != null && info.getFullExtent() != null) {
            return info.getFullExtent().getRect();
        }
        return new Rect();
    }

    @Override
    public MapServerBrowseType getMapBrowseType() {
        return MapServerBrowseType.MapTile;
    }

    @Override
    public double getGroundResolution(long l, double d) {
        return super.getGroundResolution(l, d);
    }

    @Override
    public SRefData getSRS() {
        return super.getSRS();
    }

    @Override
    public byte[] getTileImage(String s) {
        return super.getTileImage(s);
    }

    @Override
    public MapServerType getType() {
        return super.getType();
    }

    @Override
    public boolean getZoomCapacity(LongUser long1, LongUser long2) {
        return super.getZoomCapacity(long1, long2);
    }

    @Override
    public void setURL(String s) {
        super.setURL(s);
    }

    @Override
    public void setName(String s) {
        super.setName(s);
    }

    @Override
    public void setMaxZoom(long l) {
        super.setMaxZoom(l);
    }

    @Override
    public void setMinZoom(long l) {
        super.setMinZoom(l);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getURL() {
        return info.getUrl();
    }
}
