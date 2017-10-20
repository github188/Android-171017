package com.mapgis.mmt.net;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRequestInterceptorAddHeader implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();

        Request request = originalRequest.newBuilder()
                .header("Accept-Encoding", "gzip")
                .header("User-Agent", "MapGIS.Android.EA")
                .build();

        return chain.proceed(request);
    }
}
