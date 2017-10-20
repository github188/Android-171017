package com.mapgis.mmt.common.util;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.NetLogInfo;
import com.mapgis.mmt.entity.ResultWithoutData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public final class NetUtil {

    /**
     * 默认读取超时：120秒
     */
    private static final int DEFAULT_READ_TIMEOUT = 120;

    /**
     * 默认写入超时：120秒
     */
    private static final int DEFAULT_WRITE_TIMEOUT = 120;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient mOkHttpClient;

    public static OkHttpClient getHttpClient() {

        if (mOkHttpClient != null) {
            return mOkHttpClient;
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        setProxy(builder);

        mOkHttpClient = builder
                .connectTimeout(10, TimeUnit.SECONDS) // 设置默认连接超时时间 10s
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS) // 设置默认读取超时时间 60s
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS) // 设置默认写入超时时间 60s
                .build();

        return mOkHttpClient;
    }

    private static void setProxy(OkHttpClient.Builder builder) {

        String proxyHost = MyApplication.getInstance().getConfigValue("ProxyHost");

        if (!BaseClassUtil.isNullOrEmptyString(proxyHost)) {

            int proxyPort = Integer.parseInt(MyApplication.getInstance().mapGISFrame.getResources().getString(R.string.login_server_port));
            String proxyPortTemp = MyApplication.getInstance().getConfigValue("ProxyPort");
            if (!BaseClassUtil.isNullOrEmptyString(proxyPortTemp)) {
                proxyPort = Integer.parseInt(proxyPortTemp);
            }

            final String userName = MyApplication.getInstance().getConfigValue("ProxyUserName");
            final String password = MyApplication.getInstance().getConfigValue("ProxyPassword");

            if (!BaseClassUtil.isNullOrEmptyString(userName) && !BaseClassUtil.isNullOrEmptyString(password)) {

                builder.proxyAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(userName, password);
                        return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                    }
                });
            }

            Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxyHost, proxyPort));
            builder.proxy(proxy);
        }
    }

    private static String unescapeUnicode(String str) {

        StringBuffer sb = new StringBuffer();

        Matcher matcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(str);

        while (matcher.find()) {
            matcher.appendReplacement(sb, (char) Integer.parseInt(matcher.group(1), 16) + "");
        }

        matcher.appendTail(sb);

        return sb.toString().replace("\\", "");// 顺便去掉上面的转义字符"\\"
    }

    /**
     * Post json数据（utf-8），返回结果字符串以 utf-8 格式解析并返回
     *
     * @param url         the request url
     * @param jsonContent the content of request body (json string)
     * @param headers     the request header params.
     */
    public static String executeHttpPost(String url, String jsonContent, String... headers) throws Exception {
        return executeHttpPost(url, jsonContent, DEFAULT_READ_TIMEOUT, headers);
    }

    /**
     * Post json数据（utf-8），返回结果字符串以 utf-8 格式解析并返回
     *
     * @param url         the request url
     * @param jsonContent the content of request body (json string)
     * @param readTimeout the request readTimeout
     * @param headers     the request header params.
     */
    public static String executeHttpPost(String url, String jsonContent, int readTimeout, String... headers) throws Exception {
        return executeHttpPost(url, jsonContent, JSON, readTimeout, headers);
    }

    /**
     * Post the string to the specified url.
     *
     * @param url         the request url
     * @param content     the content string of request body
     * @param mediaType   the media type of request body (not header)
     * @param readTimeout the request readTimeout
     * @param headers     the request header params.
     */
    public static String executeHttpPost(String url, String content, MediaType mediaType,
                                         int readTimeout, String... headers) throws Exception {

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, content));

        resolveHeaders(requestBuilder, headers);

        return executeHttpPost(requestBuilder.build(), readTimeout);
    }

    private static void resolveHeaders(Request.Builder requestBuilder, String[] headers) {
        if (null != headers) {
            for (int i = headers.length - 2; i >= 0; i -= 2) {
                requestBuilder.header(headers[i], headers[i + 1]);
            }
        }
    }

    public static String executeHttpPost(String url, byte[] content, String... headers) throws Exception {
        return executeHttpPost(url, content, DEFAULT_READ_TIMEOUT, headers);
    }

    public static String executeHttpPost(String url, byte[] content, int readTimeout, String... headers) throws Exception {
        return executeHttpPost(url, content, readTimeout, DEFAULT_WRITE_TIMEOUT, headers);
    }

    public static String executeHttpPost(String url, byte[] content, int readTimeout, int writeTimeout, String... headers) throws Exception {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, content));

        resolveHeaders(requestBuilder, headers);

        return executeHttpPost(requestBuilder.build(), readTimeout, writeTimeout);
    }

    public static String executeFormHttpPost(String url, List<String> formData, String... headers) throws Exception {

        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        if (null != formData && !formData.isEmpty()) {
            for (int i = 1, length = formData.size(); i < length; i += 2) {
                formBodyBuilder.add(formData.get(i - 1), formData.get(i));
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBodyBuilder.build());

        resolveHeaders(requestBuilder, headers);

        return executeHttpPost(requestBuilder.build());
    }

    public static String executeHttpPost(Request request) throws Exception {
        return executeHttpPost(request, DEFAULT_READ_TIMEOUT);
    }

    private static String executeHttpPost(Request request, int readTimeout) throws Exception {
        return executeHttpPost(request, readTimeout, DEFAULT_WRITE_TIMEOUT);
    }

    private static String executeHttpPost(Request request, int readTimeout, int writeTimeout) throws Exception {
        OkHttpClient clone;

        if (readTimeout != DEFAULT_READ_TIMEOUT || writeTimeout != DEFAULT_WRITE_TIMEOUT) {
            clone = getHttpClient().newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                    .build();
        } else {
            clone = getHttpClient();
        }

        Response response = clone.newCall(request).execute();

        new NetLogInfo().insertDB(response);

        if (!response.isSuccessful()) {
            response.close();
            throw new Exception("Unexpected code : " + response);
        }

        return response.body().string();
    }

    /**
     * 表单提交
     *
     * @param url        url
     * @param params     文本
     * @param files      文件 files的key类似file[0],file[1],file[2],file[3]
     * @param charsetStr 字符集
     */
    public static String executeMultipartHttpPost(String url,
                                                  List<String> params,
                                                  Map<String, File> files,
                                                  String charsetStr,
                                                  String webFormContenttype,
                                                  RequestBody requestBody) throws Exception {

        final Charset charset = Charset.forName(charsetStr);

        MediaType mediaType = null;
        if (!TextUtils.isEmpty(webFormContenttype)) {
            mediaType = MediaType.parse(webFormContenttype);
        }


        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (params != null && params.size() > 1) {
            for (int i = params.size() - 2; i >= 0; i -= 2) {
                builder.addPart(MultipartBody.Part.createFormData(params.get(i),
                        null, RequestBody.create(null, params.get(i + 1).getBytes(charset))));
            }
        }

        if (files != null) {
            Set<String> keySet = files.keySet();
            for (String key : keySet) {
                File tempFile = files.get(key);

                if (requestBody == null) {
                    requestBody = RequestBody.create(mediaType, tempFile);
                }
                if (tempFile.exists()) {
                    builder.addFormDataPart(key, tempFile.getName(), requestBody);
                }
            }
        }

        Request request = new Request.Builder().url(url).post(builder.build()).build();

        return executeHttpPost(request);
    }

    private static long lastNetErrorTime = 0;

    /**
     * 通用HTTP GET 方式的网络请求
     */
    public static String executeHttpGet(String url, String... requestParams) {
        return executeHttpGetAppointLastTime(DEFAULT_READ_TIMEOUT, url, requestParams);
    }

    public static String executeHttpGet(final int readTimeout, String url, String... requestParams) throws Exception {
        Response response = executeGetRequest(readTimeout, url, requestParams);

        return response.body().string();
    }

    /**
     * 通用HTTP GET 方式的网络请求
     */
    public static String executeHttpGetAppointLastTime(final int readTimeout, String url, String... requestParams) {

        try {

            Response response = executeGetRequest(readTimeout, url, requestParams);

            return response.body().string();

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ConnectException) {
                showNewError();
            }
            return null;
        }
    }

    /**
     * GET请求，返回结果字节数组
     */
    public static byte[] executeHttpGetBytes(int readTimeout, String url, String... requestParams) {

        try {

            Response response = executeGetRequest(readTimeout, url, requestParams);
            return response.body().bytes();

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ConnectException) {
                showNewError();
            }
            return null;
        }
    }

    /**
     * GET请求，返回结果数据流
     */
    public static InputStream executeHttpGetInputStream(int readTimeout, String url, String... requestParams) {

        try {

            Response response = executeGetRequest(readTimeout, url, requestParams);
            return response.body().byteStream();

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof ConnectException) {
                showNewError();
            }
            return null;
        }
    }

    public static String resolveRequestURL(String url, String... requestParams) {
        String params = resolveRequestParams(requestParams);

        if (TextUtils.isEmpty(params))
            return url;

        if (url.contains("?")) {
            params = params.replaceFirst("\\?", "&");
        }

        return url + params;
    }

    private static Response executeGetRequest(int readTimeout, String url, String... requestParams) throws Exception {
        OkHttpClient clone;

        if (readTimeout != DEFAULT_READ_TIMEOUT) {
            clone = getHttpClient().newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .build();
        } else {
            clone = getHttpClient();
        }

        String paramStr = resolveRequestParams(requestParams);
        if (!"".equals(paramStr) && url.contains("?")) {
            paramStr = paramStr.replaceFirst("\\?", "&");
        }

        Request request = new Request.Builder()
                .url(url + paramStr)
                .build();

        Response response = clone.newCall(request).execute();

        new NetLogInfo().insertDB(response);

        if (!response.isSuccessful()) {
            response.close();
            throw new Exception("Unexpected code : " + response);
        }

        return response;
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

    public static String resolveRequestParams(Map<String, String> requestParams) {

        if (null == requestParams || requestParams.isEmpty()) {
            return "";
        }

        List<String> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            if (!BaseClassUtil.isNullOrEmptyString(key)) {
                if (value == null) {
                    value = "";
                }

                params.add(key);
                params.add(value);
            }
        }

        return resolveRequestParams(params.toArray(new String[params.size()]));
    }


    private static synchronized void showNewError() {
        long now = new Date().getTime();

        if ((now - lastNetErrorTime) > 5 * 60 * 1000) {
            MyApplication.getInstance().showMessageWithHandle("当前网络不可用");

            lastNetErrorTime = now;
        }
    }

    public static String downloadStringResource(String path) {
//        String url = ServerConnectConfig.getInstance().getMobileBusinessURL() +
//                "/BaseREST.svc/DownloadStringResource?path=" + path;

        String url = "http://10.37.147.80/langfang/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/DownloadStringResource?path=" + path;

        String data = executeHttpGetAppointLastTime(60, url);

        if (TextUtils.isEmpty(data))
            return data;

        String unescapeData = null;

        try {
            unescapeData = unescapeUnicode(data);

            if (unescapeData.trim().length() > 0) {
                unescapeData = unescapeData.substring(1, unescapeData.length() - 1);// 去除起始点的单引号
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return unescapeData;
    }

    public static byte[] downloadByteResource(String path) {
        try {

            Request request = new Request.Builder()
                    .url(ServerConnectConfig.getInstance().getMobileBusinessURL()
                            + "/BaseREST.svc/DownloadByteResource?path=" + Uri.encode(path))
                    .build();

            Response response = getHttpClient().newCall(request).execute();

            if (!response.isSuccessful()) {
                response.close();
                throw new Exception("Unexpected code : " + response);
            }

            return response.body().bytes();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 下载附属文件信息 /OutFiles/UpLoadFiles/
     *
     * @param uriPath 相对路径
     * @param file    本地文件名
     */
    public static void downloadFile(String uriPath, File file) {
        FileOutputStream outputStream = null;
        BufferedInputStream inputStream = null;

        Response response = null;
        try {

            Request request = new Request.Builder()
                    .url(ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                            + "/OutFiles/UpLoadFiles/" + uriPath)
                    .build();

            response = getHttpClient().newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code : " + response);
            }

            inputStream = new BufferedInputStream(response.body().byteStream());

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            outputStream = new FileOutputStream(file);

            byte[] bt = new byte[1024 * 8];

            int len = inputStream.read(bt);

            while (len != -1) {
                outputStream.write(bt, 0, len);
                len = inputStream.read(bt);
            }
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
//            MyApplication.getInstance().showMessageWithHandle("下载照片失败...");
            if (file != null) file.delete();

        } finally {

            try {

                if (null != inputStream) {
                    inputStream.close();
                }

                if (null != outputStream) {
                    outputStream.close();
                }

                if (null != response) {
                    response.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadFileV2(String uriFullPath, File file) {
        FileOutputStream outputStream = null;
        BufferedInputStream inputStream = null;

        Response response = null;
        try {

            Request request = new Request.Builder()
                    .url(uriFullPath)
                    .build();

            response = getHttpClient().newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code : " + response);
            }

            inputStream = new BufferedInputStream(response.body().byteStream());

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            outputStream = new FileOutputStream(file);

            byte[] bt = new byte[1024 * 8];

            int len = inputStream.read(bt);

            while (len != -1) {
                outputStream.write(bt, 0, len);
                len = inputStream.read(bt);
            }
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            MyApplication.getInstance().showMessageWithHandle("下载照片失败...");

        } finally {

            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != response) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File downloadTempFile(String url) {

        FileOutputStream os = null;
        BufferedInputStream is = null;

        Response response = null;
        try {

            Request request = new Request.Builder().url(url).build();

            response = getHttpClient().newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new Exception("Unexpected code : " + response);
            }

            is = new BufferedInputStream(response.body().byteStream());

            File tmpFile = File.createTempFile("net", null);

            os = new FileOutputStream(tmpFile);

            byte[] buffer = new byte[100 * 1024];//100K缓存

            int len;

            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            os.flush();

            return tmpFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != os) {
                    os.close();
                }
                if (null != response) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public interface NetGoodCallBack {
        void callBack();
    }

    /**
     * 测试与服务器的连接状态
     *
     * @return 是否通畅
     */
    public static boolean testNetState() {
        try {

            boolean pingip = 1 == Integer.valueOf(MyApplication.getInstance().getApplicationContext()
                    .getResources().getString(R.string.pingip));
            String pingip_oms_config = MyApplication.getInstance().getConfigValue("pingip");
            if (!TextUtils.isEmpty(pingip_oms_config)) {
                pingip = Integer.valueOf(pingip_oms_config) == 1;
            }
            if (pingip) {
                // ping服务器ip，平均延迟0-3000ms
                String ipAddress = ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress;

                return PingUtil.availableIPAddress(ipAddress);
            } else {

                //经查证，目前所有调用该方法的地方都在子线程中，此处可以直接调用下面的方法
                return testDb();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean testDb() {

        String url = ServerConnectConfig.getInstance()
                .getMobileBusinessURL() + "/BaseREST.svc/TestDB";

        String result = executeHttpGet(url);

        if (!BaseClassUtil.isNullOrEmptyString(result)) {
            int code = new Gson().fromJson(result, ResultWithoutData.class).ResultCode;
            return code > 0;
        }
        return false;
    }

    public static boolean testFileExist(String path) {
        try {
            String url = ServerConnectConfig.getInstance()
                    .getMobileBusinessURL() + "/BaseREST.svc/TestFileExist";

            String result = executeHttpGet(url, "path", path);

            if (!BaseClassUtil.isNullOrEmptyString(result)) {
                int code = new Gson().fromJson(result, ResultWithoutData.class).ResultCode;
                return code > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean testServiceExist(String url) {
        Response response = null;

        try {
            OkHttpClient clone = getHttpClient().newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder().url(url).build();

            response = clone.newCall(request).execute();

            new NetLogInfo().insertDB(response);

            return response.code() != 404;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return false;
    }
}
