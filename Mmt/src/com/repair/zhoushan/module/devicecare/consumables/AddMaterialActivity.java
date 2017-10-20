package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.login.UserBean;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoOrderBean;

import java.util.List;

public class AddMaterialActivity extends BaseActivity {

    public static final int RC_SEARCH_MATERIAL = 0x111;

    private AddMaterialFragment mAddMaterialFragment;

    private ScheduleTask mScheduleTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        getBaseTextView().setText("物料清单");

        createView();
        createBottomView();
    }

    private void createView() {

        // Search material.
        ImageButton searchImgBtn = getBaseRightImageView();
        searchImgBtn.setVisibility(View.VISIBLE);
        searchImgBtn.setImageResource(R.drawable.search_white);
        searchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAddMaterialFragment.getData().size() == 0) {
                    Toast.makeText(AddMaterialActivity.this, "物料列表为空!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(AddMaterialActivity.this, AddMaterialSearchActivity.class);
                intent.putParcelableArrayListExtra("MaterialList", mAddMaterialFragment.getData());
                mAddMaterialFragment.startActivityForResult(intent, RC_SEARCH_MATERIAL);
            }
        });

        // Main content view.
        this.mAddMaterialFragment = new AddMaterialFragment();
        Bundle args = new Bundle();
        args.putBoolean("IsExistCostCenter", getIntent().getBooleanExtra("IsExistCostCenter", false));
        mAddMaterialFragment.setArguments(args);
        addFragment(mAddMaterialFragment);
    }

    @Override
    public void onBackPressed() {
        if (mAddMaterialFragment.onPressBack()) {
            super.onBackPressed();
        }
    }

    private void createBottomView() {

//        LinearLayout bottomView = getBottomView();
//        bottomView.setBackgroundResource(0);
//        bottomView.setMinimumHeight(DimenTool.dip2px(AddMaterialActivity.this, 45));
//        CusBottomUnitView feedbackUnitView = new CusBottomUnitView(AddMaterialActivity.this);
//        feedbackUnitView.setContent("领 料");

        addBottomUnitView("领料", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<WuLiaoBean> orderedMaterialList = mAddMaterialFragment.getOrderedMaterialList();
                if (orderedMaterialList.size() == 0) {
                    Toast.makeText(AddMaterialActivity.this, "请先选取物料", Toast.LENGTH_SHORT).show();
                    return;
                }

                final StringBuilder sb = new StringBuilder();
                for (WuLiaoBean bean : orderedMaterialList) {
                    if (bean.isCheck() && bean.getNum() > 0) {
                        if (sb.length() != 0) {
                            sb.append("\n");
                        }
                        sb.append(bean.getNum()).append(" * {\'").append(bean.getCode())
                                .append("\', \'").append(bean.getName()).append("\'}");
                    }
                }
                new AlertDialog.Builder(AddMaterialActivity.this)
                        .setTitle("已选中的物料：")
                        .setMessage(sb.toString())
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                receiveMaterial(orderedMaterialList);
                            }
                        }).show();
            }
        });
    }

    private void receiveMaterial(final List<WuLiaoBean> orderedMaterialList) {

        MmtBaseTask<Void, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<Void, Void, ResultWithoutData>(AddMaterialActivity.this) {
            @Override
            protected ResultWithoutData doInBackground(Void... params) {

                ResultWithoutData data;

                WuLiaoOrderBean materialOrder = new WuLiaoOrderBean();

                // 填充构造数据
                materialOrder.WuLiaoList.addAll(orderedMaterialList);
                materialOrder.CaseNo = mScheduleTask.TaskCode;
                materialOrder.FlowCode = mScheduleTask.PreCodeFormat;
                materialOrder.OperType = "领料";
                // materialOrder.OperTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                materialOrder.OperTime = BaseClassUtil.getSystemTime();
                materialOrder.OperMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                String dataStr = new Gson().toJson(materialOrder, WuLiaoOrderBean.class);

                // 执行网络操作
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/PostWuLiaoOrder";

                try {

                    String result = NetUtil.executeHttpPost(url, dataStr,
                            "Content-Type", "application/json; charset=utf-8");

                    if (BaseClassUtil.isNullOrEmptyString(result)) {
                        throw new Exception("返回结果为空");
                    }

                    data = new Gson().fromJson(result, new TypeToken<ResultWithoutData>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    data = new ResultWithoutData();
                    data.ResultCode = -200;
                    data.ResultMessage = e.getMessage();
                }

                return data;
            }

            @Override
            protected void onSuccess(ResultWithoutData data) {
                if (data == null) {
                    Toast.makeText(AddMaterialActivity.this, "领料失败", Toast.LENGTH_SHORT).show();
                } else if (data.ResultCode != 200) {
                    String msg = TextUtils.isEmpty(data.ResultMessage) ? "领料失败" : data.ResultMessage;

                    TextView tv = new TextView(AddMaterialActivity.this);

                    tv.setText(msg);

                    OkDialogFragment fragment = new OkDialogFragment("错误信息", tv);

                    fragment.show(getSupportFragmentManager(), "");
                } else {
                    String msg = TextUtils.isEmpty(data.ResultMessage) ? "领料成功" : data.ResultMessage;

                    Toast.makeText(AddMaterialActivity.this, msg, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);

                    // 领料成功后清空选择的物料
                    mAddMaterialFragment.clearMaterialOrder();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }
}