package com.ecity.android.httpexecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestExecutor {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private IRequestCallback mCallback;

    private RequestExecutor(IRequestCallback callback) {
        mCallback = callback;
    }

    private static RequestExecutor newInstance(IRequestCallback callback) {
        return new RequestExecutor(callback);
    }

    public static void execute(final IRequestCallback callback) {
        RequestExecutor.newInstance(callback).execute();
    }

    private void execute() {
        Map<String, String> parameters = null;
        try {
            parameters = mCallback.getParameter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }

        if (mCallback.isPost()) {
            asyncPost(mCallback.getUrl(), parameters);
        } else {
            asyncGet(mCallback.getUrl(), parameters);
        }
    }

    private void asyncGet(String url, Map<String, String> parameters) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        Iterator<Map.Entry<String, String>> it = parameters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            urlBuilder.addQueryParameter(entry.getKey(), parameters.get(entry.getKey()));
        }

        String urlStr = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(urlStr)
                .build();

        runRequest(request);
    }

    private void asyncPost(String url, Map<String, String> parameters) {
        JSONObject json = null;
        try {
            json = RequestExecutorUtil.map2Json(parameters);
        } catch (JSONException e) {
            mCallback.onError(e);
            return;
        }

        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        runRequest(request);
    }

    private void runRequest(Request request) {
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mCallback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mCallback.onCompletion(response.body().string());
            }
        });
    }
}