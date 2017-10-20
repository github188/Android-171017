package com.mapgis.mmt.module.navigation;

import java.util.ArrayList;
import java.util.List;

public class NavigationEntity {
    public List<NavigationItem> items;

    /**
     * 菜单名和模块名
     */
    public List<String> functionNames;

    public NavigationEntity(List<NavigationItem> items) {
        this.items = items;

        functionNames = new ArrayList<String>();
        for (NavigationItem item : items) {
            if (!functionNames.contains(item.Function.Name)) {
                functionNames.add(item.Function.Name);
            }
            if (!functionNames.contains(item.Function.Alias)) {
                functionNames.add(item.Function.Alias);
            }
        }
    }

    /**
     * 根据名称获取功能块信息
     */
    public NavigationItem getItemByName(String functionName) {
        NavigationItem alianItem = null;

        for (NavigationItem item : items) {
            if (functionName.equals(item.Function.Name)) {
                return item;
            }

            if (functionName.equals(item.Function.Alias))
                alianItem = item;
        }

        return alianItem;
    }
}
