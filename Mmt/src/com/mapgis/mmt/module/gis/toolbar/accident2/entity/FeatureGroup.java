package com.mapgis.mmt.module.gis.toolbar.accident2.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FeatureGroup implements Parcelable {
    public String displayFieldName;
    public ArrayList<FeatureItem> features;    // 要素属性信息
    public ArrayList<FieldBean> fields;            // 属性说明信息
    public LinkedHashMap<String, String> fieldAliases;
    public String geometryType;
    public String hasM;
    public String hasMediaFile;
    public String hasZ;
    public String layerId;
    public String layerName;

    public LinkedHashMap<String,String> getAllVisiableAttribute(int index){
        if (this.fields == null || this.fields.size() == 0){
            return null;
        }
        // 去除不可见的属性
        ArrayList<FieldBean> fields = this.fields;
        List<String> unvisiableFields = new ArrayList<>();
        LinkedHashMap<String, String> attributes = features.get(index).getAttributes();
        LinkedHashMap<String, String> visiableAttributes = new LinkedHashMap<>();
        for (FieldBean field : fields) {
            if (field.isVisible()) {
                String name = field.getName();
                visiableAttributes.put(name,attributes.get(name));
//                unvisiableFields.add(field.getName());
            }
        }
//        LinkedHashMap<String, String> attributes = features.get(index).getAttributes();
//        for (String field : unvisiableFields) {
//            attributes.remove(field);
//        }
        return visiableAttributes;
    }


    public String getDisplayFieldValue(int index){
        if (features == null || index < 0 || index >= features.size()){
            return "";
        }
        FeatureItem featureItem = features.get(index);
        LinkedHashMap<String, String> attributes = featureItem.getAttributes();
        if (attributes == null){
            return "";
        }
//        String value = attributes.get(displayFieldName);
        String value = attributes.get("ID");
        if (value == null){
            value = "";
        }
        return value;
    }

    @Override
    public String toString() {
        return "FeatureGroup{" +
                "displayFieldName='" + displayFieldName + '\'' +
                ", features=" + features +
                '}';
    }

    public String getDisplayFieldName() {
        return displayFieldName;
    }

    public void setDisplayFieldName(String displayFieldName) {
        this.displayFieldName = displayFieldName;
    }

    public ArrayList<FeatureItem> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<FeatureItem> features) {
        this.features = features;
    }

    public ArrayList<FieldBean> getFields() {
        return fields;
    }

    public void setFields(ArrayList<FieldBean> fields) {
        this.fields = fields;
    }

    public LinkedHashMap<String, String> getFieldAliases() {
        return fieldAliases;
    }

    public void setFieldAliases(LinkedHashMap<String, String> fieldAliases) {
        this.fieldAliases = fieldAliases;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public String getHasM() {
        return hasM;
    }

    public void setHasM(String hasM) {
        this.hasM = hasM;
    }

    public String getHasMediaFile() {
        return hasMediaFile;
    }

    public void setHasMediaFile(String hasMediaFile) {
        this.hasMediaFile = hasMediaFile;
    }

    public String getHasZ() {
        return hasZ;
    }

    public void setHasZ(String hasZ) {
        this.hasZ = hasZ;
    }

    public String getLayerId() {
        return layerId;
    }

    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayFieldName);
        dest.writeTypedList(this.features);
        dest.writeList(this.fields);
        dest.writeSerializable(this.fieldAliases);
        dest.writeString(this.geometryType);
        dest.writeString(this.hasM);
        dest.writeString(this.hasMediaFile);
        dest.writeString(this.hasZ);
        dest.writeString(this.layerId);
        dest.writeString(this.layerName);
    }

    public FeatureGroup() {
    }

    protected FeatureGroup(Parcel in) {
        this.displayFieldName = in.readString();
        this.features = in.createTypedArrayList(FeatureItem.CREATOR);
        this.fields = new ArrayList<FieldBean>();
        in.readList(this.fields, FieldBean.class.getClassLoader());
        this.fieldAliases = (LinkedHashMap<String, String>) in.readSerializable();
        this.geometryType = in.readString();
        this.hasM = in.readString();
        this.hasMediaFile = in.readString();
        this.hasZ = in.readString();
        this.layerId = in.readString();
        this.layerName = in.readString();
    }

    public static final Parcelable.Creator<FeatureGroup> CREATOR = new Parcelable.Creator<FeatureGroup>() {
        @Override
        public FeatureGroup createFromParcel(Parcel source) {
            return new FeatureGroup(source);
        }

        @Override
        public FeatureGroup[] newArray(int size) {
            return new FeatureGroup[size];
        }
    };
}
