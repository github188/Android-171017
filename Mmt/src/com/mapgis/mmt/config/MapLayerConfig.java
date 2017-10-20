package com.mapgis.mmt.config;

public class MapLayerConfig {
    public String Name;
    public String Type;
    public String Visible;
    public String Baseflag;
    public String Config = "";
    public String Url = "";
    public String label = "";

    public String cachePath = "";

    public MapLayerConfig() {
    }

    public MapLayerConfig(String name, String type, String visible, String baseflag) {
        Name = name;
        Type = type;
        Visible = visible;
        Baseflag = baseflag;
    }

}
