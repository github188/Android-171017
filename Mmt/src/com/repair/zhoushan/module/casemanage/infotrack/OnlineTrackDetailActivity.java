package com.repair.zhoushan.module.casemanage.infotrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.CusBottomUnitView;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.FlowBeanFragment;

public class OnlineTrackDetailActivity extends BaseActivity {

    private GDFormBean gdFormBean;
    private FlowBeanFragment mFlowBeanFragment;

    private HotlineModel mHotlineModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mHotlineModel = getIntent().getParcelableExtra("ListItemEntity");

        createView();
        createBottomView();
    }

    private void createView() {

        gdFormBean = GDFormBean.generateSimpleForm(
                new String[]{"DisplayName", "事件编号", "Name", "事件编号", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.EventCode},
                new String[]{"DisplayName", "反映人", "Name", "反映人", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.ReporterName},
                new String[]{"DisplayName", "来电号码", "Name", "来电号码", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.TelNum},
                new String[]{"DisplayName", "反映时间", "Name", "反映时间", "Type", "日期框", "IsRead", "true", "Value", mHotlineModel.CallTime},
                new String[]{"DisplayName", "发生地址", "Name", "发生地址", "Type", "百度地址", "IsRead", "true", "Value", mHotlineModel.Address},
                new String[]{"DisplayName", "反映内容", "Name", "反映内容", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.Content},
                new String[]{"DisplayName", "勘察人", "Name", "勘察人", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.Surveyor},
                new String[]{"DisplayName", "勘察时间", "Name", "勘察时间", "Type", "日期框", "IsRead", "true", "Value", mHotlineModel.SurveyTime},
                new String[]{"DisplayName", "现场勘察情况", "Name", "现场勘察情况", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.SurveyRemark},
                new String[]{"DisplayName", "解决措施", "Name", "解决措施", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.Solution},
                new String[]{"DisplayName", "处理结果", "Name", "处理结果", "Type", "短文本", "IsRead", "true", "Value", mHotlineModel.DealResult},
                new String[]{"DisplayName", "处理备注", "Name", "处理备注", "Type", "选择器", "IsRead", "true", "Value", mHotlineModel.DealRemark});

        this.mFlowBeanFragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBean);
        mFlowBeanFragment.setArguments(args);

        addFragment(mFlowBeanFragment);
    }

    private void createBottomView() {

        if (mHotlineModel.FollowState.equals("完成")) {
            return;
        }

        LinearLayout bottomView = getBottomView();
        bottomView.setBackgroundResource(0);
        bottomView.setMinimumHeight(DimenTool.dip2px(OnlineTrackDetailActivity.this, 45));

        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(OnlineTrackDetailActivity.this);
        feedbackUnitView.setContent("信息跟踪");

        addBottomUnitView(feedbackUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                        new String[]{"DisplayName", "跟踪登录员", "Name", "跟踪登录员", "Type", "本人姓名"},
                        new String[]{"DisplayName", "跟踪时间", "Name", "跟踪时间", "Type", "日期框"},
                        new String[]{"DisplayName", "跟踪结果", "Name", "跟踪结果", "Type", "值选择器", "ConfigInfo", "完成,客不,主不"},
                        new String[]{"DisplayName", "跟踪备注", "Name", "跟踪备注", "Type", "短文本", "DisplayColSpan", "100"});

                Intent intent = new Intent(OnlineTrackDetailActivity.this, InfoTrackDialogActivity.class);
                intent.putExtra("Tag", "信息跟踪");
                intent.putExtra("Title", "信息跟踪");
                intent.putExtra("GDFormBean", gdFormBean);
                intent.putExtra("ListItemEntity", mHotlineModel);

                startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });
    }

}
