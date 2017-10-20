package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.SettingsInputDialog;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 * 大华视频输出比特率
 */

public class RealtimeValueSettingItem extends BaseMoreSettingItem {

    public RealtimeValueSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        String msg = MyApplication.getInstance().getConfigValue("realtimeLocateInterval", 5) + "秒";
        mItemView.setRightMessage(msg);
    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        SettingsInputDialog fragment = new SettingsInputDialog("定时定位", "realtimeLocateInterval");
        fragment.show(((FragmentActivity)mContext).getSupportFragmentManager(), "1");
        fragment.setOnOkClickListener(new SettingsInputDialog.OnOkClickListener() {
            @Override
            public void onOkClick(View view, String text) {
                String value = MyApplication.getInstance().getConfigValue("realtimeLocateInterval", 5) + "秒";
                mItemView.setMessage("定时定位的时间间隔：" + value);
                mItemView.setRightMessage(value);
            }
        });
    }
}
