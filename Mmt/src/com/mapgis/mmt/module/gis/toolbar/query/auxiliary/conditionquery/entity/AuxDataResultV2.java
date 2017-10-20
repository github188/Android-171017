package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by lyunfan on 17/3/17.
 */
public class AuxDataResultV2 {
    public String layerId;
    public String layerName;
    public String displayFieldName;
    public LinkedHashMap<String,String> fieldAliases;
    public List<MapGISField> fields;

    public List<Feature> features;
}

