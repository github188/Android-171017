package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

/**
 * Created by Comclay on 2017/6/14.
 * 大华视频输出比特率
 */

public class DHSettingItem extends BaseMoreSettingItem {

    private String[] mBitrates;
    private int mSelectIndex = -1;

    public DHSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        mBitrates = mItemView.getResources().getStringArray(R.array.video_bitrates);
        int dhBitRate = SettingUtil.getConfig(SettingUtil.Config.DAHUA_VIEDEO_OUT_RATE, 512);
        mItemView.setRightMessage(String.valueOf(dhBitRate));
    }

    @Override
    public void save() {
        SharedPreferences systemSharedPreferences = MyApplication.getInstance().getSystemSharedPreferences();
        SharedPreferences.Editor edit = systemSharedPreferences.edit();
        int bitrate = Integer.valueOf(mBitrates[mSelectIndex]);
        edit.putInt("DHBitRate", bitrate);
        edit.apply();
    }

    @Override
    public void performClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("视频传出码率");
        builder.setItems(mBitrates, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mSelectIndex = which;
                save();
                mItemView.setRightMessage(mBitrates[which]);
            }
        });
        builder.show();
    }
}
