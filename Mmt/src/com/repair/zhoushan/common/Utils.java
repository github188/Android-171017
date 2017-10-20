package com.repair.zhoushan.common;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultStatus;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class Utils {

    /**
     * 将 ResultData Json字符串转化成 ResultData对象，错误消息以 Toast方式呈现
     */
    public static <T> ResultData<T> resultDataJson2ResultDataToast(Class<T> clz, Context context, String jsonStr, String defErrMsg, boolean allowEmpty) {

        ResultData<T> rawData;
        try {
            if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            rawData = new Gson().fromJson(jsonStr, getType(ResultData.class,clz));

            if (rawData == null) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            if (rawData.ResultCode != 200 || (!allowEmpty && rawData.DataList.size() == 0)) {
                if (!BaseClassUtil.isNullOrEmptyString(rawData.ResultMessage)) {
                    Toast.makeText(context, rawData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                }
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return rawData;
    }

    /**
     * 将 ResultWithoutData Json字符串转化成 ResultWithoutData，错误消息以 Toast方式呈现
     */
    public static ResultWithoutData resultWithoutDataJson2ResultDataToast(Context context, String jsonStr, String defErrMsg,String defSucessMsg) {
        ResultWithoutData rawData;
        try {
            if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            rawData = new Gson().fromJson(jsonStr, ResultWithoutData.class);

            if (rawData == null) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            if (rawData.ResultCode <= 0) {
                Toast.makeText(context, rawData.ResultMessage, Toast.LENGTH_SHORT).show();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        Toast.makeText(context, TextUtils.isEmpty(defSucessMsg)?"操作成功":defSucessMsg, Toast.LENGTH_SHORT).show();
        return rawData;
    }

    /**
     * 将 Results类型的 Json字符串转化成 ResultData对象，错误消息以 Toast方式呈现
     */
    public static <T> ResultData<T> json2ResultDataToast(Class<T> clz, Context context, String jsonStr, String defErrMsg, boolean allowEmpty) {

        ResultData<T> newData;

        try {
            if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            Results<T> rawData = new Gson().fromJson(jsonStr, getType(Results.class, clz));

            if (rawData == null) {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
                return null;
            }

            newData = rawData.toResultData();
            if (newData.ResultCode != 200 || (!allowEmpty && newData.DataList.size() == 0)) {
                if (!BaseClassUtil.isNullOrEmptyString(newData.ResultMessage)) {
                    Toast.makeText(context, newData.ResultMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, newData.ResultMessage, Toast.LENGTH_SHORT).show();
                }
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return newData;
    }

    /**
     * 将 Results类型的 Json字符串转化成 ResultData对象，错误消息呈现在 BaseActivity的 ErrorMessage中
     */
    public static <T> ResultData<T> json2ResultDataActivity(Class<T> clz, BaseActivity activity, String jsonStr, String defErrMsg, boolean allowEmpty) {

        ResultData<T> newData;

        try {
            if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
                activity.showErrorMsg(defErrMsg);
                return null;
            }
            Results<T> rawData = new Gson().fromJson(jsonStr, getType(Results.class, clz));

            if (rawData == null) {
                activity.showErrorMsg(defErrMsg);
                return null;
            }

            newData = rawData.toResultData();
            if (newData.ResultCode != 200 || (!allowEmpty && newData.DataList.size() == 0)) {
                if (!BaseClassUtil.isNullOrEmptyString(newData.ResultMessage)) {
                    activity.showErrorMsg(newData.ResultMessage);
                } else {
                    activity.showErrorMsg(defErrMsg);
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity.showErrorMsg(e.getMessage());
            return null;
        }

        return newData;
    }

    public static boolean json2ResultToast(Context context, String jsonStr, String defErrMsg) {

        if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
            Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            return false;
        }

        ResultStatus rawData = new Gson().fromJson(jsonStr, new TypeToken<ResultStatus>() {
        }.getType());
        if (rawData == null) {
            Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            return false;
        }

        ResultWithoutData newData = rawData.toResultWithoutData();

        if (newData.ResultCode != 200) {
            if (!BaseClassUtil.isNullOrEmptyString(newData.ResultMessage)) {
                Toast.makeText(context, newData.ResultMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        return true;
    }

    public static ResultWithoutData resultjson2ResultWithoutDataToast(Context context, String jsonStr, String defErrMsg) {

        if (BaseClassUtil.isNullOrEmptyString(jsonStr)) {
            Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            return null;
        }

        ResultStatus rawData = new Gson().fromJson(jsonStr, new TypeToken<ResultStatus>() {
        }.getType());
        if (rawData == null) {
            Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            return null;
        }

        ResultWithoutData newData = rawData.toResultWithoutData();

        if (newData.ResultCode<=0) {
            if (!BaseClassUtil.isNullOrEmptyString(newData.ResultMessage)) {
                Toast.makeText(context, newData.ResultMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, defErrMsg, Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        return newData;
    }

    public static ParameterizedType getType(final Class raw, final Type... type) {

        return new ParameterizedType() {

            @Override
            public Type getRawType() {
                return raw;
            }

            @Override
            public Type[] getActualTypeArguments() {
                return type;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
