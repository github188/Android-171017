package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.content.Context;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.module.systemsetting.setting.BaseSwitchSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 */

public class RealtimeLocateSettingItem extends BaseSwitchSettingItem {


    public RealtimeLocateSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        if (MyApplication.getInstance().getConfigValue("isRealtimeLocate", 0) == 1) {
            mItemView.setSwitchChecked(true);
        }
    }

    @Override
    public void save() {
        SettingUtil.saveConfig(SettingUtil.Config.CONFIG_REALTIME_LOCATE
                , MyApplication.getInstance().getConfigValue("isRealtimeLocate", 0));
    }

    @Override
    public void performCheckedChanged(boolean isChecked) {
        MyApplication.getInstance().putConfigValue("isRealtimeLocate", isChecked ? "1" : "0");
        save();
    }
}
