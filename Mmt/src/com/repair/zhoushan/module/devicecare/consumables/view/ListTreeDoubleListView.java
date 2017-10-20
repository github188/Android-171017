package com.repair.zhoushan.module.devicecare.consumables.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;

import java.util.ArrayList;
import java.util.List;

public class ListTreeDoubleListView extends LinearLayout implements ViewBaseAction, View.OnClickListener {

    private ListView leftListView;
    private ListView rightListView;

    private TextView txtLeftTitle;
    private TextView txtRightTitle;

    // 一级数据列表
    private List<SAPBean> groups;
    // 二级列表的所有数据
    // private SparseArray<LinkedList<SAPBean>> children = new SparseArray<LinkedList<SAPBean>>();
    private List<List<SAPBean>> children;

    // 当前二级可选数据列表
    private List<SAPBean> childrenItem = new ArrayList<SAPBean>();

    private TextAdapter leftListAdapter;
    private TextAdapter rightListAdapter;

    private OnItemSelectListener onItemSelectListener;
    private int curLeftPosition = 0;
    private int curRightPosition = 0;
    private String showString = "不限";

    private enum showSubwayOrBcd {
        SUBWAY, BCD
    }

    private showSubwayOrBcd displayItem = showSubwayOrBcd.BCD;

    public ListTreeDoubleListView(Context context, List<SAPBean> lOneListData, List<List<SAPBean>> lTwoListData) {
        super(context);
        this.groups = lOneListData;
        this.children = lTwoListData;
        init(context);
    }

    public ListTreeDoubleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void updateShowText(String showArea, String showBlock) {
        if (showArea == null || showBlock == null) {
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).equals(showArea)) {
                leftListAdapter.setSelectedPosition(i);
                childrenItem.clear();
                if (i < children.size()) {
                    childrenItem.addAll(children.get(i));
                }
                curLeftPosition = i;
                break;
            }
        }
        for (int j = 0; j < childrenItem.size(); j++) {
            if (childrenItem.get(j).getName().replace("不限", "").equals(showBlock.trim())) {
                rightListAdapter.setSelectedPosition(j);
                curRightPosition = j;
                break;
            }
        }
        setDefaultSelect();
    }

    private void init(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_tree_double_list, this, true);
        leftListView = (ListView) findViewById(R.id.listView_left);
        rightListView = (ListView) findViewById(R.id.listView_right);

        txtLeftTitle = (TextView) findViewById(R.id.txt_left_title);
        txtRightTitle = (TextView) findViewById(R.id.txt_right_title);
        // txtLeftTitle.setOnClickListener(this);
        // txtRightTitle.setOnClickListener(this);

        setBackgroundDrawable(getResources().getDrawable(R.drawable.list_tree_bg));

        leftListAdapter = new TextAdapter(context, groups, R.drawable.list_tree_level_one_selected_bg, R.drawable.list_tree_normal_item_selector);
        leftListAdapter.setTextSize(17);
        leftListAdapter.setSelectedPositionNoNotify(curLeftPosition);
        leftListView.setAdapter(leftListAdapter);
        leftListAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (position < children.size()) {
                    childrenItem.clear();
                    childrenItem.addAll(children.get(position));
                    rightListAdapter.notifyDataSetChanged();
                }
            }
        });
        if (curLeftPosition < children.size())
            childrenItem.addAll(children.get(curLeftPosition));
        rightListAdapter = new TextAdapter(context, childrenItem, R.drawable.list_tree_selected_item_bg, R.drawable.list_tree_reverse_item_selector);
        rightListAdapter.setTextSize(15);
        rightListAdapter.setSelectedPositionNoNotify(curRightPosition);
        rightListView.setAdapter(rightListAdapter);
        rightListAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, final int position) {

                showString = childrenItem.get(position).getName();
                if (onItemSelectListener != null) {

                    onItemSelectListener.getValue(leftListAdapter.getSelectedPosition(), rightListAdapter.getSelectedPosition(), showString);
                }

            }
        });
        if (curRightPosition < childrenItem.size())
            showString = childrenItem.get(curRightPosition).getName();
        if (showString.contains("不限")) {
            showString = showString.replace("不限", "");
        }
        setDefaultSelect();

    }

    public void setDefaultSelect() {
        leftListView.setSelection(curLeftPosition);
        rightListView.setSelection(curRightPosition);
    }

    public String getShowText() {
        return showString;
    }

    public void setOnSelectListener(OnItemSelectListener onSelectListener) {
        onItemSelectListener = onSelectListener;
    }

    public interface OnItemSelectListener {
        void getValue(int levelOneSelIndex, int levelTwoSelIndex, String showText);
    }

    @Override
    public void hideMenu() {

    }

    @Override
    public void showMenu() {

    }

    @Override
    public void onClick(View v) {
        if (v == txtLeftTitle) {
            if (displayItem == showSubwayOrBcd.BCD) {

            } else {

            }
        } else if (v == txtRightTitle) {
            leftListAdapter = new TextAdapter(getContext(), groups, R.drawable.list_tree_selected_item_bg, R.drawable.list_tree_normal_item_selector);
            leftListAdapter.setTextSize(17);
            leftListAdapter.setSelectedPositionNoNotify(curLeftPosition);
            leftListView.setAdapter(leftListAdapter);
            leftListAdapter.notifyDataSetInvalidated();
            leftListAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    if (position < children.size()) {
                        childrenItem.clear();
                        childrenItem.addAll(children.get(position));
                        rightListAdapter.notifyDataSetChanged();
                    }
                }
            });
            if (curLeftPosition < children.size())
                childrenItem.addAll(children.get(curLeftPosition));
            rightListAdapter = new TextAdapter(getContext(), childrenItem, R.drawable.list_tree_level_one_selected_bg, R.drawable.list_tree_reverse_item_selector);
            rightListAdapter.setTextSize(15);
            rightListAdapter.setSelectedPositionNoNotify(curRightPosition);
            rightListView.setAdapter(rightListAdapter);
            rightListAdapter.setOnItemClickListener(new TextAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, final int position) {

                    showString = childrenItem.get(position).getName();
                    if (onItemSelectListener != null) {

                        // onItemSelectListener.getValue(showString);
                    }

                }
            });
            if (curRightPosition < childrenItem.size())
                showString = childrenItem.get(curRightPosition).getName();
            if (showString.contains("不限")) {
                showString = showString.replace("不限", "");
            }
            setDefaultSelect();
        }
    }

    public void notifyDataSetChanged() {
        rightListAdapter.notifyDataSetChanged();
        leftListAdapter.notifyDataSetChanged();
    }
}
