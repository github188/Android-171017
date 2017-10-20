package com.mapgis.mmt.net;

import android.net.Uri;

import com.mapgis.mmt.common.util.NetUtil;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static OkHttpClient mOkHttpClient;

    static {
        mOkHttpClient = NetUtil.getHttpClient().newBuilder().build();

//        mOkHttpClient = NetUtil.getHttpClient().newBuilder()
//                .connectTimeout(10_000, TimeUnit.MILLISECONDS)
//                .readTimeout(60_1000, TimeUnit.MILLISECONDS)
//                .addInterceptor(new HttpRequestInterceptorAddHeader())
//                .addNetworkInterceptor(new HttpRequestInterceptorSetEntity())
//                .build();
    }

    public static void setProxy(String proxyStr) {

        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(proxyStr.split(":")[0], Integer.valueOf(proxyStr.split(":")[1])));

        mOkHttpClient = mOkHttpClient.newBuilder().proxy(proxy).build();
    }

    protected static String executeHttpGet(String url, String... requestParams) throws Exception {

        Request request = new Request.Builder()
                .url(url + resolveRequestParams(requestParams))
                .build();

        Response response = mOkHttpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            response.close();
            throw new Exception("Unexpected code : " + response);
        }

        return response.body().string();
    }

    protected static String executeFormHttpPost(String url, List<String> formData, String... headers) throws Exception {

        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        if (null != formData && !formData.isEmpty()) {
            for (int i = 1, length = formData.size(); i < length; i += 2) {
                formBodyBuilder.add(formData.get(i - 1), formData.get(i));
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBodyBuilder.build());

        if (null != headers) {
            for (int i = headers.length - 2; i >= 0; i -= 2) {
                requestBuilder.header(headers[i], headers[i + 1]);
            }
        }

        Response response = mOkHttpClient.newCall(requestBuilder.build()).execute();

        if (!response.isSuccessful()) {
            response.close();
            throw new Exception("Unexpected code : " + response);
        }

        return response.body().string();

    }

    private static String resolveRequestParams(String[] params) {

        if (null != params && params.length > 1) {

            StringBuilder sb = new StringBuilder("?");
            for (int i = 1; i < params.length; i += 2) {
                sb.append(Uri.encode(params[i - 1])).append("=").append(Uri.encode(params[i])).append("&");
            }

            return sb.deleteCharAt(sb.lastIndexOf("&")).toString(); // 去除末尾的 & 符号

        } else {
            return "";
        }
    }

    public static final boolean mapToList(String url, Map<String, String> parameters, List<String> paramList) {

        if ((parameters == null) || (parameters.isEmpty())) {
            return true;
        }

        int i = url.length();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!isStringNullOrEmpty(key)) {
                if (value == null) {
                    value = "";
                }

                paramList.add(key);
                paramList.add(value);

                i += key.length() + value.length() + 2;
            }
        }

        return i < 2000;
    }

    private static final boolean isStringNullOrEmpty(String string) {
        return (string == null) || (string.length() < 1);
    }

//	public static final String encoding = "UTF-8";
//	public static final String userAgent = "MapGIS.Android.EA";
//	public static final int stringLen = 8192;
//	protected static final DefaultHttpClient httpClient;
//	static {
//		BasicHttpParams basicHttpParams = new BasicHttpParams();
//		HttpProtocolParams.setVersion(basicHttpParams, HttpVersion.HTTP_1_1);
//		HttpProtocolParams.setContentCharset(basicHttpParams, "UTF-8");
//
//		HttpConnectionParams.setStaleCheckingEnabled(basicHttpParams, false);
//		HttpConnectionParams.setConnectionTimeout(basicHttpParams, 10 * 1000);// 连接超时限制：10秒
//		HttpConnectionParams.setSoTimeout(basicHttpParams, 300 * 1000);// 读取数据超时限制:300秒
//		HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);
//
//		HttpClientParams.setRedirecting(basicHttpParams, true);
//
//		HttpProtocolParams.setUserAgent(basicHttpParams, userAgent);
//
//		SchemeRegistry schemeRegistry = new SchemeRegistry();
//		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
//
//		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry);
//		httpClient = new DefaultHttpClient(threadSafeClientConnManager, basicHttpParams);
//
//		httpClient.addRequestInterceptor(new HttpRequestInterceptorAddHeader());
//
//		httpClient.addResponseInterceptor(new HttpRequestInterceptorSetEntity());
//	}

}
