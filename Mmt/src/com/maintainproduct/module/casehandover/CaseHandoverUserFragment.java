package com.maintainproduct.module.casehandover;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.maintainproduct.entity.HandoverEntity;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.MutilTreeView;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.R;

import java.util.List;

public class CaseHandoverUserFragment extends DialogFragment {
    private final MaintainSimpleInfo entity;

    private MutilTreeView treeView;


    /**
     * 是否允许多选
     */
    private boolean isMutilEnable;

    private Handler handler;
    private int what;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new GetUsersTask(getActivity()).executeOnExecutor(MyApplication.executorService, entity);
    }

    public CaseHandoverUserFragment(MaintainSimpleInfo entity) {
        this(entity, null);
    }

    public CaseHandoverUserFragment(MaintainSimpleInfo entity, Handler handler) {
        this(entity, handler, -1);
    }

    public CaseHandoverUserFragment(MaintainSimpleInfo entity, Handler handler, int what) {
        this.entity = entity;
        this.handler = handler;
        this.what = what;
    }

    public void setIsMutilEnable(boolean isMutilEnable) {
        this.isMutilEnable = isMutilEnable;
    }

    /**
     * 是否允许多选
     */
    public boolean isMutilEnable() {
        return isMutilEnable;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.case_handover_user_select, container, false);

        treeView = (MutilTreeView) view.findViewById(R.id.caseHandoverList);

        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Node> nodes = treeView.getSeletedNodes();

                if (nodes == null || nodes.size() == 0) {
                    Toast.makeText(getActivity(), "请选择承办人", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isMutilEnable && nodes.size() > 1) {
                    Toast.makeText(getActivity(), "不允许选择多个人员", Toast.LENGTH_SHORT).show();
                    return;
                }

                String undertakeman = "";

                for (Node node : nodes) {
                    undertakeman = undertakeman + node.getValue() + ",";
                }

                undertakeman = undertakeman.substring(0, undertakeman.length() - 1);

                HandoverEntity handoverEntity = new HandoverEntity(entity);
                handoverEntity.undertakeman = undertakeman;
                handoverEntity.option = ((EditText) view.findViewById(R.id.caseHandoverEdit)).getText().toString().trim();

                // handoverEntity.flowID = "";
                // handoverEntity.nextActiveID = "";

                Toast.makeText(getActivity(), "案件移交保存成功", Toast.LENGTH_SHORT).show();

                if (what > 0) {
                    Message msg = handler.obtainMessage();

                    msg.what = what;
                    msg.obj = handoverEntity;

                    handler.sendMessage(msg);
                } else {
                    if (handler != null) {// 移交上报合并为一个操作
                        Message msg = handler.obtainMessage();
                        msg.what = MaintenanceConstant.SERVER_BOTH_FEEDBACK_HANDOVER;
                        msg.obj = handoverEntity;
                        handler.sendMessage(msg);
                    } else {// 只移交
                        new CaseHandoverTask().createCaseHandoverData(handoverEntity);
                    }
                }

                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

                dismiss();

            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        return view;
    }

    class GetUsersTask extends GetHandoverUsersTask {

        public GetUsersTask(FragmentActivity activity) {
            super(activity);
        }

        @Override
        protected void onTaskDone(Node node) {
            if (node != null) {
                treeView.setData(getActivity(), node);
            }
        }
    }
}
