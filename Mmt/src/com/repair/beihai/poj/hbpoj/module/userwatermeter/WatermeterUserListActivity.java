package com.repair.beihai.poj.hbpoj.module.userwatermeter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.filtermenubar.FilterMenuBar;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.repair.beihai.poj.hbpoj.entity.InstallWaterMeterModel;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class WatermeterUserListActivity extends SimplePagerListActivity {
    private final static String INSTALL_STATE = "安装状态"; // 排序方式
    // private final static String INSTALL_MAN = "安装人"; // 是否查看

    private EditText txtSearch;
    private String caseno = "";
    private String state = "";
    private String installMan = "";
    private ArrayList<InstallWaterMeterModel> eventItemList = new ArrayList<>();

    @Override
    public void init() {

        caseno = getIntent().getStringExtra("caseno");

        installMan=MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

        if (TextUtils.isEmpty(caseno)) {
            showErrorMsg("未找到工程编号");
            return;
        }

        mSimplePagerListDelegate = new SimplePagerListDelegate<InstallWaterMeterModel>(WatermeterUserListActivity.this, eventItemList, InstallWaterMeterModel.class) {
            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new WatermeterUserAdapter(WatermeterUserListActivity.this, eventItemList);
            }

            @Override
            protected String generateUrl() {

                String keyWord = "";
                if (txtSearch != null) {
                    keyWord = txtSearch.getText().toString();
                }
                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/ProjectManage/GetInstallWaterMeterWithPaging")
                        .append("?pageSize=").append(mSimplePagerListDelegate.getPageSize())
                        .append("&pageIndex=").append(mSimplePagerListDelegate.getLoadPageIndex())
                        .append("&sortFields=ID")
                        .append("&direction=desc")
                        .append("&isOnFlow=未发起")
                        .append("&caseno=" + caseno)
                        .append("&state=" + state)
                        .append("&installMan=" + installMan)
                        .append("&keyWord=" + keyWord);

                return sb.toString();
            }

            @Override
            protected void initRootView() {
                setContentView(R.layout.bh_watermeteruserbox_list_activity);
            }
        };

    }

    private void initToolBar() {
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnOtherAction).setVisibility(View.GONE);

        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setHint("档案编号、用户名称");
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(WatermeterUserListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", "档案编号、用户名称");
                intent.putExtra("searchHistoryKey", "CaseListSearchHistory_DoingBox");

                WatermeterUserListActivity.this.startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });
    }

    private void initConditionBar() {

        final FilterMenuBar mFilterMenuBar = (FilterMenuBar) findViewById(R.id.mMenuBar);
        mFilterMenuBar.setMenuItems(new String[]{INSTALL_STATE},
                new String[][]{{"全部", "已安装", "未安装"}},
                new String[]{"全部"});


        // 过滤条件
        mFilterMenuBar.setOnMenuItemSelectedListener(new FilterMenuBar.OnMenuItemSelectedListener() {
            @Override
            public void onItemSelected(Map<String, String> selectResult) {

                state = selectResult.get(INSTALL_STATE);

                if ("全部".equals(state)) {
                    state = "";
                }

                //后来改为安装人就是自己
//                installMan = selectResult.get(INSTALL_MAN);
//                if ("全部".equals(installMan)) {
//                    installMan = "";
//                }

                updateData();
            }
        });


    }

    @Override
    protected void afterViewCreated() {
        super.afterViewCreated();

        // 初始化toolbar
        initToolBar();

        // 初始化条件选择
        initConditionBar();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);

            updateData();

        }
    }
}
