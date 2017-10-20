package com.mapgis.mmt.module.webappentry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.login.UserBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by cmios on 2017/5/16.
 */
public class WebAppActivity extends BaseActivity implements HotWebChomeClient.OpenFileChooserCallBack {
    private static final String TAG = "MyActivity";
    private static final int REQUEST_CODE_PICK_IMAGE = 0;
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1;
    private Intent mSourceIntent;
    private ValueCallback<Uri> mUploadMsg;
    private ValueCallback<Uri[]> mUploadMsg5Plus;
    BridgeWebView webView;

    static class User {
        UserBean userBean;
        String origin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSwipeBackEnable(false);

        String pageUrl = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(pageUrl)) {
            MyApplication.getInstance().showMessageWithHandle("路径不能为空");
            return;
        }
        boolean isConfigFullPath = pageUrl.toLowerCase().startsWith("http");
        if (!isConfigFullPath) {
            if (!pageUrl.startsWith("/")) {
                MyApplication.getInstance().showMessageWithHandle("相对路径必须以／开头");
                return;
            }

            pageUrl = ServerConnectConfig.getInstance().getHostPath() + "/CityWebFW/Product/WebApp/#" + pageUrl;
            if (!pageUrl.contains("?")) {
                pageUrl = pageUrl + "?from=android";
            } else {
                pageUrl = pageUrl + "&from=android";
            }
        } else {
            if (!pageUrl.contains("?")) {
                pageUrl = pageUrl + "?from=android";
            } else {
                pageUrl = pageUrl + "&from=android";
            }
        }

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);

        webSettings.setAppCachePath(Battle360Util.getFixedPath(Battle360Util.GlobalPath.Data));
        webSettings.setUseWideViewPort(true);

        webSettings.setLoadWithOverviewMode(true);

        webView.setWebChromeClient(new HotWebChomeClient(this));

        //webView.setWebViewClient(new HotWebViewClient());
        webView.setDefaultHandler(new DefaultHandler());

        fixDirPath();

        webView.loadUrl(pageUrl);

        User user = new User();

        user.userBean = MyApplication.getInstance().getUserBean();

        user.origin = ServerConnectConfig.getInstance().getHostPath();
        webView.registerHandler(getResources().getString(R.string.js_go_map), new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = submitFromWeb, data from web = " + data);

                function.onCallBack("已调用原生的submitFromWeb，这句话是原生回调的");

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("XY")) {
                        String xy = jsonObject.getString("XY");
                        String title = jsonObject.getString("title");
                        BaseMapCallback callback = new ShowMapPointCallback(WebAppActivity.this, xy, title, "", -1);
                        MyApplication.getInstance().sendToBaseMapHandle(callback);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

        webView.callHandler(getResources().getString(R.string.js_get_usr_info), new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                Toast.makeText(WebAppActivity.this, "已获取用户信息", Toast.LENGTH_LONG).show();
            }
        });

        webView.send("hello");
    }

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.webapp_entryview);
        webView = (BridgeWebView) findViewById(R.id.webView);
        defaultBackBtn = getBaseLeftImageView();
        addBackBtnListener(defaultBackBtn);
    }

    @Override
    public void onCustomBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }

        super.onCustomBack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case REQUEST_CODE_IMAGE_CAPTURE:

            case REQUEST_CODE_PICK_IMAGE: {

                try {
                    if (mUploadMsg == null && mUploadMsg5Plus == null) {

                        return;

                    }
                    String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);

                    if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {

                        Log.w(TAG, "sourcePath empty or not exists.");

                        break;
                    }

                    Uri uri = Uri.fromFile(new File(sourcePath));

                    if (mUploadMsg != null) {

                        mUploadMsg.onReceiveValue(uri);

                        mUploadMsg = null;
                    } else {

                        mUploadMsg5Plus.onReceiveValue(new Uri[]{uri});

                        mUploadMsg5Plus = null;
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUploadMsg = uploadMsg;

        showOptions();
    }

    @Override
    public void showFileChooserCallBack(ValueCallback<Uri[]> filePathCallback) {
        mUploadMsg5Plus = filePathCallback;

        showOptions();
    }

    public void showOptions() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setOnCancelListener(new ReOnCancelListener());

        alertDialog.setTitle("图片上传");

        alertDialog.setItems(new String[]{"相册", "拍照"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {

                    mSourceIntent = ImageUtil.choosePicture();

                    startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);

                } else {

                    mSourceIntent = ImageUtil.takeBigPicture();

                    startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);
                }
            }
        });

        alertDialog.show();
    }

    private void fixDirPath() {
        String path = ImageUtil.getDirPath();

        File file = new File(path);

        if (!file.exists()) {

            file.mkdirs();

        }
    }

    private class ReOnCancelListener implements DialogInterface.OnCancelListener {


        @Override
        public void onCancel(DialogInterface dialogInterface) {

            if (mUploadMsg != null) {

                mUploadMsg.onReceiveValue(null);

                mUploadMsg = null;

            }
            if (mUploadMsg5Plus != null) {

                mUploadMsg5Plus.onReceiveValue(null);

                mUploadMsg5Plus = null;

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        webView.removeAllViews();
        webView.destroy();
    }
}