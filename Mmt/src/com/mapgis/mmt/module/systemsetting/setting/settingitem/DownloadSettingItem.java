package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.download.DownloadActivity;
import com.mapgis.mmt.module.systemsetting.download.DownloadManager;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 * 下载管理
 */

public class DownloadSettingItem extends BaseMoreSettingItem {
    public DownloadSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        int downloadFileCount = DownloadManager.getInstance().getDownloadFileCount();
        TextView rightMessage = mItemView.getRightMessage();
        rightMessage.setVisibility(View.VISIBLE);
        if (downloadFileCount == 0) {
            rightMessage.setText("暂无更新");
            rightMessage.setBackgroundResource(android.R.color.transparent);
        } else {
            rightMessage.setText(String.valueOf(downloadFileCount));
            rightMessage.setBackgroundResource(R.drawable.shape_red_point);
        }
    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        Intent intent = new Intent(mContext, DownloadActivity.class);
        mContext.startActivity(intent);
        ((Activity)mContext).overridePendingTransition(R.anim.slide_in_right, 0);
    }
}
