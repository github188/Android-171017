package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.adapter.TreeAdapter;
import com.mapgis.mmt.entity.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 多级树状结构视图
 */
public class MutilTreeView extends LinearLayout {

    private TreeAdapter ta;

    private ListView listView;

    public MutilTreeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView(context);
    }

    public MutilTreeView(Context context) {
        this(context, null);
    }

    private void initView(Context context) {

        listView = new ListView(context);
        listView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setSelector(R.drawable.item_focus_bg);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                // 这句话写在最后面
                ((TreeAdapter) arg0.getAdapter()).ExpandOrCollapse(arg2);
            }
        });

        addView(listView);
    }

    public void setData(Context context, Node root) {
        ta = new TreeAdapter(context, root);
        // 设置整个树是否显示复选框
        ta.setCheckBox(true);
        // 设置展开和折叠时图标
        ta.setExpandedCollapsedIcon(R.drawable.tree_ex, R.drawable.tree_ec);
        // 设置默认展开级别
        int defaultLevel = 2;
        if (MyApplication.getInstance().getConfigValue("ExpandAll", 0) == 1) {
            defaultLevel = 4;
        }
        ta.setExpandLevel(defaultLevel);
        listView.setAdapter(ta);
    }

    // 获得选中叶子节点
    public List<Node> getSeletedNodes() {
        List<Node> nodes = new ArrayList<Node>();

        if (ta != null) {
            nodes.addAll(ta.getSeletedLeafNodes());
        }
        return nodes;
    }

    /**
     * 设置树的展开级别
     *
     * @param expandLevel 展开级别
     */
    public void setTreeExpandLevel(int expandLevel) {
        ta.setExpandLevel(expandLevel);
    }
}
