package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.systemsetting.SystemSettingActivity;
import com.mapgis.mmt.module.systemsetting.SystemSettingFragment;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;
import com.zbar.lib.CaptureActivity;

/**
 * Created by Comclay on 2017/6/14.
 * 扫描
 */

public class SaoMiaoSettingView extends BaseMoreSettingItem {

    public SaoMiaoSettingView(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {

    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        SystemSettingFragment fragment = ((SystemSettingActivity) mContext).getSettingFragment();
        fragment.startActivityForResult(intent, 100);
        MyApplication.getInstance().startActivityAnimation((Activity) mContext);
    }
}
