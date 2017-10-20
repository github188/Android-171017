package com.ecity.android.httpexecutor;

import java.util.Map;

public interface IRequestCallback {

    boolean isPost();

    String getUrl();

    Map<String, String> getParameter() throws Exception;

    void onCompletion(String response);

    void onError(Throwable e);
}
