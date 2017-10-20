package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 * 清除缓存
 */

public class AppCacheSettingItem extends BaseMoreSettingItem {

    public AppCacheSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    /* 包含Tile缓存文件，如果文件夹为空也要占用4096b的空间大小，避免用户误解*/
    @Override
    public void init() {
        long tileCacheSize = Battle360Util.getTileCacheSize();
        mItemView.setRightMessage(Formatter.formatFileSize(mContext, tileCacheSize));
    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        MyApplication.getInstance().clearCache();
        init();
    }
}
