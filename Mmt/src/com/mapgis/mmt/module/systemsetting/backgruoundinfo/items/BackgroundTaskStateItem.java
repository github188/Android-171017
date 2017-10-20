package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Administrator on 2017/8/22 0022.
 */

public class BackgroundTaskStateItem extends BaseMoreSettingItem {
    public BackgroundTaskStateItem(Context context, View itemView) {
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
        try {
            Intent intent = new Intent(mContext, BackgroundTaskStateActivity.class);
            mContext.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation((Activity) mContext);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
