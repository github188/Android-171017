package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.systemsetting.customsetting.CustomSettingActivity;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 * 个性化设置
 */

public class CustomSettingItem extends BaseMoreSettingItem {

    public CustomSettingItem(Context context, View itemView) {
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
        Intent intent = new Intent(mContext, CustomSettingActivity.class);
        mContext.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation((Activity) mContext);
    }
}
