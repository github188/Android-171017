package com.maintainproduct.v2.caselist;

import android.R.drawable;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.maintenance.detail.MaintenanceDetailActivity;
import com.maintainproduct.v2.callback.GDOneLocationOnMapCallback;
import com.maintainproduct.v2.task.AcceptGDTask;
import com.maintainproduct.v2.task.GDCaseInfo;
import com.maintainproduct.v2.task.MaintainGDDetailTask;
import com.maintainproduct.v2.task.ReadGDTask;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.patrol.common.ShowGISDetailTask;
import com.patrol.entity.KeyPoint;

import java.util.UUID;

/**
 * 桂林 维修 工单 的 工单详情
 *
 * @author meikai
 */
public class MaintainGDDetailActivity extends BaseActivity {
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
    protected String GDState;
    protected String ReadGDTime;
    protected String CaseID;
    protected Boolean isMapToMe = false;
    protected String OneLocTitle;
    protected String OneLocDescription;

    private Intent resultIntent;

    private GDItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("工单详情");

        resultIntent = new Intent();

        OneLocTitle = getIntent().getStringExtra("OneLocTitle");
        OneLocDescription = getIntent().getStringExtra("OneLocDescription");
        isMapToMe = getIntent().getBooleanExtra("isMapToMe", false);

