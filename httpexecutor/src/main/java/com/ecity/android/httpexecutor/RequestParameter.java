package com.ecity.android.httpexecutor;

import java.util.Map;

public class RequestParameter {

    protected Map<String, String> requestParams;

    public Map<String, String> generateRequestParams() throws Exception {
        return this.requestParams;
    }

    public boolean validate() {
        return true;
    }

}
