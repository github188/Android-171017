package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Formatter;
import android.view.View;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.TrafficUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.TrafficInfo;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Comclay on 2017/6/14.
 * 本月流量显示
 */

public class TrafficSettingItem extends BaseMoreSettingItem {


    public TrafficSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {
        // 显示流量信息的TextView
        SharedPreferences sp = mContext.getSharedPreferences("historyTraffic", Context.MODE_PRIVATE);
        // 上个月结束时所记录的流量使用量,如果本月手机关过机就清除该数据
        long lastMonth = sp.getLong("lastMonth", 0);
        // 本月每次关机所累积的流量数据
        long traffic = sp.getLong("traffic", 0);
        // 上次开机到现在所使用的流量数据
        long currTraffic = TrafficUtil.getTraffic(mContext, mContext.getPackageName());
        long totalTraffic = traffic + currTraffic - lastMonth;  // 总的使用量
        String trafficMsg = Formatter.formatFileSize(mContext, totalTraffic);
        mItemView.setMessage(String.format(Locale.CHINA, mContext.getString(R.string.text_traffic_used), trafficMsg));
        mItemView.setRightMessage(trafficMsg);

        // 存储截至目前本月已经使用的流量到数据库中
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        final String date = year + "-" + month;
        ArrayList<TrafficInfo> list = DatabaseHelper.getInstance().query(TrafficInfo.class, "date='" + date + "'");
        if (list.size() == 0) {
            // 数据库没有本月的流量使用信息
            // 则插入数据
            DatabaseHelper.getInstance().insert(new TrafficInfo(date, Formatter.formatFileSize(mContext, totalTraffic)));
        } else {
            // 数据库中已经存有本月的流量使用信息
            // 则更新数据
            ContentValues cv = new ContentValues();
            cv.put("totalTraffic", Formatter.formatFileSize(mContext, totalTraffic));
            DatabaseHelper.getInstance().update(TrafficInfo.class, cv, "date='" + date + "'");
        }
    }

    @Override
    public void save() {
        // do nothing...
    }

    @Override
    public void performClicked() {
        // do nothing...
    }
}
