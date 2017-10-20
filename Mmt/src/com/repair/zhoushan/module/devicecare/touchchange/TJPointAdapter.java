package com.repair.zhoushan.module.devicecare.touchchange;

import android.content.Intent;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.repair.zhoushan.entity.CrashPointRecord;
import com.repair.zhoushan.module.devicecare.patrolmonitor.TouchListAdapter;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/3/15.
 */
public class TJPointAdapter extends TouchListAdapter {

    public TJPointAdapter(BaseActivity mActivity, ArrayList<CrashPointRecord> eventItems) {
        super(mActivity, eventItems);
    }

    @Override
    public void onItemClick(int position) {
        super.onItemClick(position);

        Intent intent = new Intent(context, TableOneRecordActivity.class);
        intent.putExtra("tableName", "碰接点记录表");
        intent.putExtra("ID", dataList.get(position).ID);
        intent.putExtra("viewMode", TabltViewMode.DELETE.getTableViewMode());
        context.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(context);
    }
}