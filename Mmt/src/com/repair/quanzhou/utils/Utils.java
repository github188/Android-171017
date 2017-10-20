package com.repair.quanzhou.utils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;

/**
 * Created by liuyunfan on 2016/1/21.
 */
public class Utils {
    public final static String baseUrl = "http://" + (BaseClassUtil.isNullOrEmptyString(MyApplication.getInstance().getConfigValue(
            "IPPort")) ? "200.200.200.231:80" : MyApplication.getInstance().getConfigValue(
            "IPPort")) + "/workTask";
}