        if (isMapToMe) {
            // 如果 是 从地图 上的 标签 跳转 到 此 详情界面 ，则不显示 右上角 的 定位图标
            // 如果 是 从 工单列表 的 单击事件 跳转到 此详情界面，则 显示 右上角的 定位图标
        } else {
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

                    if (BaseClassUtil.isNullOrEmptyString(formBeanFragment.getPosition())) {
                        showErrorMsg("该工单不包含坐标信息");
                    } else {
                        MyApplication.getInstance().sendToBaseMapHandle(
                                new GDOneLocationOnMapCallback(formBeanFragment.getPosition(), OneLocTitle, OneLocDescription));
                        Intent intent = new Intent(MaintainGDDetailActivity.this, MapGISFrame.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                }
            });
        }

        itemEntity = getIntent().getParcelableExtra("ListItemEntity");
        GDState = getIntent().getStringExtra("GDState");
        CaseID = getIntent().getStringExtra("CaseID");
        ReadGDTime = getIntent().getStringExtra("ReadGDTime");

        handler.sendEmptyMessage(GET_DETAIL_FORM);

        // 如果 此条 工单 未阅读 ， 则 修改其 阅读时间
        if (ReadGDTime == null || ReadGDTime.length() == 0) {
            ReportItem reportItem = new ReportItem();
            reportItem.CaseID = CaseID;
            reportItem.ReportManID = MyApplication.getInstance().getUserId() + "";
            new ReadGDTask(MaintainGDDetailActivity.this, handler).executeOnExecutor(MyApplication.executorService, reportItem);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == MaintainConstant.DEFAULT_REQUEST_CODE) {
            setResult(Activity.RESULT_OK);
            AppManager.finishActivity(this);
        } else {
            formBeanFragment.onActivityResult(requestCode, resultCode, data);
        }

        switch (requestCode) {
            case MaintainConstant.DaoChang:
                if (resultCode == MaintainConstant.GDOpeSuccess) {
                    resultIntent.putExtra("CaseNo", itemEntity.CaseNo);
                    setResult(MaintainConstant.DaoChang, resultIntent);
                    resetBottomView(MaintainConstant.DaoChang);
                    // Intent i = new Intent();
                    // i.putExtra("CaseNo", itemEntity.CaseNo);
                    // setResult(MaintainConstant.DaoChang, i);
                    // AppManager.finishActivity(this);
                }
                break;
            case MaintainConstant.WanCheng:
                if (resultCode == MaintainConstant.GDOpeSuccess) {
                    resultIntent.putExtra("CaseNo", itemEntity.CaseNo);
                    setResult(MaintainConstant.WanCheng, resultIntent);
                    // Intent i = new Intent();
                    // i.putExtra("CaseNo", itemEntity.CaseNo);
                    // setResult(MaintainConstant.WanCheng, i);
                    AppManager.finishActivity(this);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 桂林供水 接单请求
                case MaintainConstant.SERVER_RECEIVE_GD:
                    ReportItem reportItem = new ReportItem();
                    reportItem.CaseID = CaseID;
                    reportItem.ReportManID = MyApplication.getInstance().getUserId() + "";
                    new AcceptGDTask(MaintainGDDetailActivity.this, handler).executeOnExecutor(MyApplication.executorService, reportItem);
                    break;
                // 桂林供水 接单请求 服务访问 成功
                case MaintainConstant.SERVER_RECEIVE_GD_DONE:
                    ResultData<String> result = new Gson().fromJson(msg.obj.toString(), new TypeToken<ResultData<String>>() {
                    }.getType());
                    showToast(result.ResultMessage);
                    if (result.ResultCode > 0) {
                        // Intent i = new Intent();
                        // i.putExtra("CaseNo", itemEntity.CaseNo); // 此 工单编号 指定的工单
                        // 被接受
                        // MaintainGDDetailActivity.this.setResult(
                        // MaintainConstant.SERVER_RECEIVE_GD_DONE , i );
                        // MaintainGDDetailActivity.this.finish();
                        resultIntent.putExtra("CaseNo", itemEntity.CaseNo);
                        setResult(MaintainConstant.SERVER_RECEIVE_GD_DONE, resultIntent);
                        resetBottomView(MaintainConstant.SERVER_RECEIVE_GD_DONE); // 更新底部
                        // 操作
                        // 栏
                    }
                    break;
                case MaintainConstant.SERVER_READ_GD_DONE: // 阅单成功
                    ResultData<String> result2 = new Gson().fromJson(msg.obj.toString(), new TypeToken<ResultData<String>>() {
                    }.getType());
                    showToast(result2.ResultMessage);
                    resultIntent.putExtra("CaseNo", itemEntity.CaseNo);
                    setResult(MaintainConstant.SERVER_READ_GD_DONE, resultIntent);
                    break;
                case GET_DETAIL_FORM:
                    new MaintainGDDetailTask(MaintainGDDetailActivity.this, handler).executeOnExecutor(MyApplication.executorService, CaseID);
                    break;
                case MaintainConstant.SERVER_GET_DETAIL_SUCCESS:
                    // 将Fragment显示在界面上
                    GDCaseInfo info = (GDCaseInfo) msg.obj;

                    item = info.Item;

                    final GDFormBean bean = info.Bean;
                    formBeanFragment = new BeanFragment(bean);
                    formBeanFragment.setCls(MaintenanceDetailActivity.class);
                    formBeanFragment.setFragmentFileRelativePath("工单详情下载/");
                    formBeanFragment.setAddEnable(false);
                    addFragment(formBeanFragment);
                    createBottomView();
                    break;
            }
        }
    };

    /**
     * 创建对工单进行操作的按钮
     */
    private void createBottomView() {
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

        if (GDState.equals("未接受") || GDState.equals("已阅读")) {
            BottomUnitView backUnitView = new BottomUnitView(MaintainGDDetailActivity.this);
            backUnitView.setContent("接单");
            backUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(backUnitView, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.sendEmptyMessage(MaintainConstant.SERVER_RECEIVE_GD);
                }
            });
        }
        if (GDState.equals("待处理")) {
            BottomUnitView backUnitView = new BottomUnitView(MaintainGDDetailActivity.this);
            backUnitView.setContent("到场");
            backUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(backUnitView, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String feedBackUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/ReportArrive";
                    Intent i = new Intent(MaintainGDDetailActivity.this, BaseFeedBackActivity.class);
                    i.putExtra("CaseID", CaseID);
                    i.putExtra("feedBackUrl", feedBackUrl);
                    i.putExtra("feedBackType", "到场反馈");
                    startActivityForResult(i, MaintainConstant.DaoChang);
                    // MyApplication.getInstance().startActivityAnimation(
                    // MaintainGDDetailActivity.this );
                }
            });
        }

        if (GDState.equals("处理中")) {
            BottomUnitView backUnitView = new BottomUnitView(MaintainGDDetailActivity.this);
            backUnitView.setContent("处理");
            backUnitView.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(backUnitView, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String feedBackUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/ReportProcess";
                    Intent i = new Intent(MaintainGDDetailActivity.this, BaseFeedBackActivity.class);
                    i.putExtra("CaseID", CaseID);
                    i.putExtra("feedBackUrl", feedBackUrl);
                    i.putExtra("feedBackType", "处理反馈");
                    MaintainGDDetailActivity.this.startActivity(i);
                    // MyApplication.getInstance().startActivityAnimation(
                    // MaintainGDDetailActivity.this );
                }
            });

            BottomUnitView backUnitView2 = new BottomUnitView(MaintainGDDetailActivity.this);
            backUnitView2.setContent("完成");
            backUnitView2.setImageResource(R.drawable.handoverform_report);
            addBottomUnitView(backUnitView2, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String feedBackUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/CaseHandOver?ID0=" + itemEntity.ID0;
                    Intent i = new Intent(MaintainGDDetailActivity.this, BaseFeedBackActivity.class);
                    i.putExtra("CaseID", CaseID);
                    i.putExtra("feedBackUrl", feedBackUrl);
                    i.putExtra("feedBackType", "完成反馈");
                    // MaintainGDDetailActivity.this.startActivity(i);
                    startActivityForResult(i, MaintainConstant.WanCheng);
                    // MyApplication.getInstance().startActivityAnimation(
                    // MaintainGDDetailActivity.this );
                }
            });
        }

        BottomUnitView manageUnitView = new BottomUnitView(MaintainGDDetailActivity.this);
        manageUnitView.setContent(" 退单");
        manageUnitView.setImageResource(R.drawable.handbackform_report);
        addBottomUnitView(manageUnitView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edtV = new EditText(MaintainGDDetailActivity.this);
                edtV.setBackgroundResource(drawable.editbox_background_normal);
                edtV.setHint("请输入退单原因");
                edtV.setLines(3);
                edtV.setId(edtV.hashCode());
                OkCancelDialogFragment fragment = new OkCancelDialogFragment("确认退单？", edtV);

                fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        if (edtV.getText().toString().length() == 0) {
                            MyApplication.getInstance().showMessageWithHandle("退单原因必须填写");
                            return;
                        }

                        String feedBackUrl = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/CaseHandBack?CaseID=" + CaseID + "&ID0="
                                + itemEntity.ID0 + "&option=" + edtV.getText().toString();
                        ReportItem reportItem = new ReportItem();
                        reportItem.CaseID = CaseID;
                        reportItem.ReportMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;
                        reportItem.ReportManID = MyApplication.getInstance().getUserId() + "";
                        String data = new Gson().toJson(reportItem, ReportItem.class);

                        ReportInBackEntity backEntity = new ReportInBackEntity(data, MyApplication.getInstance().getUserId(),
                                ReportInBackEntity.REPORTING, feedBackUrl, UUID.randomUUID().toString(), "工单退单", "", "");

                        long count = backEntity.insert();
                        if (count > 0) {
                            Toast.makeText(MaintainGDDetailActivity.this, "工单退单保存成功", Toast.LENGTH_SHORT).show();
                            // Intent i = new Intent();
                            // i.putExtra("CaseNo", itemEntity.CaseNo);
                            // setResult(MaintainConstant.TuiDan, i);
                            resultIntent.putExtra("CaseNo", itemEntity.CaseNo);
                            setResult(MaintainConstant.TuiDan, resultIntent);
                            MaintainGDDetailActivity.this.finish();
                        }
                    }
                });
                fragment.show(getSupportFragmentManager(), "");
            }
        });

        if (!TextUtils.isEmpty(item.LayerName) && !TextUtils.isEmpty(item.FieldValue)) {
            BottomUnitView btnGIS = new BottomUnitView(MaintainGDDetailActivity.this);

            btnGIS.setContent("属性");
            btnGIS.setImageResource(R.drawable.handbackform_report);

            addBottomUnitView(btnGIS, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        KeyPoint kp = new KeyPoint();

                        kp.GisLayer = item.LayerName;
                        kp.FieldName = item.FieldName;
                        kp.FieldValue = item.FieldValue;

                        new ShowGISDetailTask(MaintainGDDetailActivity.this).mmtExecute(kp);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void resetBottomView(int resetType) {
        switch (resetType) {
            case MaintainConstant.SERVER_RECEIVE_GD_DONE:
                GDState = "待处理";
                ((LinearLayout) findViewById(R.id.baseBottomView)).removeAllViews();
                createBottomView();
                break;
            case MaintainConstant.DaoChang:
                GDState = "处理中";
                ((LinearLayout) findViewById(R.id.baseBottomView)).removeAllViews();
                createBottomView();
                break;
            default:
                break;
        }

    }

}
