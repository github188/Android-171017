package com.ecity.android.httpexecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by jonathanma on 13/4/2017.
 */

final class RequestExecutorUtil {

    public static JSONObject map2Json(Map<String, String> map) throws JSONException {
        JSONObject json = new JSONObject();
        if (map == null) {
            return json;
        }

        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = keys.iterator().next();
            json.putOpt(key, map.get(key));
        }

        return json;
    }
}
