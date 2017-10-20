package com.maintainproduct.module.maintenance.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.BeanFragment.BeanFragmentOnCreate;
import com.maintainproduct.module.casehandover.CaseHandoverTask;
import com.maintainproduct.module.casehandover.CaseHandoverUserFragment;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.feedback.MaintenanceFormActivity;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceDetailActivity extends BaseActivity {
    /**
     * 获取工单详情
     */
    protected final int GET_DETAIL_FORM = 0;
    /**
     * 获取反馈表单信息
     */
    protected final int GET_FEEDBACK_FORM = 1;
    /**
     * 移交给默认承办人
     */
    protected final int TO_DEFAULT_NEXT = 2;

    protected BeanFragment formBeanFragment;

    protected MaintainSimpleInfo itemEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("工单详情");

        // 显示定位按钮
        getBaseRightImageView().setVisibility(View.VISIBLE);
        getBaseRightImageView().setImageResource(R.drawable.common_location);
        getBaseRightImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().show(formBeanFragment).commit();

                if (formBeanFragment == null) {
                    return;
                }

                String position = itemEntity.Position;

                if (BaseClassUtil.isNullOrEmptyString(position)) {
                    position = formBeanFragment.getPosition();
                }

                if (position == null || position.trim().length() == 0) {
                    showErrorMsg("工单详情中未包含<坐标>属性，无法进行工单定位");
                } else {
                    showOnMap(position);
                }
            }
        });

        itemEntity = getIntent().getParcelableExtra("ListItemEntity");

        if (getIntent().getBooleanExtra("isLocation", false)) {
            String position = itemEntity.Position;
            showOnMap(position);
        }

        handler.sendEmptyMessage(GET_DETAIL_FORM);
    }

    private void showOnMap(String position) {
        MyApplication.getInstance().sendToBaseMapHandle(
                new TaskLocationOnMapCallback(position, itemEntity.CaseName, itemEntity.CaseNo));
        Intent intent = new Intent(MaintenanceDetailActivity.this, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        if (arg1 == Activity.RESULT_OK && arg0 == MaintenanceConstant.DEFAULT_REQUEST_CODE) {
            setResult(Activity.RESULT_OK);
            AppManager.finishActivity(this);
        } else {
            formBeanFragment.onActivityResult(arg0, arg1, arg2);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * 获取详情配置表单信息
     */
    protected void getDetailForm(String id, String source) {
        new MaintenanceDetailTask(MaintenanceDetailActivity.this, handler).executeOnExecutor(MyApplication.executorService, id,
                source);
    }

    /**
     * 获取反馈配置表单信息
     */
    protected void getFeedbackForm() {
        new MaintenanceFlowStepTask(MaintenanceDetailActivity.this, handler).executeOnExecutor(MyApplication.executorService,
                itemEntity);
    }


    protected void createView(final GDFormBean formBean) {
        // 将Fragment显示在界面上
        formBeanFragment = new BeanFragment(formBean);
        formBeanFragment.setCls(MaintenanceDetailActivity.class);
        formBeanFragment.setFragmentFileRelativePath("Repair/" + itemEntity.CaseNo + "/");
        formBeanFragment.setAddEnable(false);
        formBeanFragment.setBeanFragmentOnCreate(new BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                if (formBean.hasGroupName("办理过程")) {
                    showFetchCaseProcedure("办理过程");
                }
            }
        });

        addFragment(formBeanFragment);

        createBottomView();
    }

    /**
     * 显示办理过程
     */
    protected void showFetchCaseProcedure(String produce) {
        List<String> groupNames = formBeanFragment.getGroupNames();

        if (groupNames != null && groupNames.contains(produce)) {
            int index = groupNames.indexOf(produce);

            FetchCaseProcedureFragment caseProcedureFragment = new FetchCaseProcedureFragment(itemEntity);
            formBeanFragment.replaceFragment(caseProcedureFragment, index);

            if (formBeanFragment.getShowFragmentIndex() != 0) {
                //getSupportFragmentManager().beginTransaction().hide(caseProcedureFragment).commitAllowingStateLoss();
            }
        }
    }

    protected Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {

                    case GET_DETAIL_FORM:
                        getDetailForm(itemEntity.ID + "", itemEntity.FlowName + "_工单详情");
                        break;

                    case MaintenanceConstant.SERVER_GET_DETAIL_SUCCESS:
                        // 将Fragment显示在界面上
                        GDFormBean bean = (GDFormBean) msg.obj;
                        createView(bean);
                        break;

                    case GET_FEEDBACK_FORM:
                        getFeedbackForm();
                        break;

                    case MaintenanceConstant.SERVER_SELECT_NEXT:
                        CaseHandoverUserFragment fragment = new CaseHandoverUserFragment(itemEntity);
                        fragment.show(getSupportFragmentManager(), "");
                        break;

                    case MaintenanceConstant.SERVER_DEFAULT_NEXT:
                        toDefaultHandover();
                        break;

                    case MaintenanceConstant.SERVER_GET_FEEDBACK:
                        parseFeedbackForm((ResultData<GDFormBean>) msg.obj);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 创建对工单进行操作的按钮
     */
    protected void createBottomView() {
        // BottomUnitView delayUnitView = new
        // BottomUnitView(MaintenanceDetailActivity.this);
        // delayUnitView.setContent("延期");
        // delayUnitView.setImageResource(R.drawable.handoverform_report);
        // addBottomUnitView(delayUnitView, new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // MaintenanceDelayFragment fragment = new
        // MaintenanceDelayFragment("延期原因");
        // fragment.show(getSupportFragmentManager(), "2");
        // }
        // });

        BottomUnitView backUnitView = new BottomUnitView(MaintenanceDetailActivity.this);
        backUnitView.setContent("退单");
        backUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(backUnitView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                MaintenanceBackFragment fragment = new MaintenanceBackFragment("回退原因");
                fragment.show(getSupportFragmentManager(), "1");
            }
        });

        BottomUnitView manageUnitView = new BottomUnitView(MaintenanceDetailActivity.this);
        manageUnitView.setContent("处理");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(manageUnitView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(GET_FEEDBACK_FORM);
            }
        });
    }

    /**
     * 跳转至反馈界面。当有多个表单信息GDFormBean时候，会根据分别读取GDFormBean的DESC属性，弹出选择框，
     * 根据选择框进入相应的反馈视图界面；只有一个时则不弹出选择界面，默认进入当前反馈视图界面
     */
    protected void parseFeedbackForm(final ResultData<GDFormBean> result) {
        // 跳转至工单反馈界面
        List<String> eventType = new ArrayList<String>();

        // 手动选择流程名称
        for (GDFormBean formBean : result.DataList) {
            eventType.add(formBean.Desc);
        }

        if (eventType.contains(null)) {
            showErrorMsg("请在配置表单配置Desc属性，用来描述事件类型");
            return;
        }

        if (eventType.size() == 1) {// 一个的情况
            toFeedbackView(result.DataList.get(0));
        } else {// 多个的情况
            ListDialogFragment fragment = new ListDialogFragment("选择类型", eventType);
            fragment.setListItemClickListener(new OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    toFeedbackView(result.DataList.get(arg2));
                }
            });
            fragment.show(getSupportFragmentManager(), "");
        }

    }
    /**
     * 跳转到反馈界面
     * @param formBean
     */
    protected void toFeedbackView(GDFormBean formBean) {
        Intent intent = new Intent(MaintenanceDetailActivity.this, MaintenanceFormActivity.class);
        intent.putExtra("ListItemEntity", itemEntity);
        intent.putExtra("GDFormBean", formBean);
        startActivityForResult(intent, MaintenanceConstant.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(MaintenanceDetailActivity.this);
    }

    protected void toDefaultHandover() {
        OkCancelDialogFragment fragment = new OkCancelDialogFragment("是否进行移交");
        fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                HandoverEntity handoverEntity = new HandoverEntity(itemEntity);
                handoverEntity.option = "移交案件";

                new CaseHandoverTask().createCaseHandoverData(handoverEntity);

                setResult(Activity.RESULT_OK);
                AppManager.finishActivity(MaintenanceDetailActivity.this);

            }
        });
        fragment.show(getSupportFragmentManager(), "");
    }
}
