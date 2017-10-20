package com.mapgis.mmt.entity;

import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * 不带业务数据的返回结果，标志操作结果
 *
 * @author Administrator
 */
public class ResultWithoutData {

    public static final int SUCCEED = 200;

    /**
     * 操作结果码
     */
    public int ResultCode = -100;

    /**
     * 操作结果信息
     */
    public String ResultMessage = "";

    public static ResultWithoutData fromJson(String json) {
        return new Gson().fromJson(json, ResultWithoutData.class);
    }

    public static boolean isEmptyData(ResultData data) {
        return data == null || data.ResultCode < 0 || data.DataList == null || data.DataList.size() == 0;
    }

    public static String getErrMsg(ResultWithoutData data, String msg) {
        return (data == null || TextUtils.isEmpty(data.ResultMessage)) ? msg : data.ResultMessage;
    }

    public static void checkEmptyData(ResultData data, String msg) throws Exception {
        if (isEmptyData(data))
            throw new Exception(getErrMsg(data, msg));
    }
}
