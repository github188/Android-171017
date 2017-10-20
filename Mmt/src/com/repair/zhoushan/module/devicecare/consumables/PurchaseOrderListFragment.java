package com.repair.zhoushan.module.devicecare.consumables;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.CaiGouOrderBean;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderListFragment extends Fragment {

    protected PullToRefreshListView mPullRefreshListView;
    private PurchaseOrderListAdapter listAdapter;

    private List<CaiGouOrderBean> purchaseOrderList = new ArrayList<CaiGouOrderBean>();

    private ScheduleTask mScheduleTask;

    private boolean allowDelete;

    // 是否存在成本中心
    private boolean isExistCostCenter; // default 'false'

    public boolean isExistCostCenter() {
        return isExistCostCenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScheduleTask = getArguments().getParcelable("ListItemEntity");
        allowDelete = getArguments().getBoolean("AllowDelete", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pulltorefresh_both, container, false);

        this.mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mainFormList);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        ListView mListView = mPullRefreshListView.getRefreshableView();

        this.listAdapter = new PurchaseOrderListAdapter(getActivity());
        mListView.setAdapter(listAdapter);

        getPurchaseListData();

        return view;
    }

    protected void getPurchaseListData() {

        MmtBaseTask<String, Void, ResultData<CaiGouOrderBean>> fetchOrderTask = new MmtBaseTask<String, Void, ResultData<CaiGouOrderBean>>(getActivity()) {
            @Override
            protected ResultData<CaiGouOrderBean> doInBackground(String... params) {

                // 检查是否存在成本中心(未取到暂未给出提示)
                String urlExistCBCenter = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/ExistCBCenter"
                        + "?bizCode=" + params[1] + "&objType=采购订单";

                String resultCC = NetUtil.executeHttpGet(urlExistCBCenter);

                if (!TextUtils.isEmpty(resultCC)) {
                    ResultData<String> resultData = new Gson().fromJson(resultCC, new TypeToken<ResultData<String>>() {
                    }.getType());
                    if (resultData.DataList.size() > 0) {
                        if ("1".equals(resultData.getSingleData())) {
                            isExistCostCenter = true;
                        }
                    }
                }

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchUsedCaiGouOrderList";

                String result = NetUtil.executeHttpGet(url, "caseNo", params[0]);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }

                return new Gson().fromJson(result, new TypeToken<ResultData<CaiGouOrderBean>>() {
                }.getType());
            }

            @Override
            protected void onSuccess(ResultData<CaiGouOrderBean> resultData) {
                String defErrMsg = "获取已采购订单列表失败";

                if (resultData == null) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(),
                            TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (resultData.DataList.size() == 0) {
                    Toast.makeText(getActivity(), "未查询到该工单的采购订单记录", Toast.LENGTH_SHORT).show();
                    return;
                }

                purchaseOrderList = resultData.DataList;
                listAdapter.notifyDataSetChanged();
            }
        };
        fetchOrderTask.setCancellable(false);
        fetchOrderTask.mmtExecute(mScheduleTask.TaskCode, mScheduleTask.PreCodeFormat);
    }

    /**
     * @param purchaseOrder  待插入的记录
     * @param updateListView 插入记录是否刷新列表
     */
    public void addSingleData(CaiGouOrderBean purchaseOrder, boolean updateListView) {
        purchaseOrderList.add(purchaseOrder);

        if (updateListView) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class PurchaseOrderListAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        public PurchaseOrderListAdapter(Context context) {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return purchaseOrderList.size();
        }

        @Override
        public Object getItem(int position) {
            return purchaseOrderList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.purchase_order_list_item, null);
            }

            CaiGouOrderBean purchaseOrder = purchaseOrderList.get(position);

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");
            ((TextView) MmtViewHolder.get(convertView, R.id.supplier)).setText(purchaseOrder.GongYinShang.getName());
            ((TextView) MmtViewHolder.get(convertView, R.id.amount)).setText(purchaseOrder.Num + " 元");
            ((TextView) MmtViewHolder.get(convertView, R.id.costCenter))
                    .setText(isExistCostCenter ? purchaseOrder.ChenBenCenter.getName() : purchaseOrder.GongChang.getName());

            Button delBtn = MmtViewHolder.get(convertView, R.id.btn_delete);

            if (allowDelete) {
                delBtn.setTag(position);
                delBtn.setOnClickListener(delClickListener);
            } else {
                delBtn.setVisibility(View.GONE);
            }

            return convertView;
        }

        private View.OnClickListener delClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("该采购订单将被删除？");
                okCancelDialogFragment.setLeftBottonText("取消");
                okCancelDialogFragment.setRightBottonText("删除");
                okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {

                        int selectIndex = Integer.parseInt(v.getTag().toString());
                        deletePurchaseOrder(purchaseOrderList.get(selectIndex));
                    }
                });
                okCancelDialogFragment.show(getFragmentManager(), "");
            }
        };

        private void deletePurchaseOrder(final CaiGouOrderBean caiGouOrderBean) {

            MmtBaseTask<Void, Void, ResultWithoutData> mmtBaseTask = new MmtBaseTask<Void, Void, ResultWithoutData>(getActivity()) {
                @Override
                protected ResultWithoutData doInBackground(Void... params) {

                    ResultWithoutData data;

                    caiGouOrderBean.CaseNo = mScheduleTask.TaskCode;
                    caiGouOrderBean.FlowCode = mScheduleTask.PreCodeFormat;
                    caiGouOrderBean.OperType = "删除采购订单";
                    caiGouOrderBean.OperTime = BaseClassUtil.getSystemTime();
                    caiGouOrderBean.OperMan = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName;

                    String dataStr = new Gson().toJson(caiGouOrderBean, CaiGouOrderBean.class);

                    // 执行网络操作
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/DelCaiGouOrder";

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
                protected void onSuccess(ResultWithoutData resultWithoutData) {
                    if (resultWithoutData == null) {
                        Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                    } else if (resultWithoutData.ResultCode != 200) {
                        Toast.makeText(getActivity(), TextUtils.isEmpty(resultWithoutData.ResultMessage) ? "删除失败" : resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                        purchaseOrderList.remove(caiGouOrderBean);
                        PurchaseOrderListAdapter.this.notifyDataSetChanged();
                    }
                }
            };
            mmtBaseTask.setCancellable(false);
            mmtBaseTask.mmtExecute();

        }
    }

}
