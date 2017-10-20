package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;

import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.engine.MemoryManager;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 */

public class MediaSettingItem extends BaseMoreSettingItem {

    public MediaSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    /* 包含Media和Record两个文件，如果文件夹为空也要占用8192b的空间大小，避免用户误解*/
    @Override
    public void init() {
        long mediaCacheSize = Battle360Util.getMediaCacheSize();
        if (mediaCacheSize <= 8192) {
            mediaCacheSize = 0L;
        }
        mItemView.setRightMessage(Formatter.formatFileSize(mContext, mediaCacheSize));
    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        MemoryManager.newInstance(mContext).showConfirmDialog("是否确认删除？");
    }
}
