package com.customform.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.repair.common.BaseTaskResults;

import java.util.ArrayList;
import java.util.List;

/**
 * 站点选择器
 * Created by zoro at 2017/9/1.
 */
class MmtStationSelectorView extends MmtBaseView implements MmtBaseView.ReadonlyHandleable {

    MmtStationSelectorView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_radiobutton;
    }

    public View build() {

        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        // 只读情况下能取到默认值就给默认值，没有就从网络上取再将取到的第一个数据当做值
        if (control.isReadOnly()) {
            view.getButton().setVisibility(View.GONE);

            String value = "";
            if (!TextUtils.isEmpty(control.Value)) {
                value = control.Value;
            } else if (!TextUtils.isEmpty(control.DefaultValues)) {
                value = control.DefaultValues;
            } else if (!TextUtils.isEmpty(control.ConfigInfo)) {
                value = control.ConfigInfo;
            }
            value = value.trim();

            if (!TextUtils.isEmpty(value)) {
                view.setValue(value);
                return view;
            }
        }

        // 获取用户的所属站点
        new BaseTaskResults<String, Void, String>(context) {
            @NonNull
            @Override
            protected String getRequestUrl() throws Exception {
                return ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc"
                        + "/CommonModule/GetStationListByUserID?userID=" + userID;
            }
            @Override
            protected void onSuccess(Results<String> results) {
                ResultData<String> resultData = results.toResultData();
                if (resultData.ResultCode != ResultData.SUCCEED || resultData.DataList.size() == 0) {
                    Toast.makeText(context, "获取所属站点信息失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                final List<String> values = new ArrayList<>(resultData.DataList);
                if (control.Value.length() > 0) {
                    view.setValue(control.Value);
                } else {
                    view.setValue(values.size() > 0 ? values.get(0) : "");
                }

                // 非只读才允许修改值
                if (!control.isReadOnly()) {
                    view.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, values);
                            fragment.show(getActivity().getSupportFragmentManager(), "");
                            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    view.setValue(value);
                                }
                            });
                        }
                    });
                }
            }
        }.mmtExecute();

        return view;
    }
}
