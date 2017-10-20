package com.network.request;

import com.ecity.android.httpexecutor.RequestParameter;

import java.util.HashMap;
import java.util.Map;

/***
 * Created by maoshoubei on 2017/10/19.
 */

public class TestConnectionParameters extends RequestParameter {

    private String userId;

    public TestConnectionParameters(String userId) {
        this.userId = userId;
    }

    @Override
    public Map<String, String> generateRequestParams() throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        return map;
    }
}
