package com.mapgis.mmt.module.gis.toolbar.layercontrol;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.adapter.TreeAdapter;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.entity.Node;
import com.zondy.mapgis.android.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

public class LayerManage extends Activity implements OnItemClickListener {

    protected boolean isContainCheckBox = true;
    private ListView code_list;
    private LinearLayout toolBar;
    public int count = 1;
    private MapView mapView;
    Node appTree;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layer_manage_treeview);

        toolBar = (LinearLayout) findViewById(R.id.toolBar);
        code_list = (ListView) findViewById(R.id.code_list);
        code_list.setOnItemClickListener(this);

        onCurCreate();

        mapView = MyApplication.getInstance().mapGISFrame.getMapView();


        if (GisQueryUtil.isofflineMap()) {
            appTree = LayereManageUtils.initOfflineLayerTree(mapView,true);
        } else {
            appTree = LayereManageUtils.initOnlineLayerTree(mapView,true);
        }



        TreeAdapter ta = new TreeAdapter(LayerManage.this, appTree);
        ta.setCheckBox(isContainCheckBox);// 设置整个树是否显示复选框
        ta.setExpandedCollapsedIcon(R.drawable.tree_ex, R.drawable.tree_ec);// 设置展开和折叠时图标
        ta.setExpandLevel(2);// 设置默认展开级别
        code_list.setAdapter(ta);
    }

    protected void onCurCreate() {
        setToolBar(new String[]{"确定", "", "", "退出"}, new int[]{0, 3});
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Node clickNode = (Node) ((TreeAdapter.ViewHolder) view.getTag()).mutilTreeItemSelect.getTag();

        if (clickNode.isLeaf()) {
            MyApplication.getInstance().showMessageWithHandle(clickNode.getText());
        }

        // 根据点击的项是否 是 叶子节点 来重置 ListView 显示的数据源 ， 写在点击事件处理的后面，避免处理的是更新之后的数据源
        ((TreeAdapter) parent.getAdapter()).ExpandOrCollapse(position);
    }

    // 设置底部工具栏
    private void setToolBar(String[] name_array, int[] pos_array) {
        LayoutInflater inflater = LayoutInflater.from(LayerManage.this);
        View view = inflater.inflate(R.layout.ok_cancel_button, null);

        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                MyApplication.getInstance().putConfigValue("appTree", appTree);

                List<Node> nodes = ((TreeAdapter) code_list.getAdapter()).getAll();

                List<String> checkedNodeNames = new ArrayList<>();

                for (int i = 0; i < nodes.size(); i++) {
                    Node n = nodes.get(i);

                    String name = n.getText();

                    if (!n.isChecked()) {
                        continue;
                    }

                    if (BaseClassUtil.isNullOrEmptyString(name)) {
                        continue;
                    }
                    if (name.equals(MyApplication.getInstance().getString(R.string.app_name))) {
                        continue;
                    }
                    if (name.equals(MyApplication.getInstance().mapGISFrame.getMapView().getMap().getName())) {
                        continue;
                    }
                    checkedNodeNames.add(name);

                }

                if (GisQueryUtil.isofflineMap()) {
                    LayereManageUtils.reShowMapForOffline(checkedNodeNames);
                } else {
                    LayereManageUtils.reShowMapForOnline(mapView, checkedNodeNames);
                }

                finish();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(ResultCode.LAYER_MANAGE_CANCEL);
                finish();
            }
        });

        toolBar.addView(view);
    }
}