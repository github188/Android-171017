package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;
import com.mapgis.mmt.module.systemsetting.task.ServerAppVersion;

/**
 * Created by Comclay on 2017/6/14.
 * 关于
 */

public class AppAboutSettingItem extends BaseMoreSettingItem {
    public AppAboutSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        new ServerAppVersion().execute(new ServerAppVersion.NewAppCallback() {
            @Override
            public void needUpdate() {
                mItemView.setRightMessage("NEW");
                TextView rightMessage = mItemView.getRightMessage();
                Typeface typeface = rightMessage.getTypeface();
                rightMessage.setTypeface(typeface, Typeface.ITALIC);
                rightMessage.setBackgroundResource(R.drawable.text_update);
                rightMessage.setTextColor(Color.WHITE);
                rightMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        try {
            Class<?> clz = ActivityClassRegistry.getInstance().getActivityClass("关于界面");
            mContext.startActivity(new Intent(mContext, clz));
            MyApplication.getInstance().startActivityAnimation((Activity) mContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
