package com;

import com.ecity.android.log.LogUtil;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.L;

public class GsonUtil {
    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        try {
            return gson.toJson(obj, obj.getClass());
        } catch (Exception e) {
            LogUtil.e("GsonUtil", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String json, Class<T> cls) throws Exception {
        try {
            return gson.fromJson(json, cls);
        } catch (Exception e) {
            LogUtil.e("GsonUtil", e);
            throw new Exception(e);
        }
    }
}
