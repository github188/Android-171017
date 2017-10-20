package com.repair.shaoxin.water.valveinstruction;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ValveInstructionListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private ValveInstructionAdapter adapter;
    private Button btnHandover;

    protected CaseItem mCaseItem;
    private final ArrayList<ValveModel> valveModelList = new ArrayList<>();

    @Override
    protected void setDefaultContentView() {

        this.mCaseItem = getIntent().getParcelableExtra("ListItemEntity");
        setSwipeBackEnable(false);
        setContentView(R.layout.activity_simple_list);

        initActionBar();
        this.btnHandover = (Button) findViewById(R.id.btn_handover);
        final String nodeName = getIntent().getStringExtra("CurrentNodeName");
        btnHandover.setText(nodeName);
        btnHandover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handover();
            }
        });
        this.mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        this.adapter = new ValveInstructionAdapter(ValveInstructionListActivity.this, valveModelList);
        LinearLayoutManager llm = new LinearLayoutManager(ValveInstructionListActivity.this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(adapter);

        fetchRelatedValveIntroduction(mCaseItem.EventCode);
    }

    private void handover() {
        Intent intent = new Intent(ValveInstructionListActivity.this, CaseHandoverActivity.class);
        intent.putExtra("ListItemEntity", mCaseItem);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(ValveInstructionListActivity.this);
    }

    private void fetchRelatedValveIntroduction(final String eventCode) {
        MmtBaseTask<Void, Void, Results<ValveModel>> mmtBaseTask
                = new MmtBaseTask<Void, Void, Results<ValveModel>>(ValveInstructionListActivity.this) {

            @Override
            protected Results<ValveModel> doInBackground(Void... params) {

                Results<ValveModel> result;
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/CitySvr_Biz_SXGS/REST/BizSXGSRest.svc/GetValveOrderInfo"
                        + "?eventCode=" + eventCode;
                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("网络错误");
                    }
                    result = new Gson().fromJson(jsonResult, new TypeToken<Results<ValveModel>>() {
                    }.getType());

                } catch (Exception e) {
                    e.printStackTrace();
                    result = new Results<>("-100", e.getMessage());
                }
                return result;
            }

            @Override
            protected void onSuccess(Results<ValveModel> results) {
                ResultData<ValveModel> resultData = results.toResultData();
                if (resultData.ResultCode != 200) {
                    Toast.makeText(ValveInstructionListActivity.this, resultData.ResultMessage,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                valveModelList.clear();
                valveModelList.addAll(resultData.DataList);
                Collections.sort(valveModelList, operateOrderComparator);
                adapter.notifyDataSetChanged();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void initActionBar() {

        getBaseTextView().setText("阀门指令");
        addBackBtnListener(getBaseLeftImageView());

        ImageView btnLoc = getBaseRightImageView();
        btnLoc.setVisibility(View.VISIBLE);
        btnLoc.setImageResource(R.drawable.navigation_locate);
        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ArrayList<ValveModel> taskList = adapter.getDataList();
                MyApplication.getInstance().sendToBaseMapHandle(
                        new ShowAllValveCallback(ValveInstructionListActivity.this, taskList));
            }
        });
    }

    private final Comparator<ValveModel> operateOrderComparator = new Comparator<ValveModel>() {
        @Override
        public int compare(ValveModel lhs, ValveModel rhs) {
            return lhs.operateOrder - rhs.operateOrder;
        }
    };

}
