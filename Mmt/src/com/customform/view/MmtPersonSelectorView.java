package com.customform.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.NodeEntity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.HandoverUserFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 人员选择器
 * Created by zoro at 2017/9/1.
 */
class MmtPersonSelectorView extends MmtBaseView {
    MmtPersonSelectorView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_checkbox;
    }

    private void traverseNode(@NonNull Node node, @NonNull List<String> checkedValues) {
        if (node.isLeaf()) {
            node.setChecked(checkedValues.contains(node.getValue()));
        } else {
            for (Node childNode : node.getChildren()) {
                traverseNode(childNode, checkedValues);
            }
        }
    }

    public View build(){

        final ImageButtonView view = new ImageButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.setValue(control.Value.length() != 0 ? control.Value : control.DefaultValues);

        MmtBaseTask<String, Void, Node> mmtBaseTask = new MmtBaseTask<String, Void, Node>(context, false) {
            @Override
            protected Node doInBackground(String... params) {

                Node node = null;

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/WorkFlow/GetMenTreeByRole";
                try {
                    String jsonResult = NetUtil.executeHttpGet(url, "role", params[0], "userID", params[1]);

                    if (TextUtils.isEmpty(jsonResult)) {
                        new Exception("获取数据失败：网络错误");
                    }
                    NodeEntity nodeEntity = new Gson().fromJson(jsonResult, new TypeToken<NodeEntity>() {
                    }.getType());
                    node = nodeEntity.toNode(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return node;
            }

            @Override
            protected void onSuccess(Node nodes) {

                if (nodes == null) {
                    Toast.makeText(context, "人员选择:获取人员数据失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                control.mTag = nodes;

                view.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final List<String> defValueList = new ArrayList<>();
                        String val = view.getValue();
                        if (!BaseClassUtil.isNullOrEmptyString(val)) {
                            defValueList.addAll(BaseClassUtil.StringToList(val, ","));
                        }

                        final Node node = (Node) control.mTag;

                        traverseNode(node, defValueList);

                        HandoverUserFragment fragment = new HandoverUserFragment(node);
                        Bundle args = new Bundle();
                        args.putBoolean("showOption", false);
                        args.putBoolean("allowEmpty", true);
                        fragment.setArguments(args);
                        fragment.setOnUserSelectedInterface(new HandoverUserFragment.OnUserSelectedInterface() {
                            @Override
                            public void onUserSelected(String userIds, String option) {
                                view.setValue(userIds);
                            }
                        });
                        fragment.show(getActivity().getSupportFragmentManager(), "");
                    }
                });
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute(control.ConfigInfo, String.valueOf(MyApplication.getInstance().getUserId()));

        return view;
    }
}
