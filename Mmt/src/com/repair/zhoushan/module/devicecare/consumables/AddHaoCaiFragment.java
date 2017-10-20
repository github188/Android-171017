package com.repair.zhoushan.module.devicecare.consumables;

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
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.Material;

import java.util.ArrayList;
import java.util.List;

public class AddHaoCaiFragment extends Fragment {

    private List<Material> materialList = new ArrayList<Material>();
    private HaoCaiAdapter listAdapter;

    private ScheduleTask mScheduleTask;
    private String careType = "";

    private boolean showOnly = false;

    public List<Material> getMaterialList() {
        return materialList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {

            mScheduleTask = args.getParcelable("ListItemEntity");
            showOnly = args.getBoolean("ShowOnly", false);

            if (args.containsKey("CareType")) {
                careType = args.getString("CareType");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pulltorefresh_both, container, false);

        PullToRefreshListView mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.mainFormList);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        ListView mListView = mPullRefreshListView.getRefreshableView();

        this.listAdapter = new HaoCaiAdapter(getActivity(), materialList, showOnly);
        mListView.setAdapter(listAdapter);

        getCaiLiaoListData();

        return view;

    }

    private void getCaiLiaoListData() {

        MmtBaseTask<String, Void, ResultData<Material>> fetchOrderTask = new MmtBaseTask<String, Void, ResultData<Material>>(getActivity()) {
            @Override
            protected ResultData<Material> doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchMaterials";

                String result = NetUtil.executeHttpGet(url, "caseNo", params[0], "bizName", params[1],
                        "filterValue", params[2], "deviceType", params[3]);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                ResultData<Material> resultData = new Gson().fromJson(result, new TypeToken<ResultData<Material>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<Material> resultData) {
                String defErrMsg = "获取材料列表失败";

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
                    Toast.makeText(getActivity(), "未查询材料列表记录", Toast.LENGTH_SHORT).show();
                    return;
                }

                materialList.addAll(resultData.DataList);
                listAdapter.notifyDataSetChanged();
            }
        };
        fetchOrderTask.setCancellable(false);
        fetchOrderTask.mmtExecute(mScheduleTask.TaskCode, mScheduleTask.BizName,
                TextUtils.isEmpty(careType) ? "" : careType,
                TextUtils.isEmpty(mScheduleTask.EquipmentType) ? "" : mScheduleTask.EquipmentType);

    }
}