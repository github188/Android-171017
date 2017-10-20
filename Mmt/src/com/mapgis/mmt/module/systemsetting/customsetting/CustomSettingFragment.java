package com.mapgis.mmt.module.systemsetting.customsetting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.module.systemsetting.itemwidget.SwitchItemSettingView;
import com.mapgis.mmt.receiver.NetPingReceiver;
import com.simplecache.ACache;

import java.util.Locale;

/**
 * 新版设置界面
 */
public class CustomSettingFragment extends Fragment implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ACache mCache;

    private SwitchItemSettingView mItemCamera;
    private SwitchItemSettingView mitemPatrolReport;
    private SwitchItemSettingView mItemArrivedTip;
    private SwitchItemSettingView mItemGPSTip;
    private SwitchItemSettingView mItemGPSAccracyTip;
    private SwitchItemSettingView mItemNewTip;
    private SwitchItemSettingView mItemNetPing;
    private SwitchItemSettingView mItemTileGrid;

    public CustomSettingFragment() {

    }

    public static CustomSettingFragment newInstance() {
        return new CustomSettingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.custom_settings_view, container, false);
        init(v);
        initData();
        return v;
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (mCache == null) {
            mCache = BaseClassUtil.getConfigACache();
        }
        initCamera();
        initArrivedTip();
        initGpsTip();
        initGpsAccuracyTip();
        initNewTip();

        initPatrolReport();

        initCheckNetPing();
    }

    private void initCheckNetPing() {
        boolean isPingNet = SettingUtil.getConfig(SettingUtil.Config.PING_NET_DELAY, true);
        mItemNetPing.setSwitchChecked(isPingNet);

        mItemTileGrid.setSwitchChecked(MyApplication.getInstance().getConfigValue(SettingUtil.Config.SHOW_TILE_GRID, false));
    }

    private void initPatrolReport() {
        mitemPatrolReport.setVisibility(View.GONE);
        if (MyApplication.getInstance().getConfigValue("Album_Photo_PatrolReport", 0) == 1) {
            mitemPatrolReport.setVisibility(View.VISIBLE);
            mitemPatrolReport.setSwitchChecked(false);
            if (MyApplication.getInstance().getConfigValue("choseFromAlbum", 0) == 1) {
                mitemPatrolReport.setSwitchChecked(true);
            }
        }
    }

    private void initNewTip() {
        boolean isNewChecked = !"0".equals(mCache.getAsString("newTip"));
        mItemNewTip.setSwitchChecked(isNewChecked);
    }

    private void initArrivedTip() {
        boolean isArrivedChecked = !"0".equals(mCache.getAsString("arrivedTip"));
        mItemArrivedTip.setSwitchChecked(isArrivedChecked);
    }

    private void initGpsTip() {
        boolean isGpsTip = !"0".equals(mCache.getAsString("GPSSignalTip"));
        mItemGPSTip.setSwitchChecked(isGpsTip);
    }

    private void initGpsAccuracyTip() {
        boolean isGpsAccuracyTip = !"0".equals(mCache.getAsString("GPSAccuracyTip"));
        mItemGPSAccracyTip.setSwitchChecked(isGpsAccuracyTip);
    }

    /**
     * 初始化相机的选择
     */
    private void initCamera() {
        //相机
        boolean isCustromPhoto = MyApplication.getInstance().getConfigValue("MmtCamera", 0) == 1;
        ACache mCache = BaseClassUtil.getConfigACache();
        String cacheMmtCamera = mCache.getAsString("MmtCamera");
        if (!TextUtils.isEmpty(cacheMmtCamera)) {
            isCustromPhoto = cacheMmtCamera.equals("1");
            MyApplication.getInstance().putConfigValue("MmtCamera", cacheMmtCamera);
        }
        mItemCamera.setSwitchChecked(isCustromPhoto);
        String msg = String.format(Locale.CHINA, getString(R.string.text_desc_camera)
                , getCheckedString(isCustromPhoto));
        mItemCamera.setMessage(msg);
    }


    private String getCheckedString(boolean isChecked) {
        return isChecked ? "是" : "否";
    }

    /**
     * 初始化设置界面并添加点击事件
     */
    protected void init(View view) {
        try {
            // 相机
            mItemCamera = (SwitchItemSettingView) view.findViewById(R.id.itemCamera);
            mItemCamera.setOnCheckedChangeListener(this);

            // 巡检上报是否允许从相册选取
            mitemPatrolReport = (SwitchItemSettingView) view.findViewById(R.id.itemPatrolReport);
            mitemPatrolReport.setOnCheckedChangeListener(this);

            mItemArrivedTip = (SwitchItemSettingView) view.findViewById(R.id.itemArrviedTip);
            mItemArrivedTip.setOnCheckedChangeListener(this);

            mItemGPSTip = (SwitchItemSettingView) view.findViewById(R.id.itemGPSTip);
            mItemGPSTip.setOnCheckedChangeListener(this);

            mItemGPSAccracyTip = (SwitchItemSettingView) view.findViewById(R.id.itemGPSAccuracyTip);
            mItemGPSAccracyTip.setOnCheckedChangeListener(this);

            mItemNewTip = (SwitchItemSettingView) view.findViewById(R.id.itemNewTip);
            mItemNewTip.setOnCheckedChangeListener(this);

            // 设置是否显示网络延迟的悬浮球
            mItemNetPing = (SwitchItemSettingView) view.findViewById(R.id.itemNetPing);
            mItemNetPing.setOnCheckedChangeListener(this);

            mItemTileGrid = (SwitchItemSettingView) view.findViewById(R.id.itemTileGrid);
            mItemTileGrid.setOnCheckedChangeListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(mItemCamera.getSwitchButton())) {
            MyApplication.getInstance().putConfigValue("MmtCamera", isChecked ? 1 : 0);
            ACache mCache = BaseClassUtil.getConfigACache();
            mCache.put("MmtCamera", isChecked ? "1" : "0");
            String msg = String.format(Locale.CHINA, getString(R.string.text_desc_camera)
                    , getCheckedString(isChecked));
            mItemCamera.setMessage(msg);
        } else if (buttonView.equals(mItemArrivedTip.getSwitchButton())) {
            mCache.put("arrivedTip", isChecked ? "1" : "0");
        } else if (buttonView.equals(mItemGPSTip.getSwitchButton())) {
            mCache.put("GPSSignalTip", isChecked ? "1" : "0");
        } else if (buttonView.equals(mItemGPSAccracyTip.getSwitchButton())) {
            mCache.put("GPSAccuracyTip", isChecked ? "1" : "0");
        } else if (buttonView.equals(mItemNewTip.getSwitchButton())) {
            mCache.put("newTip", isChecked ? "1" : "0");
        } else if (buttonView.equals(mitemPatrolReport.getSwitchButton())) {

            MyApplication.getInstance().putConfigValue("choseFromAlbum", isChecked ? 1 : 0);
            ACache mCache = BaseClassUtil.getConfigACache();
            mCache.put("choseFromAlbum", isChecked ? "1" : "0");
        } else if (buttonView.equals(mItemNetPing.getSwitchButton())) {
            saveNetPingConfig();
        } else if (buttonView.equals(mItemTileGrid.getSwitchButton())) {
            int val = mItemTileGrid.getSwitchButton().isChecked() ? 1 : 0;

            SettingUtil.saveConfig(SettingUtil.Config.SHOW_TILE_GRID, val);

            MyApplication.getInstance().putConfigValue(SettingUtil.Config.SHOW_TILE_GRID, val);
        }
    }

    private void saveNetPingConfig() {
        boolean checked = mItemNetPing.getSwitchButton().isChecked();
        SettingUtil.saveConfig(SettingUtil.Config.PING_NET_DELAY, checked);
        String action;
        if (checked) {
            // 开启检测
            action = NetPingReceiver.ACTION_START;
        } else {
            // 停止
            action = NetPingReceiver.ACTION_CANCEL;
        }
        Intent intent = new Intent(action);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onClick(View v) {

    }
}
