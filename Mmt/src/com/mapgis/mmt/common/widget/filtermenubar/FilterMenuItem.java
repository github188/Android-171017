package com.mapgis.mmt.common.widget.filtermenubar;


import java.util.ArrayList;
import java.util.List;

public class FilterMenuItem {

    private String name;
    private String showName;
    private String parentName;
    private String value;

    private final int orderIndex;

    private List<FilterMenuItem> subMenuItemList;

    public FilterMenuItem() {
        this.name = "";
        this.showName = "";
        this.parentName = "";
        this.value = "";
        this.orderIndex = 0;
        this.subMenuItemList = new ArrayList<FilterMenuItem>();
    }

    public FilterMenuItem(String name, String showName, String parentName, String value, int orderIndex, List<FilterMenuItem> subMenuItemList) {
        this.name = name;
        this.showName = showName;
        this.parentName = parentName;
        this.value = (value == null ? "" : value);
        this.orderIndex = orderIndex;
        this.subMenuItemList = subMenuItemList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<FilterMenuItem> getSubMenuItemList() {
        return subMenuItemList;
    }

    public void setSubMenuItemList(List<FilterMenuItem> subMenuItemList) {
        this.subMenuItemList = subMenuItemList;
    }

    public void clearValue() {
        this.value = "";
    }
}
