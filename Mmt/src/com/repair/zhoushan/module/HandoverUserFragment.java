package com.repair.zhoushan.module;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.MutilTreeView;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.R;

import java.util.List;

public class HandoverUserFragment extends DialogFragment {

    private MutilTreeView treeView;
    private EditText eTxtOption;

    /**
     * 选择人信息中是否包含 next_node_id, 默认包含
     */
    private boolean isContainNodeId = true;

    private Handler handler;
    private int what;
    private Node mNode;

    private boolean showOption = true; // 是否展示输入框
    private boolean allowEmpty = false; // 是否允许选择为空
    private boolean allowMulti = true; // 是否允许多选

    public HandoverUserFragment(Node node) {
        this.mNode = node;
    }

    public HandoverUserFragment(Handler handler, int what, Node node) {
        this.handler = handler;
        this.what = what;
        this.mNode = node;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnUserSelectedInterface) {
            this.onUserSelectedInterface = (OnUserSelectedInterface) context;
        }

        Bundle args = getArguments();
        if (args != null) {
            this.showOption = args.getBoolean("showOption", true);
            this.allowEmpty = args.getBoolean("allowEmpty", false);
            this.allowMulti = args.getBoolean("allowMulti", true);
        }
    }

    private int[] nodeCounts = {0, 0, 0, 0};

    private void getTreeLevelNodeCount(Node rootNode, final int level) {

        if (rootNode != null && level < 4) {

            nodeCounts[level]++;

            if (!rootNode.isLeaf()) {
                List<Node> childrenNodes = rootNode.getChildren();
                for (Node node : childrenNodes) {
                    getTreeLevelNodeCount(node, level + 1);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        final View view = inflater.inflate(R.layout.case_handover_user_select, container, false);

        eTxtOption = (EditText) view.findViewById(R.id.caseHandoverEdit);
        if (!showOption) {
            eTxtOption.setVisibility(View.GONE);
        }

        treeView = (MutilTreeView) view.findViewById(R.id.caseHandoverList);
        treeView.setData(getActivity(), mNode);

        getTreeLevelNodeCount(mNode, 0);

        if (MyApplication.getInstance().getConfigValue("ExpandAll", 0) == 1) {
            treeView.setTreeExpandLevel(4);
        } else {
            if ((nodeCounts[0] + nodeCounts[1] + nodeCounts[2] + nodeCounts[3]) < 9) {
                treeView.setTreeExpandLevel(3);
            } else if ((nodeCounts[0] + nodeCounts[1] + nodeCounts[2]) < 9) {
                treeView.setTreeExpandLevel(2);
            } else {
                treeView.setTreeExpandLevel(1);
            }
        }

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Node> nodes = treeView.getSeletedNodes();

                if (!allowEmpty && (nodes == null || nodes.size() == 0)) {
                    Toast.makeText(getActivity(), "请选择人员", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!allowMulti && nodes != null && nodes.size() > 1) {
                    Toast.makeText(getActivity(), "不允许选择多个人员", Toast.LENGTH_SHORT).show();
                    return;
                }

                String option = "";
                String undertakeman = "";
                if (nodes != null && nodes.size() > 0) {
                    option = eTxtOption.getText().toString().trim();
                    undertakeman = resolveSelectedUser(nodes);
                }

                if (!allowEmpty && TextUtils.isEmpty(undertakeman)) {
                    Toast.makeText(getActivity(), "请选择人员", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (handler != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("undertakeman", undertakeman);
                    bundle.putString("option", option);

                    Message msg = handler.obtainMessage();
                    msg.what = what;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

                if (onUserSelectedInterface != null) {
                    onUserSelectedInterface.onUserSelected(undertakeman, option);
                }

                dismiss();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    @NonNull
    protected String resolveSelectedUser(List<Node> nodes) {

        StringBuilder undertakeMan = new StringBuilder("");

        // 服务器返回的Node的Value格式为 "next_node_id/user_id"
        if (isContainNodeId) {
            // 流程移交
            for (Node node : nodes) {
                String nodeValue = node.getValue();
//                if (nodeValue.split("/").length == 2) {
//                    undertakeMan.append(node.getValue()).append(",");
//                }
                undertakeMan.append(node.getValue()).append(",");
            }
        } else {
            // 事件上报
            for (Node node : nodes) {
                undertakeMan.append(node.getValue().split("/")[1]).append(",");
            }
        }

        if (undertakeMan.length() > 0) {
            undertakeMan.deleteCharAt(undertakeMan.length() - 1);
        }

        return undertakeMan.toString();
    }

    public void setIsContainNodeId(boolean isContainNodeId) {
        this.isContainNodeId = isContainNodeId;
    }

    public interface OnUserSelectedInterface {
        void onUserSelected(String userIds, String option);
    }

    public void setOnUserSelectedInterface(OnUserSelectedInterface onUserSelectedInterface) {
        this.onUserSelectedInterface = onUserSelectedInterface;
    }

    private OnUserSelectedInterface onUserSelectedInterface;
}
