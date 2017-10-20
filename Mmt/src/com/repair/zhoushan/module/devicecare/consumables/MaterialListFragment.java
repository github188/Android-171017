package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.ArrayList;
import java.util.List;

public class MaterialListFragment extends Fragment {

    private PullToRefreshListView mPullRefreshListView;
    private ListView mListView;
    private MaterialListAdapter mListAdapter;

    private boolean isExistCostCenter; // default 'false'
    private List<WuLiaoBean> materialList = new ArrayList<>();

    private ScheduleTask mScheduleTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mScheduleTask = getArguments().getParcelable("ListItemEntity");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pulltorefresh_both, container, false);

        this.mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mainFormList);
        this.mListView = mPullRefreshListView.getRefreshableView();

        mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mListView.setDivider(new ColorDrawable(0x77808080));
        mListView.setDividerHeight(DimenTool.dip2px(getActivity(), 0.5f));

        this.mListAdapter = new MaterialListAdapter(getActivity(), materialList, isExistCostCenter);
        mListView.setAdapter(mListAdapter);

        getDataList();

        return view;
    }

    public List<WuLiaoBean> getMaterialList() {
        return materialList;
    }

    public boolean isExistCostCenter() {
        return isExistCostCenter;
    }

    private void getDataList() {

        MmtBaseTask<String, Void, ResultData<WuLiaoBean>> fetchDataTask
                = new MmtBaseTask<String, Void, ResultData<WuLiaoBean>>(getActivity(), true) {
            @Override
            protected ResultData<WuLiaoBean> doInBackground(String... params) {

                // 检查是否存在成本中心(未取到暂未给出提示)
                String urlExistCBCenter = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/ExistCBCenter"
                        + "?bizCode=" + params[1] + "&objType=物料";

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
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchUsedWuliaoList"
                        + "?caseNo=" + params[0];

                String result = NetUtil.executeHttpGet(url);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                ResultData<WuLiaoBean> resultData = new Gson().fromJson(result, new TypeToken<ResultData<WuLiaoBean>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<WuLiaoBean> resultData) {

                String defErrMsg = "获取已领物料列表失败";
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
                    Toast.makeText(getActivity(), "未查询到该工单的领料记录", Toast.LENGTH_SHORT).show();
                    if (materialList.size() > 0) {
                        materialList.clear();
                        mListAdapter.notifyDataSetChanged();
                    }
                    return;
                }

                mListAdapter.setExistCostCenter(isExistCostCenter);

                materialList.clear();
                materialList.addAll(resultData.DataList);
                mListAdapter.notifyDataSetChanged();
            }
        };
        fetchDataTask.setCancellable(false);
        fetchDataTask.mmtExecute(mScheduleTask.TaskCode, mScheduleTask.PreCodeFormat);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 领料、退料成功后返回需要刷新列表
        if (requestCode == Constants.DEFAULT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getDataList();
        }
    }

}
