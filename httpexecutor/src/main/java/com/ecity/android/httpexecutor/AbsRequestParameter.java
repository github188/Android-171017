package com.ecity.android.httpexecutor;

import java.util.HashMap;
import java.util.Map;

/***
 * Created by maoshoubei on 2017/10/20.
 */

public abstract class AbsRequestParameter {
    public Map<String, String> toMap() {
        return prepareParameters();
    }

    private Map<String, String> prepareParameters() {
        Map<String, String> map = new HashMap<>();
        setDefaultParameters(map);
        fillParameters(map);

        return map;
    }

    private void setDefaultParameters(Map<String, String> map) {
        map.put("plat", "mobile");
        map.put("f", "json");
    }

    protected abstract void fillParameters(Map<String, String> map);
}
