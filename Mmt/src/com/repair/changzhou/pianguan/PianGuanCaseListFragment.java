package com.repair.changzhou.pianguan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.casemanage.mycase.CaseListFragment;

import java.util.UUID;

public class PianGuanCaseListFragment extends CaseListFragment {

    public static final String TAG = "PianGuanCaseListFragment";
    static final int RC_NEED_REFRESH_DATA = 111;

    private String userName;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userName = MyApplication.getInstance().getUserBean().TrueName;
    }

    public static PianGuanCaseListFragment newInstance() {
        PianGuanCaseListFragment fragment = new PianGuanCaseListFragment();
        return fragment;
    }

    @Override
    protected String generateRequestUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                .append("/Services/Zondy_MapGISCitySvr_CZ/REST/CaseManageREST.svc/CaseManage/GetCaseOverviewBoxWithPagingForCZ")
                .append("?_mid=").append(UUID.randomUUID().toString())
                .append("&userName=").append(Uri.encode(userName))
                .append("&pageIndex=").append(String.valueOf(getLoadingPageIndex()))
                .append("&pageSize=").append(String.valueOf(getPageSize()))
                .append("&sortFields=ID0&direction=desc");

        String searchKey = getSearchKey();
        if (!TextUtils.isEmpty(searchKey)) {
            sb.append("&eventInfo=").append(Uri.encode(searchKey));
        }

        String flowName = getFilterFlowName();
        if (!TextUtils.isEmpty(flowName)) {
            sb.append("&flowName=").append(Uri.encode(flowName));
        }

        String isReadStr = isRead();
        if (!TextUtils.isEmpty(isReadStr)) {
            sb.append("&isReaded=").append(isReadStr);
        }

        return sb.toString();
    }

    @Override
    public void onItemClicked(CaseItem caseItem) {
        Intent intent = new Intent(getActivity(), PianGuanCaseDetailActivity.class);
        intent.putExtra("ListItemEntity", caseItem);
        startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(getActivity());
    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == RC_NEED_REFRESH_DATA) {
//
//            removeLastClickedItem();
//            // requestRefreshData();
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
}
