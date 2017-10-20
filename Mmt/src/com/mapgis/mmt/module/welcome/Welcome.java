package com.mapgis.mmt.module.welcome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.LauncherShortcutUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.common.util.ScreenShot;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.systemsetting.locksetting.util.LoginManager;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.mapgis.mmt.global.MmtBaseTask;

import org.codehaus.jackson.map.ObjectMapper;

import java.net.URLEncoder;
import java.util.Map;

public class Welcome extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            //结束你的activity
            finish();
            return;
        }

        if (!isTaskRoot()) {
            finish();
            return;
        }

        if (MyApplication.getInstance().mapGISFrame != null) {//已经登陆了
            LoginMain();
            return;
        }

        setContentView(R.layout.welcome);

        // Shortcut name refers to the label of Welcome Activity
        String shortcutName = null;
        try {
            PackageManager pm = getPackageManager();
            ActivityInfo activityInfo = pm.getActivityInfo(getComponentName(), 0);
            shortcutName = activityInfo.loadLabel(pm).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(shortcutName)) {
            shortcutName = getString(R.string.app_name);
        }
        if (!LauncherShortcutUtil.checkShortcutExist(this, shortcutName)) {
            LauncherShortcutUtil.addShortcut(this, Welcome.class, shortcutName, R.drawable.icon, false);
        }

        ((TextView) findViewById(R.id.version)).setText(ResourceUtil.getVersionName(Welcome.this));

        // 注意应用打开的时候逻辑是先根据包名启动该应用，（所以忽略第一次传值），随后会根据android自定义的协议去传递数据
        String scheme = getIntent().getScheme();

        // 获取iCome平台传递过来的参数值,sso为自定义的协议
        if (!TextUtils.isEmpty(scheme)) {
            String data = "";

            if (scheme.equals("pgyer")) {
                data = getIntent().getExtras().getString("allLoginMsg");
            } else if (scheme.equals("pipenetenn")) {
                data = getIntent().getDataString().split("=")[1];
            }

            if (!TextUtils.isEmpty(data)) {
                new IComeTask(Welcome.this).execute(scheme, data);

                return;
            }
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginMain();
            }
        }, 1000);
    }

    Handler handler = new Handler();

    private void LoginMain() {
        LoginMain(null);
//        Intent intent = new Intent();
//
//        intent.setClass(Welcome.this, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
//        startActivity(intent);
//        finish();
    }

    /*
     * 进入程序的主界面
     */
    private void LoginMain(ServerConfigInfo info) {
//        Debug.waitForDebugger();
        Bundle bundle = null;
        if (info != null) {
            bundle = new Bundle();
            bundle.putParcelable(LoginManager.PARAM_SERVER_CONFIG_INFO, info);
        }

        if (PasswordManager.isNeedLockScreen()) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString(Welcome.class.getSimpleName(), "");
        }

        LoginManager manager = LoginManager.getInstance(this);
        manager.enterLoginActivity(bundle);
    }

    class IComeTask extends MmtBaseTask<String, Integer, ResultData<ServerConfigInfo>> {
        public IComeTask(Context context) {
            super(context, true, "正在通过云之家验证,请稍候...");
        }

        @Override
        protected ResultData<ServerConfigInfo> doInBackground(String... params) {
            ResultData<ServerConfigInfo> data = new ResultData<>();

            try {
                String json, itcode = "";

                if (getString(R.string.login_url).contains("192.168.12.193")) {
                    itcode = "hefengb";
                } else if (params[0].equals("pgyer")) {
                    //注意需要将data进行encode,里边含有不合法的字符
                    String url = "http://icome.enn.cn/snsapi/open/validateUser.json?source=vyh8B8tgQ7IvfSC7&message="
                            + URLEncoder.encode(params[1], "UTF-8");

                    json = NetUtil.executeHttpGet(url);

                    if (TextUtils.isEmpty(json))
                        throw new Exception("iCome密钥验证失败");

                    itcode = new Gson().fromJson(json, IComeUserResult.class).userInfo.moblie;
                } else if (params[0].equals("pipenetenn")) {
                    String url = "http://183.196.130.125:9302/licensor/access_token";
                    String jsonContent = "{\"appId\":\"app_10000033\",\"appSecret\":\"a9b7c2efa594fa0765d0242434a9a86b\"}";

                    json = NetUtil.executeHttpPost(url, jsonContent);

                    ObjectMapper mapper = new ObjectMapper();

                    Map<?, ?> map = mapper.readValue(json, Map.class);

                    String access_token = String.valueOf(((Map) map.get("data")).get("access_token"));

                    jsonContent = "{\"ticket\":\"" + params[1] + "\",\"access_token\":\"" + access_token + "\"}";

                    url = "http://183.196.130.125:9302/licensor/userinfo";

                    json = NetUtil.executeHttpPost(url, jsonContent);

                    map = mapper.readValue(json, Map.class);

                    itcode = String.valueOf(((Map) map.get("data")).get("account"));
                }

                if (TextUtils.isEmpty(itcode))
                    throw new Exception("没有获取到iCome账号");

                String loginURL = getString(R.string.login_url);
                String isHttps = loginURL.startsWith("https") ? "1" : "0";
                json = NetUtil.executeHttpGet(loginURL, "userName", itcode, "isHttps", isHttps);

                if (TextUtils.isEmpty(json))
                    throw new Exception("iCome统一验证失败");

                data = new Gson().fromJson(json, new TypeToken<ResultData<ServerConfigInfo>>() {
                }.getType());

                if (data.getSingleData() != null)
                    new ScreenShot(false).shoot(Welcome.this);
            } catch (Exception e) {
                e.printStackTrace();

                data.ResultCode = -400;
                data.ResultMessage = e.getMessage();
            }

            return data;
        }

        @Override
        protected void onSuccess(ResultData<ServerConfigInfo> data) {
            try {
                if (data.ResultCode < 0 || data.DataList == null || data.DataList.size() == 0) {
                    Toast.makeText(Welcome.this, "用户验证失败，可能用户不存在，请联系管理员", Toast.LENGTH_SHORT).show();

                    finish();
                } else {

                    ServerConfigInfo serverConfigInfo = data.getSingleData();
                    if (TextUtils.isEmpty(serverConfigInfo.HttpProtocol)) {
                        serverConfigInfo.HttpProtocol = "https";
                    }

                    LoginMain(serverConfigInfo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class IComeUserInfo {
        String moblie;
    }

    class IComeUserResult {
        IComeUserInfo userInfo;
    }
}
