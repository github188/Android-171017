package com.maintainproduct.module.maintenance.feedback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.BeanFragment;
import com.maintainproduct.module.casehandover.CaseHandoverTask;
import com.maintainproduct.module.casehandover.CaseHandoverUserFragment;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.AdjustSoftKeyboard;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.taskcontrol.TaskControlDBHelper;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceFormActivity extends BaseActivity {
    protected GDFormBean data;

    protected BeanFragment formBeanFragment;

    protected MaintainSimpleInfo itemEntity;

    //是否加水印的标志，！=null且 size> 则加水印
    public ArrayList<String> waterTexts;
    //简单的限制一下是否上报了
    public boolean isReported = false;// 是否点击过上报按钮
    //是否从服务器或本地取已上报的数据
    protected boolean getcachedata = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AdjustSoftKeyboard.assistActivity(this);

        getBaseTextView().setText("工单反馈");

		/*---------------------------------------------------底部菜单生成-------------------------------------------------------------*/
        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                // if (!isReported)
//                saveGroupValue(formBeanFragment, ReportInBackEntity.UNREPORTED);

                AppManager.finishActivity(MaintenanceFormActivity.this);
                MyApplication.getInstance().finishActivityAnimation(
                        MaintenanceFormActivity.this);
            }
        });

        itemEntity = getIntent().getParcelableExtra("ListItemEntity");
        data = getIntent().getParcelableExtra("GDFormBean");

        if (MyApplication.getInstance().getConfigValue("fromcache", 1) == 0 || !getcachedata) {
            //不取缓存
            handler.sendEmptyMessage(MaintenanceConstant.SERVER_CREATE_FEEDBACK_VIEW);
        } else {
            // 获取缓存数据
            new MaintenanceFormValueTask(MaintenanceFormActivity.this, handler)
                    .executeOnExecutor(MyApplication.executorService, itemEntity,
                            data);
        }
        createBottom();

    }

    protected void createBottom() {
        BottomUnitView handoverUnitView = new BottomUnitView(this);
        handoverUnitView.setContent("保存");
        handoverUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(handoverUnitView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissErrorMsg();
                // 保存在本地等待反馈
                // isReported = true;
                saveGroupValue(formBeanFragment, ReportInBackEntity.REPORTING);
//                AppManager.finishActivity(MaintenanceFormActivity.this);
//                MyApplication.getInstance().finishActivityAnimation(
//                        MaintenanceFormActivity.this);
            }
        });

        BottomUnitView bothrUnitView = new BottomUnitView(this);
        bothrUnitView.setContent("移交");
        bothrUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(bothrUnitView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReported) {
                    dismissErrorMsg();
                    handover();
                } else {
                    Toast.makeText(MaintenanceFormActivity.this, "请先保存再移交", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
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
                case MaintenanceConstant.SERVER_CREATE_FEEDBACK_VIEW:
                    createFragment();
                    formBeanFragment.waterTexts = waterTexts;
                    break;
                case MaintenanceConstant.SERVER_BOTH_FEEDBACK_HANDOVER:
                    if (saveGroupValue(formBeanFragment,
                            ReportInBackEntity.REPORTING)) {
                        new CaseHandoverTask()
                                .createCaseHandoverData((HandoverEntity) msg.obj);
                    }
                    break;
                case MaintenanceConstant.SERVER_ONLY_HANDOVER:
                    startHandoverTask((HandoverEntity) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    protected void startHandoverTask(HandoverEntity entity) {
        new CaseHandoverTask().createCaseHandoverData(entity);
    }

    protected void createFragment() {
        formBeanFragment = new BeanFragment(data);
        formBeanFragment.setFragmentFileRelativePath("Repair/"
                + itemEntity.CaseNo + "/");
        formBeanFragment.setCls(MaintenanceFormActivity.class);
        addFragment(formBeanFragment);

    }

    /**
     * 保存表单数据
     *
     * @param formBeanFragment 表单对象
     * @param status           当前状态
     * @return 是否保存成功
     */
    public boolean saveGroupValue(BeanFragment formBeanFragment, int status) {
        if (formBeanFragment == null) {
            return false;
        }
        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/Feedback?caseNo="
                + itemEntity.CaseNo + "&flowName=" + itemEntity.FlowName
                + "&stepName=" + itemEntity.ActiveName + "&tableName="
                + data.TableName;

        List<FeedItem> items = formBeanFragment.getFeedbackItems(status);

        if (items == null) {
            return false;
        }

        items.addAll(getDefaultFeedItem(items, itemEntity));

        // 将对信息转换为JSON字符串
        String str = new Gson().toJson(items,
                new TypeToken<ArrayList<FeedItem>>() {
                }.getType());

        // 将所有信息封装成后台上传的数据模型
        ReportInBackEntity entity = new ReportInBackEntity(str, MyApplication
                .getInstance().getUserId(), status, uri, itemEntity.CaseNo
                + "_工单反馈", "工单反馈", formBeanFragment.getAbsolutePaths(),
                formBeanFragment.getRelativePaths());

        // 若本地数据存在，则修改保存数据，否则插入新数据
        int taskId = entity.getIdInSQLite();

        long row;

        if (taskId == -1) {
            row = entity.insert();

            TaskControlDBHelper.getIntance().createControlData(entity.getIdInSQLite() + "", "工单反馈");
        } else {
            row = entity.update();
        }

        if (row == -1) {
            isReported = false;
            Toast.makeText(MaintenanceFormActivity.this, "操作本地数据库失败", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            isReported = true;
            Toast.makeText(MaintenanceFormActivity.this, "反馈信息保存成功", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    /**
     * 必须上报的信息
     */
    private List<FeedItem> getDefaultFeedItem(List<FeedItem> formItems, MaintainSimpleInfo itemEntity) {
        List<FeedItem> mustItems = new ArrayList<>();

        FeedItem id0Item = new FeedItem();
        id0Item.Name = "ID0";
        id0Item.Value = itemEntity.ID0 + "";
        mustItems.add(id0Item);

        FeedItem userIdItem = new FeedItem();
        userIdItem.Name = "UserID";
        userIdItem.Value = MyApplication.getInstance().getUserId() + "";
        mustItems.add(userIdItem);

        FeedItem userNameItem = new FeedItem();
        userNameItem.Name = "UserName";
        userNameItem.Value = MyApplication.getInstance().getConfigValue(
                "UserBean", UserBean.class).TrueName;
        mustItems.add(userNameItem);

        FeedItem timeItem = new FeedItem();
        timeItem.Name = "FeedbackTime";
        timeItem.Value = BaseClassUtil.getSystemTime();
        mustItems.add(timeItem);

        FeedItem positionItem = new FeedItem();
        positionItem.Name = "Position";
        positionItem.Value = GpsReceiver.getInstance().getLastLocalLocation()
                .getX()
                + "," + GpsReceiver.getInstance().getLastLocalLocation().getY();
        mustItems.add(positionItem);

        // 如果配置的有默认字段，则过滤掉默认字段，采用填写的字段
        List<FeedItem> items = new ArrayList<>(mustItems);

        for (FeedItem formItem : formItems) {
            for (FeedItem mustItem : items) {
                if (formItem.Name.equalsIgnoreCase(mustItem.Name)) {
                    mustItems.remove(mustItem);
                }
            }
        }

        return mustItems;
    }

    /**
     * 移交工单
     */
    protected void handover() {

        new MmtBaseTask<Void, Void, String>(this) {
            @Override
            protected String doInBackground(Void... params) {
                //服务返回格式： 下一活动id／下一承办人id
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/CitySvr_Biz_CZPDATwo/REST/CZPDATwoRest.svc/GetNextUndertakeman";

                boolean isExist = NetUtil.testServiceExist(url);
                if (!isExist) {
                    return null;
                }
                try {
                    //服务返回格式：List<String> 下一活动id,下一承办人id
                    return NetUtil.executeHttpGet(url, "ID0", String.valueOf(itemEntity.ID0));
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void onSuccess(final String undertakemans) {
                super.onSuccess(undertakemans);
                if (TextUtils.isEmpty(undertakemans)) {
                    CaseHandoverUserFragment fragment = new CaseHandoverUserFragment(itemEntity);

                    fragment.show(getSupportFragmentManager(), "");
                    return;
                }

                List<String> undertakemanList = new ArrayList<String>();
                List<String> undertakemanArr = new Gson().fromJson(undertakemans, new TypeToken<List<String>>() {
                }.getType());
                for (String item : undertakemanArr) {
                    String[] temp = item.split(",");
                    undertakemanList.add(temp[0] + "/" + temp[1]);
                }
                final String undertakemanStr = TextUtils.join(",", undertakemanList);

                OkCancelDialogFragment fragment = new OkCancelDialogFragment(
                        "确认移交");
                fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        HandoverEntity handoverEntity = new HandoverEntity(
                                itemEntity);
                        handoverEntity.option = itemEntity.Opinion;
                        handoverEntity.undertakeman = undertakemanStr;

                        new CaseHandoverTask()
                                .createCaseHandoverData(handoverEntity);

                        setResult(Activity.RESULT_OK);
                        AppManager.finishActivity(MaintenanceFormActivity.this);
                    }
                });
                fragment.show(getSupportFragmentManager(), "");

            }
        }.mmtExecute();

    }
}
