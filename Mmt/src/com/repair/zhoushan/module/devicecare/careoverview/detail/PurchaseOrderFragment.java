package com.repair.zhoushan.module.devicecare.careoverview.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.BaseDetailFragment;
import com.repair.zhoushan.module.devicecare.consumables.entity.CaiGouOrderBean;

import java.util.List;

/**
 * 采购订单
 */
public class PurchaseOrderFragment extends BaseDetailFragment<CaiGouOrderBean> {

    private String caseNo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            caseNo = args.getString("CaseNo");
        }
    }

    @Override
    protected String getRequestUrl() {

        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchUsedCaiGouOrderList"
                + "?caseNo=" + caseNo;
    }

    @Override
    protected ResultData<CaiGouOrderBean> parseResultData(@NonNull String jsonResult) throws Exception {

        ResultData<CaiGouOrderBean> resultData = new Gson().fromJson(jsonResult, new TypeToken<ResultData<CaiGouOrderBean>>() {
        }.getType());

        // 特殊处理：当查询到记录为空的时候，状态码修改为失败情况下的状态码，以用现有的界面结构显示提示信息
        if (resultData.DataList == null || resultData.DataList.size() == 0) {
            resultData.ResultCode = -100;
            resultData.ResultMessage = "采购订单为空";
        }
        return resultData;
    }

    @Override
    protected void fillContentView(ResultData<CaiGouOrderBean> resultData) {

        ListView listView = new ListView(getActivity());
        listView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        PurchaseOrderAdapter listAdapter = new PurchaseOrderAdapter(getActivity(), resultData.DataList);
        listView.setDividerHeight(DimenTool.dip2px(getActivity(), 8));
        listView.setAdapter(listAdapter);

        addContentView(listView);
    }

    private class PurchaseOrderAdapter extends BaseAdapter {

        private List<CaiGouOrderBean> dataList;
        private LayoutInflater inflater;

        private PurchaseOrderAdapter(Context context, List<CaiGouOrderBean> dataList) {
            this.dataList = dataList;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.purchase_order_list_item, null);
            }

            CaiGouOrderBean purchaseOrder = dataList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");
            ((TextView) MmtViewHolder.get(convertView, R.id.supplier)).setText(purchaseOrder.GongYinShang.getName());
            ((TextView) MmtViewHolder.get(convertView, R.id.amount)).setText(purchaseOrder.Num + " 元");
            ((TextView) MmtViewHolder.get(convertView, R.id.costCenter))
                    .setText(!TextUtils.isEmpty(purchaseOrder.ChenBenCenter.getName())
                            ? purchaseOrder.ChenBenCenter.getName() : purchaseOrder.GongChang.getName());

            MmtViewHolder.get(convertView, R.id.btn_delete).setVisibility(View.GONE);

            return convertView;
        }
    }
}
