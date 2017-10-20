package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.systemsetting.backgruoundinfo.BackgroundInfoActivity;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * 后台统计展示
 */

public class BackgroundInfoItem extends BaseMoreSettingItem {
    public BackgroundInfoItem(Context context, View itemView) {
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
       //跳转详情页
        try {
            Intent intent = new Intent(mContext, BackgroundInfoActivity.class);
            mContext.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation((Activity) mContext);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
