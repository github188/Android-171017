package com.mapgis.mmt.module.systemsetting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordHandleException;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.mapgis.mmt.module.systemsetting.setting.ISettingItem;
import com.mapgis.mmt.module.systemsetting.setting.settingitem.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 新版设置界面
 * 2017-6-14    重构系统设置
 *              每项配置分离，便于项目的定制和SystemSettingFragment的阅读
 *
 */
public class SystemSettingFragment extends Fragment {

    private List<ISettingItem> mSettingItemList;

    public SystemSettingFragment() {

    }

    public static SystemSettingFragment newInstance() {
        return new SystemSettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.system_settings_view, container, false);
        init(v);
        return v;
    }

    /**
     * 初始化设置界面并添加点击事件
     */
    protected void init(View view) {
        try {
            mSettingItemList = new ArrayList<>();

            // 定时定位
            mSettingItemList.add(new RealtimeLocateSettingItem(getActivity(), findView(view, R.id.itemRealtimeLocate)));
            // 定时定位间隔
            mSettingItemList.add(new RealtimeValueSettingItem(getActivity(), findView(view, R.id.itemRealtimeValue)));
            // 流量
//            mSettingItemList.add(new TrafficSettingItem(getActivity(), findView(view, R.id.itemTraffic)));
            // 清理缓存
            mSettingItemList.add(new AppCacheSettingItem(getActivity(), findView(view, R.id.itemClearAppCache)));
            // 清理多媒体文件
            mSettingItemList.add(new MediaSettingItem(getActivity(), findView(view, R.id.itemClearMediaFile)));
            // 大华视频输出比特率设置
            boolean bool = MyApplication.getInstance().hasConnDHPermission();
            View dahuaItem = findView(view, R.id.itemVideoBitRate);
            if (bool) mSettingItemList.add(new DHSettingItem(getActivity(), dahuaItem));
            else dahuaItem.setVisibility(View.GONE);

            // 个性化设置
            mSettingItemList.add(new CustomSettingItem(getActivity(), findView(view, R.id.itemCustomSetting)));
            // 后台统计
            mSettingItemList.add(new BackgroundInfoItem(getActivity(), findView(view, R.id.itemBackgroundInfo)));
            // 扫描
            mSettingItemList.add(new SaoMiaoSettingView(getActivity(), findView(view, R.id.itemSaomiao)));
            // 修改密码
            mSettingItemList.add(new PasswordSettingItem(getActivity(), findView(view, R.id.itemChangePwd)));
            // 屏幕锁频
            mSettingItemList.add(new LockScreenSettingItem(getActivity(), findView(view, R.id.itemLockScreen)));
            // 下载管理
            mSettingItemList.add(new DownloadSettingItem(getActivity(), findView(view, R.id.itemDownloadManager)));
            // 关于
            mSettingItemList.add(new AppAboutSettingItem(getActivity(), findView(view, R.id.itemAppAbout)));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理扫描结果（在界面上显示）
        if (resultCode == Activity.RESULT_OK) {
            String scanResult = data.getStringExtra("code");

            Toast.makeText(getActivity(), scanResult, Toast.LENGTH_SHORT).show();
        } else if (resultCode == Activity.RESULT_CANCELED
                && requestCode == Activity.RESULT_CANCELED) {
            // 确认并清除密码
            try {
                PasswordManager.clearPassword();
                LockScreenSettingItem lockItem = (LockScreenSettingItem) getItemView(R.id.itemLockScreen);
                if (lockItem != null){
                    lockItem.choiceUnlockedType();
                }
            } catch (PasswordHandleException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LockScreenSettingItem lockItem = (LockScreenSettingItem) getItemView(R.id.itemLockScreen);
        if (lockItem != null){
            lockItem.setPasswordTypeText();
        }
    }

    @Override
    public void onDestroy() {
        SharedPreferences uiState = MyApplication.getInstance().getSystemSharedPreferences();

        SharedPreferences.Editor editor = uiState.edit();

        editor.putLong("isRealtimeLocate", MyApplication.getInstance().getConfigValue("isRealtimeLocate", 0));
        editor.putLong("isReportDisplay", MyApplication.getInstance().getConfigValue("isReportDisplay", 0));
        editor.putLong("isInfoConfirm", MyApplication.getInstance().getConfigValue("isInfoConfirm", 0));
        editor.putLong("isWifiConfirm", MyApplication.getInstance().getConfigValue("isWifiConfirm", 0));

        editor.putLong("realtimeLocateInterval", MyApplication.getInstance().getConfigValue("realtimeLocateInterval", 5));
        // 设置是否自动调整 定位时间间隔
        Log.v("SettingsDialog",
                "SettingsDialog.onDestroy.autoAdjustLocateInterval:"
                        + MyApplication.getInstance().getConfigValue("autoAdjustLocateInterval"));

        editor.putLong("autoAdjustLocateInterval", MyApplication.getInstance().getConfigValue("autoAdjustLocateInterval", 0));
        editor.putLong("maxAdjustLocateTimeInterval", MyApplication.getInstance().getConfigValue("maxAdjustLocateTimeInterval", 120));

        editor.putLong("areaSize", MyApplication.getInstance().getConfigValue("areaSize", 2));
        editor.putString("additionalConditionName", MyApplication.getInstance().getConfigValue("additionalConditionName"));

        editor.apply();

        super.onDestroy();
    }

    private ISettingItem getItemView(int id) {
        for (ISettingItem item : mSettingItemList) {
            if (item.getViewId() == id) {
                return item;
            }
        }
        return null;
    }

    private View findView(View view, int id) {
        return view.findViewById(id);
    }
}
