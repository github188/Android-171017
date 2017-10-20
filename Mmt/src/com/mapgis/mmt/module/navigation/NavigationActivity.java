package com.mapgis.mmt.module.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.util.ScreenShot;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnLeftButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.AppVersionManager;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.systemsetting.download.DownloadService;
import com.mapgis.mmt.module.systemsetting.download.DownloadType;
import com.mapgis.mmt.net.update.MmtUpdateService;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationActivity extends BaseActivity {

    protected long touchTime;

    public static final String BACK_TO_MODULE = "backToModule";

    /**
     * 标识此次运行 后 是否进行过 版本更新检测， 首次进入APP检测一次后不再检测
     */
    public static Boolean updateChecked = false;
    /**
     * 标识此次运行 后 是否进行过 地图文件存在性 检测， 首次进入APP检测一次后不再检测
     */
    public static Boolean mapDataFileChecked = false;

    private Fragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            Class<?> naviFragmentClz = ActivityClassRegistry.getInstance().getActivityClass("导航界面");

            if (naviFragmentClz == null) {
                fragment = new NavigationMainFragment();
            } else {
                fragment = (Fragment) naviFragmentClz.newInstance();
            }
            addFragment(fragment);

            setTopViewVisibility(View.GONE);

            setSwipeFinish(false);

            // 若系系统有更新，则不再检查App更新
            if (!checkSystemUpdate()) {
                checkVersion();
            }

            if (getIntent().hasExtra(BACK_TO_MODULE)) {
                backToModule(getIntent().getStringExtra(BACK_TO_MODULE));
                getIntent().removeExtra(BACK_TO_MODULE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 检查系统更新，需要刷机
     *
     * @return 是否有系统更新
     */
    private boolean checkSystemUpdate() {

        // 型号:版本号，机型间分号;分隔（注意符号都是西文符号）
        String newestSystemInfo = MyApplication.getInstance().getConfigValue("NewestSystemInfo");

        if (!TextUtils.isEmpty(newestSystemInfo)) {

            String[] buildInfo = DeviceUtil.getBuildInfoByKeys(
                    new String[]{DeviceUtil.KEY_DEVICE_MODEL, DeviceUtil.KEY_DEVICE_BUILD_VERSION});

            String[] systemInfos = newestSystemInfo.split(";");
            for (String systemInfo : systemInfos) {
                String[] info = systemInfo.split(":");
                if (info.length != 2) {
                    continue;
                }

                // 设备型号相同而系统版本与配置的不同则需要刷新系统
                if (buildInfo[0].equalsIgnoreCase(info[0].trim())) {
                    if (!buildInfo[1].equalsIgnoreCase(info[1].trim())) {
                        String tip = String.format(Locale.CHINA, "系统更新：检查到新版本 %s，需刷机到新版本后才能继续使用！", info[1]);
                        OkDialogFragment dialogFragment = new OkDialogFragment(tip);
                        dialogFragment.setOnButtonClickListener(new OkDialogFragment.OnButtonClickListener() {
                            @Override
                            public void onButtonClick(View view) {
                                NavigationController.exitApp(NavigationActivity.this);
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "sysupdate");
                        dialogFragment.setCancelable(false);

                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().getBooleanExtra("needClearMap", false)) {
            getIntent().removeExtra("needClearMap");
            MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                @Override
                public boolean handleMessage(Message msg) {
                    mapGISFrame.resetMenuFunction();

                    return false;
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("updateChecked", updateChecked);
        saveNavigations();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateChecked = savedInstanceState.getBoolean("updateChecked", false);
    }

    @Override
    public void onBackPressed() {
        if (MyApplication.getInstance().getConfigValue("NavigationBackMode", 0) == 1) {

            String exitTip = "确认退出" + getString(R.string.app_name);
            if (MmtUpdateService.isRunning || DownloadService.isRunning) {
                exitTip = "地图更新中," + exitTip;
            }
            OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment(exitTip);
            okCancelDialogFragment.setLeftBottonText("继续使用");
            okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    NavigationController.exitApp(NavigationActivity.this);
                }
            });
            okCancelDialogFragment.show(getSupportFragmentManager(), "");

        } else {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - touchTime) > 2000) {
                MyApplication.getInstance().showMessageWithHandle("再按一次退出程序");

                touchTime = currentTime;
            } else {
                if (DownloadService.isRunning || MmtUpdateService.isRunning) {
                    final OkCancelDialogFragment fragment = new OkCancelDialogFragment(
                            "正在更新地图");

                    fragment.setLeftBottonText("强制退出");
                    fragment.setRightBottonText("继续下载");

                    fragment.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {

                        @Override
                        public void onLeftButtonClick(View view) {
                            NavigationController.exitApp(NavigationActivity.this);
                        }
                    });

                    fragment.show(getSupportFragmentManager(), "");
                } else {
                    NavigationController.exitApp(NavigationActivity.this);
                }
            }
        }
    }

    public void onFragmentViewCreated(View view) {
        setActionBarBG();

        final ServerConfigInfo info = ServerConnectConfig.getInstance().getServerConfigInfo();

        if (!info.IsTopCorp)
            return;

        TextView tvTopBar = (TextView) view.findViewById(R.id.baseActionBarTextView);

        tvTopBar.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_white_down, 0);

        tvTopBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeStation(info);
            }
        });
    }

    /*新奥却换站点*/
    protected void exchangeStation(final ServerConfigInfo info) {
        try {
            List<String> corps = new ArrayList<>(info.Corps);

            corps.remove(info.CorpName);

            ListDialogFragment dialogFragment = new ListDialogFragment("切换站点", corps);

            dialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    new MmtBaseTask<String, Integer, ResultData<ServerConfigInfo>>(NavigationActivity.this, true, "正在切换，请稍候...") {
                        @Override
                        protected ResultData<ServerConfigInfo> doInBackground(String... params) {
                            return getServerConfigData(params[0], info);
                        }

                        @Override
                        protected void onPostExecute(ResultData<ServerConfigInfo> data) {
                            try {
                                if (data == null) {
                                    Toast.makeText(NavigationActivity.this, "切换失败,可能企业中不存在当前用户", Toast.LENGTH_SHORT).show();

                                    loadingDialog.dismiss();

                                    return;
                                }

                                renterLoginActivity(data.getSingleData());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.mmtExecute(value);
                }
            });

            dialogFragment.show(getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    protected ResultData<ServerConfigInfo> getServerConfigData(String param, ServerConfigInfo info) {
        try {
            String corp = param;

            String loginURL = getString(R.string.login_url);
            String isHttps = loginURL.startsWith("https") ? "1" : "0";
            String json = NetUtil.executeHttpGet(getString(R.string.login_url),
                    "userName", info.LoginName, "pwd", "", "corp", corp, "isHttps", isHttps);

            if (TextUtils.isEmpty(json))
                return null;

            ResultData<ServerConfigInfo> data = new Gson().fromJson(json,
                    new TypeToken<ResultData<ServerConfigInfo>>() {
                    }.getType());

            if (data == null || TextUtils.isEmpty(data.ResultMessage))
                return null;

            if (data.ResultCode > 0 && data.DataList != null && data.DataList.size() > 0) {
                if (!data.getSingleData().CorpName.equals(corp))
                    return null;

                new ScreenShot(false).shoot(NavigationActivity.this);

                return data;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    protected void renterLoginActivity(ServerConfigInfo serverConfigInfo) {
        Intent intent = new Intent(NavigationActivity.this, ActivityClassRegistry.getInstance().getActivityClass("登录界面"));
        if (TextUtils.isEmpty(serverConfigInfo.HttpProtocol)) {
            serverConfigInfo.HttpProtocol = "https";
        }
        intent.putExtra("serverConfigInfo", serverConfigInfo);
        intent.putExtra("bitmap", "sdcard/xx.png");
        intent.putExtra("tip", "正在加载菜单,请稍候...");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);

        finish();
        overridePendingTransition(0, 0);
    }

    private void setActionBarBG() {
        //  hasSetActionBarBG = true;
        View rootView = fragment.getView();
        if (rootView == null) {
            return;
        }
        View actionBar = rootView.findViewById(R.id.mainActionBar);
        if (actionBar == null) {
            return;
        }
        actionBar.setBackgroundResource(AppStyle.getActionBarStyleResource());
    }

    private void backToModule(String functionName) {

        final ArrayList<ArrayList<NavigationItem>> navigationItemList = ((FragmentCallback) fragment).getNavigationItemList();
        if (navigationItemList == null) {
            return;
        }

        for (int i = 0, groupCount = navigationItemList.size(); i < groupCount; i++) {
            List<NavigationItem> groupItems = navigationItemList.get(i);

            for (int j = 0; j < groupItems.size(); j++) {

                if (groupItems.get(j).Function.Name.equals(functionName)) {

                    menu = NavigationMenuRegistry.getInstance()
                            .getMenuInstance(this, groupItems.get(j));
                    menu.onItemSelected();

                    break;
                }
            }
        }
    }

    /**
     * 检测是否需要有版本更新
     */
    private void checkVersion() {
        if (!updateChecked) {
            updateChecked = true;
            NavigationActivity.this.setBaseProgressBarVisibility(true);
            AppVersionManager appVersionManager = new AppVersionManager(
                    NavigationActivity.this, "", "");
            appVersionManager.alertWhenNoNewVersion = false; // 设置 没有 新版本时
            // 不弹出
            // 更新提示框
            appVersionManager.showToast(false);
            appVersionManager.start();
        }

        if (!mapDataFileChecked) {
            mapDataFileChecked = true;
            if (DownloadType.hasOfflineMap()) {
                // 只有配置了离线地图才会检查更新
                NavigationController.checkMobileFileUpdate(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveNavigations();
    }

    private void saveNavigations() {
        ArrayList<ArrayList<NavigationItem>> navi = ((FragmentCallback) fragment).getNavigationItemList();
        if (navi == null) {
            return;
        }
        MyApplication.getInstance().putConfigValue("navigationGroup", navi);
    }

    protected BaseNavigationMenu menu;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (menu != null && menu.onActivityResult(resultCode, data)) {
            return;
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        setResult(resultCode, data);
        finish();
    }

    public void onNavigationItemClick(NavigationItem item) {

        String moduleName = item.Function.Name;
        if (moduleName.contains("/") || moduleName.contains(".")) {
            item.Function.Name = "Web入口";
            item.Function.ModuleParam = moduleName;
        }
        this.menu = NavigationMenuRegistry.getInstance().getMenuInstance(this, item);

        this.menu.onItemSelected();

        if (this.menu.item.Function.Name.equals("地图浏览")) {
            this.overridePendingTransition(0, 0);
        } else {
            MyApplication.getInstance().startActivityAnimation(this);
        }
    }

    public void onNewTipReceive() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((FragmentCallback) fragment).onNewTipReceive();
            }
        });
    }

    public interface FragmentCallback {
        ArrayList<ArrayList<NavigationItem>> getNavigationItemList();

        void onNewTipReceive();
    }
}
