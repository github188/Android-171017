package com.repair.zhoushan.module.devicecare.merchantsecuritycheck;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;
import com.repair.zhoushan.module.devicecare.TaskDetailActivity;

public class RelatedDeviceListFragment extends DialogFragment {

    private String id = "";
    private String deviceType = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        this.id = args.getString("ID");
        this.deviceType = args.getString("DeviceType");
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = DimenTool.dip2px(getActivity(), 300);
            int height = DimenTool.dip2px(getActivity(), 500);
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return inflater.inflate(R.layout.custom_listdialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.listDialogTitle)).setText(deviceType);

        Fragment fragment = null;
        switch (deviceType) {
            case TaskDetailActivity.MSC_RELATED_REGULATOR:
                fragment = new RelatedRegulatorListFragment();
                break;
            case TaskDetailActivity.MSC_RELATED_METER:
                fragment = new RelatedMeterListFragment();
                break;
        }

        Bundle args = new Bundle();
        args.putString("ID", id);
        args.putBoolean("IsCheckVisible", false);
        fragment.setArguments(args);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(R.id.listContainer, fragment);
        ft.show(fragment);
        ft.commitAllowingStateLoss();

    }

    public void naviToFeedback(final String id, final String gisCode) {

        MmtBaseTask<Void, Void, ResultData<MaintenanceFeedBack>> mmtBaseTask
                = new MmtBaseTask<Void, Void, ResultData<MaintenanceFeedBack>>(getActivity()) {

            @Override
            protected ResultData<MaintenanceFeedBack> doInBackground(Void... params) {

                ResultData<MaintenanceFeedBack> resultData;

                String bizName = "";
                if (TaskDetailActivity.MSC_RELATED_REGULATOR.equals(deviceType)) {
                    bizName = "调压器养护";
                } else if (TaskDetailActivity.MSC_RELATED_METER.equals(deviceType)) {
                    bizName = "工商户表具养护";
                }

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetMaintenanceFBConfigList?BizName=" + bizName;
                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        throw new Exception("获取数据失败:网络请求错误");
                    }

                    Results<MaintenanceFeedBack> results = new Gson().fromJson(jsonResult, new TypeToken<Results<MaintenanceFeedBack>>() {
                    }.getType());
                    resultData = results.toResultData();

                } catch (Exception e) {
                    e.printStackTrace();
                    resultData = new ResultData<>();
                    resultData.ResultCode = -100;
                    resultData.ResultMessage = e.getMessage();
                }

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<MaintenanceFeedBack> resultData) {
                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(), resultData.ResultMessage, Toast.LENGTH_SHORT).show(); 
                } else if (resultData.DataList == null || resultData.DataList.size() == 0) {
                    Toast.makeText(getActivity(), "没有获取到养护反馈配置信息", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(getActivity(), MerchantSecurityCheckDCActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("GISCode", gisCode);
                    intent.putParcelableArrayListExtra("MaintenanceFBConfig", resultData.DataList);
                    startActivity(intent);

                    RelatedDeviceListFragment.this.dismiss();
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }
}
