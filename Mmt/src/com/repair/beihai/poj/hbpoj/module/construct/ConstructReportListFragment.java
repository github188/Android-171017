package com.repair.beihai.poj.hbpoj.module.construct;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.casemanage.casedetail.FeedBackListFragment;
import com.repair.zhoushan.module.tablecommonhand.TabltViewMode;

/**
 * Created by liuyunfan on 2016/7/22.
 */
public class ConstructReportListFragment extends FeedBackListFragment {

    @Override
    protected void addBottomBtn() {
        if (isRead) {
            return;
        }

        if (!addReportbtn) {
            return;
        }

        BottomUnitView fbUnitView = new BottomUnitView(baseActivity);

        fbUnitView.setContent("反馈");
        fbUnitView.setImageResource(R.drawable.handoverform_report);
        fbUnitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(tableName)) {
                    baseActivity.showErrorMsg("未获取到反馈表名，请稍候再试或检查配置");
                    return;
                }

                Intent intent = new Intent(baseActivity, ConstructReportActivity.class);

                intent.putExtra("caseno", caseNo);
                intent.putExtra("tableName", tableName);
                intent.putExtra("bizName", bizName);
                intent.putExtra("viewMode", TabltViewMode.REPORT.getTableViewMode());
                if (maintenanceFeedBacks != null && maintenanceFeedBacks.size() > 0) {
                    intent.putExtra("maintenanceFeedBacks", new Gson().toJson(maintenanceFeedBacks));
                }

                baseActivity.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(baseActivity);
            }
        });


        LinearLayout bottomContain = (LinearLayout) fullview.findViewById(R.id.fbbottombtn);
        bottomContain.setVisibility(View.VISIBLE);
        bottomContain.addView(fbUnitView);
    }

}
