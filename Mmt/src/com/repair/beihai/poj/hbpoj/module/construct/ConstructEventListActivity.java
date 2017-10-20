package com.repair.beihai.poj.hbpoj.module.construct;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.UserBean;
import com.repair.beihai.poj.hbpoj.entity.ConstructEvent;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class ConstructEventListActivity extends SimplePagerListActivity {

    protected String constructType = "";
    EditText txtSearch ;
    String keyWord="";
    private ArrayList<ConstructEvent> eventItemList = new ArrayList<>();

    @Override
    public void init() {

        constructType = getIntent().getStringExtra("constructType");

        if (TextUtils.isEmpty(constructType)) {
            return;
        }

        mSimplePagerListDelegate = new SimplePagerListDelegate<ConstructEvent>(ConstructEventListActivity.this, eventItemList, ConstructEvent.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new ConstructEventAdapter(ConstructEventListActivity.this, eventItemList, constructType);
            }

            @Override
            protected String generateUrl() {
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/FetchBHConstructList")
                        .append("?flowName=").append("户表报装流程")
                        .append("&constructMan=").append(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName)
                        .append("&bizName=").append(constructType)
                        .append("&keyWord=").append(keyWord);

                return sb.toString();
            }

            @Override
            protected void initContentView() {
                super.initContentView();
                getmPullRefreshListView().setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                initActionBar();
             }

            @Override
            protected void initRootView() {
                setContentView(R.layout.listview_withsearch);
            }
        };

    }


    private void initActionBar() {


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        txtSearch = (EditText)findViewById(R.id.txtSearch);
        txtSearch.setHint("设计图编号、工程编号、工程名称、申报人姓名、地址");
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ConstructEventListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "设计图编号、工程编号、工程名称、申报人姓名、地址");
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_DoingBox");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            keyWord = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(keyWord);

            mSimplePagerListDelegate.getmPullRefreshListView().setRefreshing(false);

        }
    }


}
