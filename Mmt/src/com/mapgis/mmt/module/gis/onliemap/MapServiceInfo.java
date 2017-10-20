package com.mapgis.mmt.module.gis.onliemap;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.onliemap.tile.Lod;
import com.mapgis.mmt.module.gis.onliemap.tile.TileInfo;
import com.zondy.mapgis.geometry.Dot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * 服务信息
 *
 * @author Zoro
 */
public class MapServiceInfo {
    private static MapServiceInfo mapServiceInfo = new MapServiceInfo();

    public static MapServiceInfo getInstance() {
        return mapServiceInfo;
    }

    public Extent getInitialExtent() {
        return initialExtent;
    }

    public void setInitialExtent(Extent initialExtent) {
        this.initialExtent = initialExtent;
    }

    public SpatialReference getInitialSpatialReference() {
        return initialSpatialReference;
    }

    public void setInitialSpatialReference(SpatialReference initialSpatialReference) {
        this.initialSpatialReference = initialSpatialReference;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }

    public void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    public void setSingleFusedMapCache(boolean singleFusedMapCache) {
        this.singleFusedMapCache = singleFusedMapCache;
    }

    public void setTileInfo(TileInfo tileInfo) {
        this.tileInfo = tileInfo;
    }

    public void setFullExtent(Extent fullExtent) {
        this.fullExtent = fullExtent;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setSupportedImageFormatTypes(String supportedImageFormatTypes) {
        this.supportedImageFormatTypes = supportedImageFormatTypes;
    }

    public OnlineLayerInfo[] getLayers() {
        return layers;
    }

    public void setLayers(OnlineLayerInfo[] layers) {
        this.layers = layers;
    }

    private String serviceDescription;
    private String mapName;
    private String description;
    private String copyrightText;
    private SpatialReference spatialReference;
    private boolean singleFusedMapCache;
    private TileInfo tileInfo;
    private Extent initialExtent;
    private SpatialReference initialSpatialReference;
    private Extent fullExtent;
    private String units;

    /**
     * 是否是高清图，高清图关闭瓦片自动缩放，普清图开启瓦片自动缩放
     */
    private String highresolution = "false";
    private String supportedImageFormatTypes;
    private String url;

    private OnlineLayerInfo[] layers;
    //点图层id列表
    private List<String> pointLayerIds;
    //线图层id列表
    private List<String> lineLayerIds;
    //全部图层id列表
    private List<String> layerIds;

    public MapServiceInfo(String url) {
        this.url = url;
    }

    public MapServiceInfo() {
    }

    public List<String> getPointLayerIds() {
        if (pointLayerIds == null) {
            pointLayerIds = new ArrayList<>();
            for (OnlineLayerInfo item : layers) {

                if (!"Point".equalsIgnoreCase(item.geometryType)) {
                    continue;
                }
                pointLayerIds.add(item.id);
            }
        }

        return pointLayerIds;
    }

    public List<String> getLineLayerIds() {
        if (lineLayerIds == null) {
            lineLayerIds = new ArrayList<>();
            for (OnlineLayerInfo item : layers) {

                if (!"Polyline".equalsIgnoreCase(item.geometryType)) {
                    continue;
                }
                lineLayerIds.add(item.id);
            }
        }

        return lineLayerIds;
    }

    public List<String> getLayerIds() {
        if (layerIds == null) {
            layerIds = new ArrayList<>();
            layerIds.addAll(getPointLayerIds());
            layerIds.addAll(getLineLayerIds());
        }

        return layerIds;
    }

    public static MapServiceInfo fromNetwork(String url) {
        try {
            String result = NetUtil.executeHttpGetAppointLastTime(30, url);

            mapServiceInfo = new Gson().fromJson(result, MapServiceInfo.class);

            if (mapServiceInfo == null) {
                mapServiceInfo = new MapServiceInfo(url);
            }

            mapServiceInfo.setUrl(url);

            return mapServiceInfo;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static MapServiceInfo fromXML(String path) {
        MapServiceInfo info = new MapServiceInfo();

        try {
            String input;

            if (new File(path).exists()) {
                input = new String(BaseClassUtil.readFileByBytes(path));
            } else
                return info;

            Matcher matcher = Pattern.compile("<name>(.*?)</name>").matcher(input);

            if (matcher.find()) {
                info.setMapName(matcher.group(1));
            }

            matcher = Pattern.compile("<boundbox>(.*?) (.*?) (.*?) (.*?)</boundbox>").matcher(input);

            if (matcher.find()) {
                info.setFullExtent(new Extent(Double.valueOf(matcher.group(1)), Double.valueOf(matcher.group(2)), Double.valueOf(matcher
                        .group(3)), Double.valueOf(matcher.group(4))));
            }

            matcher = Pattern.compile("<unit>(.*?)</unit>").matcher(input);

            if (matcher.find()) {
                info.setUnits(matcher.group(1));
            }

            matcher = Pattern.compile("<highresolution>(.*?)</highresolution>").matcher(input);

            if (matcher.find()) {
                info.setHighresolution(matcher.group(1));
            }

            TileInfo tileInfo = new TileInfo();

            matcher = Pattern.compile("<tilewidth>(.*?)</tilewidth>").matcher(input);

            if (matcher.find()) {
                tileInfo.rows = Integer.valueOf(matcher.group(1));
            }

            matcher = Pattern.compile("<tileheight>(.*?)</tileheight>").matcher(input);

            if (matcher.find()) {
                tileInfo.cols = Integer.valueOf(matcher.group(1));
            }

            tileInfo.lods = new ArrayList<Lod>();

            matcher = Pattern
                    .compile(
                            "<lodinfo>\\s*<id>(.*?)</id>\\s*<topleftcorner>(.*?) (.*?)</topleftcorner>\\s*<resolution>(.*?)</resolution>\\s*</lodinfo>")
                    .matcher(input);

            while (matcher.find()) {
                if (tileInfo.origin == null) {
                    tileInfo.origin = new Dot(Double.valueOf(matcher.group(2)), Double.valueOf(matcher.group(3)));
                }

                tileInfo.lods.add(new Lod(Integer.valueOf(matcher.group(1)), Double.valueOf(matcher.group(4))));
            }

            info.setTileInfo(tileInfo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return info;
    }

    public String getServiceDescription() {
        return this.serviceDescription;
    }

    public String getMapName() {
        return this.mapName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCopyrightText() {
        return this.copyrightText;
    }

    public SpatialReference getSpatialReference() {
        return this.spatialReference;
    }

    public boolean getSingleFusedMapCache() {
        return this.singleFusedMapCache;
    }

    public TileInfo getTileInfo() {
        return this.tileInfo;
    }

    public Extent getEnvelope() {
        return this.initialExtent;
    }

    public Extent getFullExtent() {
        return this.fullExtent;
    }

    public SpatialReference getSR() {
        return this.initialSpatialReference == null ? this.spatialReference : this.initialSpatialReference;
    }

    public void setSR(SpatialReference spatialReference) {
        this.initialSpatialReference = spatialReference;
    }

    public String getUnits() {
        return this.units;
    }

    public String getHighresolution() {
        if (!TextUtils.isEmpty(highresolution) && highresolution.equals("true"))
            return "true";
        else if (this.tileInfo != null && this.tileInfo.dpi > 96)
            return "true";
        else
            return "false";
    }

    public void setHighresolution(String highresolution) {
        this.highresolution = highresolution;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSupportedImageFormatTypes() {
        return this.supportedImageFormatTypes;
    }

    /**
     * 根据图层ID返回图层信息
     *
     * @param id 图层ID
     * @return 图层信息
     */
    public OnlineLayerInfo getLayerById(int id) {
        if (layers == null) {
            return null;
        }

        for (OnlineLayerInfo layer : layers) {
            if (Integer.valueOf(layer.id) == id) {
                return layer;
            }
        }

        return null;
    }

    /**
     * 根据图层名称返回图层信息
     *
     * @param name 图层名称
     * @return 图层信息
     */
    public OnlineLayerInfo getLayerByName(String name) {
        if (layers == null) {
            return null;
        }

        for (OnlineLayerInfo layer : layers) {
            if (name.equals(layer.name)) {
                return layer;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "MapServer [serviceDescription=" + this.serviceDescription + ", mapName=" + this.mapName + ", description="
                + this.description + ", copyrightText=" + this.copyrightText + ", spatialReference=" + this.spatialReference
                + ", singleFusedMapCache=" + this.singleFusedMapCache + ", tileInfo=" + this.tileInfo + ", initialExtent="
                + this.initialExtent + ", fullExtent=" + this.fullExtent + ", units=" + this.units + ", supportedImageFormatTypes="
                + this.supportedImageFormatTypes + "]";
    }

    @Override
    public int hashCode() {
        int i2 = 1;
        i2 = 31 * i2 + (this.copyrightText == null ? 0 : this.copyrightText.hashCode());
        i2 = 31 * i2 + (this.description == null ? 0 : this.description.hashCode());
        i2 = 31 * i2 + (this.fullExtent == null ? 0 : this.fullExtent.hashCode());
        i2 = 31 * i2 + (this.initialExtent == null ? 0 : this.initialExtent.hashCode());
        i2 = 31 * i2 + (this.mapName == null ? 0 : this.mapName.hashCode());
        i2 = 31 * i2 + (this.serviceDescription == null ? 0 : this.serviceDescription.hashCode());
        i2 = 31 * i2 + (this.singleFusedMapCache ? 1231 : 1237);
        i2 = 31 * i2 + (this.spatialReference == null ? 0 : this.spatialReference.hashCode());
        i2 = 31 * i2 + (this.supportedImageFormatTypes == null ? 0 : this.supportedImageFormatTypes.hashCode());

        i2 = 31 * i2 + (this.tileInfo == null ? 0 : this.tileInfo.hashCode());
        i2 = 31 * i2 + (this.units == null ? 0 : this.units.hashCode());
        i2 = 31 * i2 + (this.url == null ? 0 : this.url.hashCode());
        return i2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MapServiceInfo localg = (MapServiceInfo) obj;
        if (this.copyrightText == null) {
            if (localg.copyrightText != null) {
                return false;
            }
        } else if (!this.copyrightText.equals(localg.copyrightText)) {
            return false;
        }
        if (this.description == null) {
            if (localg.description != null) {
                return false;
            }
        } else if (!this.description.equals(localg.description)) {
            return false;
        }
        if (this.fullExtent == null) {
            if (localg.fullExtent != null) {
                return false;
            }
        } else if (!this.fullExtent.equals(localg.fullExtent)) {
            return false;
        }
        if (this.initialExtent == null) {
            if (localg.initialExtent != null) {
                return false;
            }
        } else if (!this.initialExtent.equals(localg.initialExtent)) {
            return false;
        }
        if (this.mapName == null) {
            if (localg.mapName != null) {
                return false;
            }
        } else if (!this.mapName.equals(localg.mapName)) {
            return false;
        }
        if (this.serviceDescription == null) {
            if (localg.serviceDescription != null) {
                return false;
            }
        } else if (!this.serviceDescription.equals(localg.serviceDescription)) {
            return false;
        }
        if (this.singleFusedMapCache != localg.singleFusedMapCache) {
            return false;
        }
        if (this.spatialReference == null) {
            if (localg.spatialReference != null) {
                return false;
            }
        } else if (!this.spatialReference.equals(localg.spatialReference)) {
            return false;
        }
        if (this.supportedImageFormatTypes == null) {
            if (localg.supportedImageFormatTypes != null) {
                return false;
            }
        } else if (!this.supportedImageFormatTypes.equals(localg.supportedImageFormatTypes)) {
            return false;
        }
        if (this.tileInfo == null) {
            if (localg.tileInfo != null) {
                return false;
            }
        } else if (!this.tileInfo.equals(localg.tileInfo)) {
            return false;
        }
        if (this.units == null) {
            if (localg.units != null) {
                return false;
            }
        } else if (!this.units.equals(localg.units)) {
            return false;
        }
        if (this.url == null) {
            if (localg.url != null) {
                return false;
            }
        } else if (!this.url.equals(localg.url)) {
            return false;
        }
        return true;
    }
}
