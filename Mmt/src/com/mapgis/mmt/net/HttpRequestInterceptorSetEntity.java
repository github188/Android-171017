package com.mapgis.mmt.net;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.GzipSource;
import okio.Okio;

public class HttpRequestInterceptorSetEntity implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response originalResponse = chain.proceed(chain.request());

        if (originalResponse.body() == null) {
            return originalResponse;
        }

        Headers headers = originalResponse.headers();

        if (null != headers) {

            for (int i = 0, length = headers.size(); i < length; i++) {
                if ("gzip".equalsIgnoreCase(headers.value(i))) {

                    GzipSource responseBody = new GzipSource(originalResponse.body().source());
                    Headers strippedHeaders = originalResponse.headers().newBuilder()
                            .removeAll("Content-Encoding")
                            .removeAll("Content-Length")
                            .build();

                    return originalResponse.newBuilder()
                            .headers(strippedHeaders)
                            .body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody)))
                            .build();
                }
            }
        }

        return originalResponse;
    }

}
