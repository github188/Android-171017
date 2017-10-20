package com.mapgis.mmt.config;

public class LayerConfigInfo {
    public String LayerName;
    public boolean IsEquipment;
    public String HighlightField;
}

class EmptyLayerConfigInfo extends LayerConfigInfo {
    public EmptyLayerConfigInfo(String layerName, boolean isEquipment) {
        this.LayerName = layerName;
        this.IsEquipment = isEquipment;
        this.HighlightField = "编号";
    }
}