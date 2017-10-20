package com.mapgis.mmt.module.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecity.android.eventcore.EventBusUtil;
import com.ecity.android.eventcore.ResponseEvent;
import com.event.ResponseEventStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.SwipListDialogFragment;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.UserPwdBean;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.mapgis.mmt.module.welcome.Welcome;
import com.mapgis.mmt.net.HttpRequest;
import com.network.LoginService;
import com.zbar.lib.CaptureActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginFragment extends Fragment implements Login.PermissionCheckCallback, Runnable {
    private static final String TAG = "LoginFragment";

    private static final int RC_SCAN_QRCODE = 100;

    protected final SharedPreferences preferences = MyApplication.getInstance().getSystemSharedPreferences();
    protected List<String> usernames = new ArrayList<>();
    protected List<UserPwdBean> userPwdBeans = new ArrayList<>();
    protected boolean isOffline = false;
    protected View view;
    // 用户选择界面
    protected SwipListDialogFragment fragment;

    protected ProgressDialog loadingDialog;

    protected Bundle args;

    protected Boolean outIntentFlag = false;
    protected String outLogName = "";
    protected String outLogPwd = "";
    protected String outPara = "";

    protected EditText txtUserName;
    protected EditText txtPassword;
    protected FrameLayout loginMaskLayer;
    protected Button btnLogin;

    protected CheckBox chbRememberPwd;

    protected NetTestTask mLoginTask;
    protected Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusUtil.register(this);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(0);

        args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        Log.i(TAG, "onCreate: " + args.toString());

        // 第三方登录
        outIntentFlag = args.getBoolean("outIntentFlag", false);
        if (outIntentFlag) {
            outLogName = args.getString("outLoginName");
            outLogPwd = args.getString("outLoginPwd");
            outPara = args.getString("outPara");
        }

        String tip = "正在登录,请稍候......";
        if (args.containsKey("tip"))
            tip = args.getString("tip");

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.MmtBaseThemeAlertDialog);
        loadingDialog = MmtProgressDialog.getLoadingProgressDialog(contextThemeWrapper, tip);
        // 重启
        if (args.containsKey("Restart_ServerConfigInfo")) {
            loadingDialog.setCancelable(false);
        }
    }

    public void hidenLoginDialog() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    protected void handOnResume() {
        try {
            if (view != null) {
                String key = preferences.getString("LoginStyle", "Unify");

                String loginBtnName;
                if ((MyApplication.getInstance().getConfigValue("NeedUnifyLogin", 0) > 0 && key.equals("Unify"))
                        || (args.getParcelable("serverConfigInfo") != null && !args.containsKey("unlockpwd"))) {
                    loginBtnName = "统一登录";
                } else if (args.containsKey("Restart_ServerConfigInfo")) {
                    loginBtnName = "登录";
                } else {
                    loginBtnName = "登录";
                }
                btnLogin.setText(loginBtnName);
            }

            String name = "", pwd = "";

            if (outIntentFlag) {
                name = outLogName;
                pwd = outLogPwd;
            }

            ServerConfigInfo info;
            if ((info = args.getParcelable("serverConfigInfo")) != null
                    || (info = args.getParcelable("Restart_ServerConfigInfo")) != null) {

                args.remove("Restart_ServerConfigInfo");
                ServerConnectConfig.getInstance().getServerConfigInfo().copy(info);

                name = info.LoginName;
                pwd = info.LoginPassword;
            }

            if (!TextUtils.isEmpty(name)) {
                txtUserName.setText(name);
                txtPassword.setText(pwd);

                if (!args.containsKey("unlockpwd"))
                    handler.sendEmptyMessage(outLoginHandlerFlag);
            }

            initUserChosePanel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handOnResume();
    }

    public void resetBG() {
        if (loginMaskLayer.getVisibility() == View.VISIBLE) {
            loginMaskLayer.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }
    }

    protected void maskLoginWithPic(String picPath) {
        if (TextUtils.isEmpty(picPath)) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(picPath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            loginMaskLayer.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            loginMaskLayer.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
        }
        loginMaskLayer.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.logins, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.txtPassword = (EditText) view.findViewById(R.id.txtPassword);
        this.txtUserName = (EditText) view.findViewById(R.id.txtUserName);
        this.loginMaskLayer = (FrameLayout) view.findViewById(R.id.login_mask_layer);
        this.btnLogin = (Button) view.findViewById(R.id.imgLogin);
        this.chbRememberPwd = (CheckBox) view.findViewById(R.id.loginCheckBox);

        if (args.containsKey("bitmap")) {
            maskLoginWithPic(args.getString("bitmap"));
        }

        view.findViewById(R.id.spinnerImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment != null && usernames != null && usernames.size() > 0 && !usernames.get(0).trim().equals("")) {
                    fragment.show(getActivity().getSupportFragmentManager(), "");
                } else {
                    Toast.makeText(getActivity(), "没有可供选择的用户", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (getResources().getBoolean(R.bool.remember_password)) {
            chbRememberPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("rememberPwd", isChecked);
                    editor.apply();
                }
            });
            chbRememberPwd.setChecked(preferences.getBoolean("rememberPwd", false));
        } else {
            chbRememberPwd.setVisibility(View.INVISIBLE);
        }

        txtUserName.setText(preferences.getString("userName", getString(R.string.login_default_user_name)));
        txtPassword.setText(preferences.getString("password", getString(R.string.login_default_password)));

        // Scan QRCode
        view.findViewById(R.id.ibtn_scan_qrcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, RC_SCAN_QRCODE);
            }
        });
        // Version name
        ((TextView) view.findViewById(R.id.version)).setText(ResourceUtil.getVersionName(getContext()));

        bindLoginEvent(view);

        if (args.containsKey("unlockpwd")) {
            btnLogin.performClick();
        }
    }

    public void initUserChosePanel() {
        userPwdBeans.clear();
        List<UserPwdBean> temp = UserPwdBean.query();
        if (temp != null) {
            userPwdBeans = temp;
        }
        usernames.clear();
        for (UserPwdBean userPwdBean : userPwdBeans) {
            usernames.add(userPwdBean.getUsername());
        }

        // 用户选择界面
        fragment = new SwipListDialogFragment("选择用户", usernames);
        fragment.setOnListItemClickListener(new SwipListDialogFragment.OnListItemClickListener() {
            @Override
            public void setOnListItemClick(int position, String value) {
                txtUserName.setText(value);
                txtPassword.setText(userPwdBeans.get(position).getPassword());
            }
        });

        fragment.setOnListItemDeleteClickListener(new SwipListDialogFragment.OnListItemDeleteClickListener() {
            @Override
            public void setOnListItemDeleteClick(int position) {

                SharedPreferences.Editor editor = preferences.edit();

                // 应删除的用户名
                String name = usernames.get(position);

                userPwdBeans.remove(position);
                UserPwdBean.delete(name);
                // 若删除的用户名是默认用户登录名，则将默认用户登录名去除
                String loginName = preferences.getString("userName", "");
                if (name.equals(loginName)) {
                    editor.putString("userName", "");
                    editor.putString("password", "");
                }
                // 若删除的用户名在输入框中，则清空输入框数据
                if (txtUserName.getText().toString().trim().equals(name)) {
                    txtUserName.setText("");
                    txtPassword.setText("");
                }
                editor.apply();
            }
        });

    }

    String logname;
    String password;

    /**
     * 检查手机时间、服务器时间、数据库时间的一致性
     *
     * @throws Exception 不一致异常提示
     */
    private void checkSystemTime() throws Exception {
        if (!getResources().getBoolean(R.bool.check_server_time))
            return;

        String json = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getMobileBusinessURL()
                + "/BaseREST.svc/SystemCurrentTime");

        if (TextUtils.isEmpty(json))
            return;

        ResultData<String> data = new Gson().fromJson(json, new TypeToken<ResultData<String>>() {
        }.getType());

        if (data != null && data.ResultCode <= 0 && !TextUtils.isEmpty(data.ResultMessage))
            throw new Exception(data.ResultMessage);

        if (data != null && data.ResultCode > 0 && data.DataList != null && data.DataList.size() > 0) {
            Date bsTime = BaseClassUtil.parseTime(data.getSingleData());

            if (bsTime == null)
                return;

            Date msTime = new Date();

            if (Math.abs(bsTime.getTime() - msTime.getTime()) > 5 * 60 * 1000)
                throw new Exception("手机时间与服务器时间不一致");
        }
    }

    /**
     * 按钮绑定事件
     */
    protected void bindLoginEvent(final View view) {

        String key = preferences.getString("LoginStyle", "Unify");
        if ((MyApplication.getInstance().getConfigValue("NeedUnifyLogin", 0) > 0 && key.equals("Unify"))
                || args.getParcelable("serverConfigInfo") != null) {
            btnLogin.setText("统一登录");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Login) getActivity()).checkPermissions(true);
            }
        });

        view.findViewById(R.id.loginSettingText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityClassRegistry.getInstance().getActivityClass("登陆设置"));
                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(getActivity());
            }
        });

    }

    /**
     * 登录
     */
    protected void login() {
        logname = txtUserName.getText().toString();
        password = txtPassword.getText().toString();

        if (logname.startsWith("guest") && password.equals("guest")) {
            isOffline = true;

            if (logname.startsWith("guest_")) {

                new MobileConfig().loadByNetState(true);

                MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name = logname.split("_")[1];
            }

            UserBean userBean = new UserBean();
            userBean.LoginName = logname;

            MyApplication.getInstance().putConfigValue("UserBean", userBean);

            processUserInfo();

            return;
        }

        if (logname.length() == 0 || password.length() == 0) {
            Toast.makeText(getActivity(), "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        ServerConfigInfo cfg = ServerConnectConfig.getInstance().getServerConfigInfo();

        LoginService.getInstance().getTestConnectionData("11");//@maoshoubei 测试okHttp框架

        mLoginTask = new NetTestTask(LoginFragment.this, false) {
            @Override
            protected void onSuccess(String msg) {
                try {
                    super.onSuccess(msg);

                    if (!isSuccess)
                        return;

                    if (!LoginFragment.this.loadingDialog.isShowing())
                        LoginFragment.this.loadingDialog.show();

                    MyApplication.getInstance().submitExecutorService(LoginFragment.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        mLoginTask.getLoadingDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mLoginTask.getLoadingDialog().setOnCancelListener(null);
                mLoginTask.cancel(true);
                resetBG();
            }
        });
        mLoginTask.mmtExecute(cfg.HttpProtocol, cfg.IpAddress, cfg.Port, cfg.VirtualPath);
    }

    /**
     * 取消登录
     */
    public void cancelLogin() {
        if (mLoginTask == null || mLoginTask.isCancelled()) {
            return;
        }

        mLoginTask.cancel(true);
        resetBG();
    }

    @Override
    public void run() {
        try {
            checkSystemTime();

            String pwd = password;

            try {
                String loginURL = getActivity().getString(R.string.login_url);
                ServerConfigInfo cfg = ServerConnectConfig.getInstance().getServerConfigInfo();

                String key = preferences.getString("LoginStyle", "Unify");

                boolean isUnify = MyApplication.getInstance().getConfigValue("NeedUnifyLogin", 0) > 0 && key.equals("Unify");

                //非iCome进入的，自带特殊登陆验证的，进行一次LDAP验证
                if (args.getParcelable("serverConfigInfo") == null && isUnify) {
                    String isHttps = loginURL.startsWith("https") ? "1" : "0";
                    String json = NetUtil.executeHttpGet(loginURL,
                            "userName", logname, "pwd", pwd, "corp", "", "isHttps", isHttps);

                    if (!TextUtils.isEmpty(json)) {
                        ResultData<ServerConfigInfo> info = new Gson().fromJson(json, new TypeToken<ResultData<ServerConfigInfo>>() {
                        }.getType());

                        ServerConfigInfo serverConfigInfo = info.getSingleData();
                        if (TextUtils.isEmpty(serverConfigInfo.HttpProtocol)) {
                            serverConfigInfo.HttpProtocol = "https";
                        }

                        pwd = serverConfigInfo.LoginPassword;//已经通过LDAP密码验证的启用超级密码，跳过调度平台二次验证

                        cfg.copy(serverConfigInfo);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //deviceid 由多个标志组成，用于将其他设备踢下线
            String deviceid = ResourceUtil.getVersionName(getContext());

            // 手机序列号，合肥燃气工程管理特有
            String serialnumber = DeviceUtil.getSerialNumber();

            MyApplication.getInstance().putConfigValue("deviceid", deviceid);

            //String json = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getMobileBusinessURL()
            //              + "/BaseREST.svc/UserLogin", "userName", logname, "password", pwd,
            //      "deviceid", deviceid, "serialnumber", serialnumber);

            String json = NetUtil.executeHttpGet("http://10.37.147.80/langfang/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/UserLogin", "userName", logname, "password", pwd,
                    "deviceid", deviceid, "serialnumber", serialnumber);

            ResultData<UserBean> data = new Gson().fromJson(json, new TypeToken<ResultData<UserBean>>() {
            }.getType());

            if (data == null) {
                throw new Exception("可能配置错误或网络不通");
            } else if (data.ResultCode == -500) {
                throw new Exception("原因可能是用户名或密码错误");
            } else if (data.ResultCode <= 0 || data.DataList == null || data.DataList.size() == 0) {
                throw new Exception(data.ResultMessage);
            }

            UserBean bean = data.getSingleData();

            MyApplication.getInstance().putConfigValue("UserBean", bean);

            processUserInfo();
        } catch (Exception ex) {
            ex.printStackTrace();

            Message message = new Message();
            message.what = knownError;

            Bundle bundle = new Bundle();
            bundle.putString("data", ex.getMessage());

            message.setData(bundle);

            handler.sendMessage(message);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1:
                processUserInfo();
                break;
            case RC_SCAN_QRCODE:
                if (resultCode == Activity.RESULT_OK) {
                    String code = data.getExtras().getString("code");
                    handleQRCodeContent(code);
                }
                break;
            default:
                handler.sendEmptyMessage(cancelLogin);
                break;
        }
    }

    private void handleQRCodeContent(final String code) {

        if (TextUtils.isEmpty(code))
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MmtBaseThemeAlertDialog);
        builder.setCancelable(true).setTitle("扫描结果").setMessage(code);

        // Copy to clipboard.
        builder.setNegativeButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager clipboardManager
                        = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", code);
                clipboardManager.setPrimaryClip(clipData);
            }
        });

        // If the content matches a website, open the web browser.
        if (BaseClassUtil.isFullWebUrl(code)) {
            builder.setPositiveButton("访问网页", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri contentUrl = Uri.parse(code);
                    intent.setData(contentUrl);
                    startActivity(intent);
                }
            });
        }
        builder.show();
    }

    protected final int networkError = 0;
    protected final int loginSucessed = 1;
    protected final int loginFailed = 2;
    protected final int cancelLogin = 3;
    protected final int knownError = 4;
    protected final int outLoginHandlerFlag = 5; // 由外部Intent跳转到巡检产品的Handler 标志
    protected final int loginUserOnline = 6; // 用户已在线标志
    protected final int exitApp = 7; // 退出应用程序

    synchronized void processUserInfo() {
        try {
            UserBean bean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

            bean.password = password;
            bean.isOffline = isOffline;

            if (!ServerConfigInfo.isSuperPwd(password)) {//不是超级密码，进行本地保存
                // 直接取到应用程序的SharedPreferences进行操作
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("userId", bean.UserID);
                editor.putString("userName", logname);
                editor.putString("password", password);

                // 在本地保存了用户名和密码
                if (chbRememberPwd.getVisibility() == View.VISIBLE && chbRememberPwd.isChecked()) {
                    UserPwdBean.insert(logname, password);
                } else {// 没有勾选记住密码,则清空该用户密码
                    UserPwdBean.insert(logname, "");
                    editor.putString("password", "");
                }

                editor.apply();
            }

            String proxy = MyApplication.getInstance().getConfigValue("Proxy");

            if (proxy != null && proxy.contains(":")) {
                HttpRequest.setProxy(proxy);
            }

            handler.sendEmptyMessage(loginSucessed);
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(loginFailed);
        }
    }

    private int mloginFailedCount = 0;
    private final static int LOGIN_FAILED_MAX_COUNT = 3;

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case exitApp: // 退出应用程序
                        AppManager.finishProgram(); // AppManager.finishActivity();
                        getActivity().finish();
                        return;
                    case outLoginHandlerFlag:
                        btnLogin.performClick();
                        return;
                    case loginSucessed:
                        if (args.containsKey("forgetpassword")) {
                            showClearScreenPwdDialog();
                        }
                        if (loginSuccessed()) return;
                        return;
                    case loginUserOnline:
                        TextView tv = new TextView(getActivity());
                        tv.setText(msg.obj == null ? "" : msg.obj.toString() + "已经在线，是否继续登录?");

                        OkCancelDialogFragment fragment = new OkCancelDialogFragment("", tv);

                        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                            @Override
                            public void onRightButtonClick(View view) {
                                handler.sendEmptyMessage(loginSucessed);
                            }
                        });

                        fragment.show(getFragmentManager(), "");
                        break;
                    case cancelLogin:
                    case knownError:
                        if (!args.containsKey("forgetpassword")) {
                            Toast.makeText(getActivity(), "登录失败：" + msg.getData().getString("data"), Toast.LENGTH_SHORT).show();
                            break;
                        }

                        if (args.containsKey(Welcome.class.getSimpleName())) {
                            // 解锁界面登录失败
                            Intent intent = new Intent(getActivity(), ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
                            startActivity(intent);
                            getActivity().finish();
                        }
                        mloginFailedCount++;
                        if (mloginFailedCount == LOGIN_FAILED_MAX_COUNT) {
                            // 清除地图数据
                            Toast.makeText(getActivity(), "即将退出应用程序并清理数据！", Toast.LENGTH_SHORT).show();
                            MyApplication.getInstance().clearCache();
                            appExitFuture();
                        } else {
                            Toast.makeText(getActivity(), "登录失败：" + msg.getData().getString("data") + "\n您还有" + (LOGIN_FAILED_MAX_COUNT - mloginFailedCount) + "次登录机会！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        Toast.makeText(getActivity(), "登录失败，可能是配置错误或者网络不通", Toast.LENGTH_SHORT).show();
                        break;
                }

                loadingDialog.dismiss();
                resetBG();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    protected boolean loginSuccessed() {
        for (Context context : AppManager.activityList) {
            if (context instanceof NavigationActivity) {
                // 应用程序已经启动了就不要再配置相应的信息
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                return true;
            }
        }
        new LoginHandleTask().executeOnExecutor(MyApplication.executorService, "");
        return false;
    }

    /**
     * 权限申请成功
     */
    @Override
    public void onSuccess() {
        login();
    }

    @Override
    public void onFailed() {

    }

    class LoginHandleTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                new MobileConfig().loadByNetState(false);

                LayerConfig.getInstance().loadByNetState(isOffline);

                //菜单获取
                return Product.getInstance().loadByNetState(isOffline);
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (getActivity().isFinishing()) {
                    return;
                }

                // 未正常加载成功，弹出提示信息
                if (!result.equals(Product.LOAD_SUCCESS)) {
                    Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();

                    resetBG();
                    return;
                }

                // 初始化程序运行关键参数
                MyApplication.getInstance().putConfigValue("isRealtimeLocate",
                        String.valueOf(preferences.getLong("isRealtimeLocate", 0)));
                MyApplication.getInstance()
                        .putConfigValue("isReportDisplay", String.valueOf(preferences.getLong("isReportDisplay", 0)));
                MyApplication.getInstance().putConfigValue("isInfoConfirm", String.valueOf(preferences.getLong("isInfoConfirm", 0)));
                MyApplication.getInstance().putConfigValue("isWifiConfirm", String.valueOf(preferences.getLong("isWifiConfirm", 0)));
                MyApplication.getInstance().putConfigValue("realtimeLocateInterval",
                        String.valueOf(preferences.getLong("realtimeLocateInterval", 5)));
                MyApplication.getInstance().putConfigValue("areaSize", String.valueOf(preferences.getLong("areaSize", 10)));
                MyApplication.getInstance().putConfigValue("additionalConditionName",
                        preferences.getString("additionalConditionName", "阀门"));

                MyApplication.getInstance().putConfigValue("isWorkTime",
                        preferences.getBoolean("isWorkTime", MyApplication.getInstance().getConfigValue("defaultWorkTime", 1) > 0));

                String key = preferences.getString("GpsReceiver", "");

                if (!TextUtils.isEmpty(key)) {
                    MyApplication.getInstance().putConfigValue("GpsReceiver", key);
                }

                MyApplication.getInstance().putConfigValue(SettingUtil.Config.SHOW_TILE_GRID, SettingUtil.getConfig(SettingUtil.Config.SHOW_TILE_GRID, 0));

                FileUtil.initPrivateMediaVisible();

                enterMapgisActivity();
            } catch (Exception e) {
                e.printStackTrace();
                resetBG();
            } finally {
                loadingDialog.dismiss();
            }
        }
    }

    protected void enterMapgisActivity() {
        Intent intent = new Intent(getActivity(), MapGISFrame.class);

        if (outIntentFlag) {
            intent.putExtra("outIntentFlag", true);
            intent.putExtra("outLoginName", outLogName);
            intent.putExtra("outLoginPwd", outLogPwd);
            intent.putExtra("outPara", outPara);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);

        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
    }

    /**
     * 提示用户是否清除锁屏密码
     */
    private void showClearScreenPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MmtBaseThemeAlertDialog));
        builder.setTitle("提示");
        builder.setMessage("是否清除锁屏密码？");

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 清除锁屏密码
                try {
                    PasswordManager.clearPassword();
                } catch (PasswordHandleException e) {
                    e.printStackTrace();
                }
                loginSuccessed();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 不清除锁屏密码
                dialog.dismiss();
                loginSuccessed();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /**
     * 弹出提示框清除地图数据
     */
    protected void showClearMapDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MmtBaseThemeAlertDialog));
        builder.setTitle("提示");
        builder.setMessage("是否清除地图数据？");
        builder.setPositiveButton("清除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 清除地图数据
                MyApplication.getInstance().clearCache();
                appExitFuture();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 不清除地图数据
                dialog.dismiss();
                handler.sendEmptyMessage(exitApp);
            }
        });
        builder.show();
    }

    /**
     * 应用程序即将退出
     */
    private void appExitFuture() {
        Toast.makeText(getActivity(), "应用程序即将退出！", Toast.LENGTH_LONG).show();
        handler.sendEmptyMessageDelayed(exitApp, 1000);
    }

    @Override
    public void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        EventBusUtil.unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(ResponseEvent event) {
        if (!event.isOK()) {
            return;
        }
        switch (event.getId()) {
            case ResponseEventStatus.LOGIN_TEST_CONNECTION_DATA:
                handleTestConnection(event);
                break;
            default:
                break;
        }
    }

    private void handleTestConnection(ResponseEvent event) {

    }

}