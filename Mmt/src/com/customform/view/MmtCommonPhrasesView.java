package com.customform.view;

import android.content.Context;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageEditButtonView;
import com.mapgis.mmt.common.widget.fragment.ListTreeDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.eventreport.FetchEventTypeTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用语
 * Created by zoro at 2017/9/1.
 */
class MmtCommonPhrasesView extends MmtBaseView {
    MmtCommonPhrasesView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_common_expression;
    }

    /**
     * 创建 常用语 类型视图
     */
    public ImageEditButtonView build() {
        final ImageEditButtonView view = new ImageEditButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        if (control.Validate.equals("1")) {
            view.isMustDo(true);
        }

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        String[] levelNames = control.ConfigInfo.split("\\.");

        if (levelNames.length == 2) {

            new FetchEventTypeTask(context, false, new MmtBaseTask.OnWxyhTaskListener<ResultData<EventTypeItem>>() {
                @Override
                public void doAfter(ResultData<EventTypeItem> result) {
                    if (result.DataList == null || result.DataList.size() == 0)
                        return;

                    ArrayList<EventTypeItem> contentNodeList = new ArrayList<>();
                    contentNodeList.addAll(result.DataList);

                    final Node root = new Node("常用语", "000000");
                    root.setCheckBox(false);

                    for (int i = 0; i < contentNodeList.size(); i++) {
                        Node levelOneNode = new Node(contentNodeList.get(i).NODENNAME, "000" + i);
                        levelOneNode.setParent(root);
                        root.add(levelOneNode);

                        for (int j = 0; j < contentNodeList.get(i).SubItem.size(); j++) {
                            Node levelTwoNode = new Node(contentNodeList.get(i).SubItem.get(j).NODENNAME, "000" + i + j);
                            levelTwoNode.setParent(levelOneNode);
                            levelOneNode.add(levelTwoNode);
                        }
                    }

                    view.setOnButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListTreeDialogFragment contentListTreeFragment = new ListTreeDialogFragment(context, "描述常用语", root, 2, true);

                            contentListTreeFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                                @Override
                                public void onRightButtonClick(View v) {
                                    @SuppressWarnings("unchecked")
                                    List<Node> nodes = (List<Node>) v.getTag();

                                    String result = "";

                                    for (Node node : nodes) {
                                        result += node.getParent().getText() + ":" + node.getText() + "; ";
                                    }
                                    if (result.length() > 0) {
                                        result = result.substring(0, result.length() - 1);
                                    }

                                    view.setValue(result);
                                }
                            });
                            contentListTreeFragment.show(getActivity().getSupportFragmentManager(), "");
                        }
                    });
                    view.getImageView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.button.performClick();
                        }
                    });
                }
            }).mmtExecute(levelNames[0], levelNames[1]);
        }

        return view;
    }
}
