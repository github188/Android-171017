package com.repair.zhoushan.module.eventmanage.eventoverview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventItem;

/**
 * 两个入口：
 * 1."上报历史"界面. (底部需要有撤回的功能按钮)
 * 2."事件总览"界面.
 */
public class EventDetailActivity extends BaseActivity {

    private EventItem mEventItem;
    private EventDetailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("事件详情");

        Intent outerIntent = getIntent();
        mEventItem = outerIntent.getParcelableExtra("ListItemEntity");
        if (mEventItem == null) {
            showErrorMsg("未获取到参数");
            return;
        }

        createView();

        // "上报历史"界面跳转,底部需要有撤回的功能按钮
        if ("EventReportHistory".equals(outerIntent.getStringExtra("SourceFlag"))) {
            createBottomView();
        }
    }

    private void createView() {

        fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("ListItemEntity", mEventItem);
        fragment.setArguments(args);

        addFragment(fragment);

    }

    private void createBottomView() {

        BottomUnitView manageUnitView = new BottomUnitView(EventDetailActivity.this);
        manageUnitView.setContent("撤回");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String warningMsg = (mEventItem.IsRelatedCase == 1)
                        ? "该事件已发起处理流程，撤回该事件会删除与之相关的流程，是否仍然撤回 ?" : "是否撤回该条上报记录 ?";

                OkCancelDialogFragment fragment = new OkCancelDialogFragment(warningMsg);
                fragment.setLeftBottonText("撤回");
                fragment.setRightBottonText("取消");
                fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {
                        rollbackEvent();
                    }
                });
                fragment.show(EventDetailActivity.this.getSupportFragmentManager(), "");
            }
        });

    }

    private void rollbackEvent() {

        new AsyncTask<String, Void, String>() {

            private ProgressDialog loadingDialog;

            @Override
            protected void onPreExecute() {
                this.loadingDialog = MmtProgressDialog.getLoadingProgressDialog(EventDetailActivity.this, "正在处理,请稍候...");
                loadingDialog.show();
            }

            @Override
            protected String doInBackground(String... params) {

                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/EventManage/DeleteEventData";
                return NetUtil.executeHttpGet(uri, "eventCode", params[0], "eventMainTable", params[1], "isCreate", params[2],"userID",String.valueOf(MyApplication.getInstance().getUserId()));
            }

            @Override
            protected void onPostExecute(String resultStr) {

                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                if (Utils.json2ResultToast(EventDetailActivity.this, resultStr, "撤回事件失败")) {

                    Toast.makeText(EventDetailActivity.this, "事件撤回成功", Toast.LENGTH_SHORT).show();
                    backByReorder(true);
                }

            }
        }.executeOnExecutor(MyApplication.executorService, mEventItem.EventCode, mEventItem.EventMainTable, mEventItem.IsCreate);
    }

}
