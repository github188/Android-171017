package com.patrol.module.posandpath2.detailinfo;

import android.os.Parcelable;

import java.util.LinkedHashMap;

/**
 * Created by Comclay on 2016/10/28.
 * 详情界面数据
 */

public abstract class DetailInfoMapData implements Parcelable {
    public abstract LinkedHashMap<String, LinkedHashMap<String, String>> toMapData();
}
