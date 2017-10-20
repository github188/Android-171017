package com.mapgis.mmt.module.gis.toolbar.layercontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.adapter.TreeAdapter;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.BitmapUtil;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.ServerLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mapgis.mmt.config.MobileConfig.MapConfigInstance;

public class LayerControlMapMenu extends BaseMapMenu implements AdapterView.OnItemClickListener {

    private ListView code_list;
    PopupWindow popupWindow;
    Node appTree;
    com.zondy.mapgis.map.Map map;


    public LayerControlMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        try {

            map = mapView.getMap();

            View view = initView();

            popupWindow = new PopupWindow(view, -2, -1, true);

            popupWindow.setContentView(view);
            popupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));

            popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                    // 这里如果返回true的话，touch事件将被拦截
                    // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
                }
            });
            popupWindow.showAtLocation(mapGISFrame.getWindow().getDecorView(),
                    Gravity.RIGHT, 0, 0);

            return true;
        } catch (Exception e) {
            Toast.makeText(mapGISFrame, "没有取到图层", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    View dt_part_view;
    List<LayerControlItem> layerControlDatas;

    /**
     * 底图展示初始化
     *
     * @param view
     */
    private void initTilesView(View view) {
        dt_part_view = view.findViewById(R.id.dt_part);
        dt_part_view.setVisibility(View.GONE);

        final GridView gridView = (GridView) view.findViewById(R.id.gview);

        List<MapLayerConfig> layerConfigs = MapConfigInstance.Layers;
        if (layerConfigs == null) {
            return;
        }
        List<LayerControlItem> classTemp = new ArrayList<>();
        layerControlDatas = MyApplication.getInstance().getConfigValue("layerControlDatas", classTemp.getClass());
        if (layerControlDatas == null) {
            layerControlDatas = new ArrayList<>();
            for (int i = 0; i < layerConfigs.size(); i++) {
                MapLayerConfig mapLayerConfig = layerConfigs.get(i);

                //排除管网
                boolean isGW = MapConfigInstance.isGISGW(mapLayerConfig.Type);
                if (isGW) {
                    continue;
                }

                //排除配置了离线地形，但是本地不存在的情况
                boolean isOffline = MapConfigInstance.isOfflineLayer(mapLayerConfig.Type);
                String fullPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Map) + mapLayerConfig.Name + "/" + mapLayerConfig.Name + ".db";
                if (isOffline && !new File(fullPath).exists()) {
                    continue;
                }

                LayerControlItem item = new LayerControlItem();
                item.name = TextUtils.isEmpty(mapLayerConfig.label) ? mapLayerConfig.Name : mapLayerConfig.label;
                item.isShow = "true".equalsIgnoreCase(mapLayerConfig.Visible);
                item.index = i;

                int drawableid = LayereManageUtils.getImageResourceID(item.name);
                item.bitmap = BitmapUtil.getBitmap(mapGISFrame.getResources(), drawableid);

                layerControlDatas.add(item);
            }
            MyApplication.getInstance().putConfigValue("layerControlDatas", layerControlDatas);
        }

        dt_part_view.setVisibility(View.GONE);
        int dxLayerSize = layerControlDatas.size();
        if (dxLayerSize == 0) {
            return;
        }
        gridView.setNumColumns(dxLayerSize);
        if (dxLayerSize > 3) {
            gridView.setNumColumns(3);
        }

        dt_part_view.setVisibility(View.VISIBLE);
        LayerControlAdpter adpter = new LayerControlAdpter(mapGISFrame, layerControlDatas);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (layerControlDatas == null) {
                    return;
                }
                LayerControlItem layerControlItem = layerControlDatas.get(position);
                layerControlItem.isShow = !layerControlItem.isShow;

                MapLayer mapLayer = map.getLayer(position);
                if (mapLayer == null) {
                    return;
                }
                mapLayer.setVisible(layerControlItem.isShow);
                mapView.refresh();

                View childView = gridView.getChildAt(position);
                if (childView == null) {
                    return;
                }
                if (!(childView instanceof LinearLayout)) {
                    return;
                }
                if (layerControlItem.isShow) {
                    childView.setBackgroundResource(R.drawable.dx_image_shape);
                    View son = childView.findViewById(R.id.dx_name);
                    if (son instanceof TextView) {
                        ((TextView) son).setTextColor(0xff2881a2);
                    }
                } else {
                    childView.setBackgroundResource(0);
                    View son = childView.findViewById(R.id.dx_name);
                    if (son instanceof TextView) {
                        ((TextView) son).setTextColor(0xff333333);
                    }
                }
                MyApplication.getInstance().putConfigValue("layerControlDatas", layerControlDatas);
            }
        });
        gridView.setAdapter(adpter);
    }

    private void initGWSpinner(View view) {

        final TextView txt_gwName = (TextView) view.findViewById(R.id.txt_gwName);

        List<MapLayerConfig> layerConfigs = MapConfigInstance.Layers;
        if (layerConfigs == null) {
            return;
        }
        final List<String> data_list = new ArrayList<>();
        for (MapLayerConfig mapLayerConfig : layerConfigs) {
            //排除管网
            boolean isGW = MapConfigInstance.isGISGW(mapLayerConfig.Type);
            if (!isGW) {
                continue;
            }
            if ("true".equalsIgnoreCase(mapLayerConfig.Visible)) {
                data_list.add(0, mapLayerConfig.Name);
                txt_gwName.setText(mapLayerConfig.Name);
            } else {
                data_list.add(mapLayerConfig.Name);
            }
        }
        if (data_list.size() < 2) {
            return;
        }
        final ListDialogFragment listDialogFragment = new ListDialogFragment("管网切换", data_list);
        listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String gwName) {
                txt_gwName.setText(gwName);

                MapLayerConfig mapLayerConfig = MapConfigInstance.getMapLayerByName(gwName);
                boolean isOfflineGW = MapConfig.MOBILE_EMS.equalsIgnoreCase(mapLayerConfig.Type);

                //地图显示控制
                com.zondy.mapgis.map.Map map = mapView.getMap();
                int layerCount = map.getLayerCount();
                for (int i = 0; i < layerCount; i++) {
                    MapLayer layer = map.getLayer(i);

                    if (isOfflineGW) {
                        //当前是离线管网
                        if (layer instanceof ServerLayer) {
                            if (data_list.contains(layer.getName())) {
                                //在线管网，在线管网的名字和服务名一样
                                layer.setVisible(false);
                            }
                            //其他的是底图
                        } else {
                            layer.setVisible(true);
                        }
                    } else {
                        if (layer instanceof ServerLayer) {
                            if (data_list.contains(layer.getName())) {
                                //在线管网，在线管网的名字和服务名一样
                                layer.setVisible(true);
                            }
                        } else {
                            layer.setVisible(false);
                        }
                    }
                }

                mapView.refresh();


                //图层树控制
                mapLayerConfig.Visible = "true";
                for (MapLayerConfig mapLayerConfig1 : MapConfigInstance.Layers) {
                    if (gwName.equalsIgnoreCase(mapLayerConfig1.Name)) {
                        continue;
                    }
                    mapLayerConfig1.Visible = "false";
                }
                initGWView(contentView);


                //在线或离线查询方式控制
                if (mapLayerConfig != null) {
                    //在线地图一定是在线查询
                    if (MapConfig.MOBILE_DYNAMIC.equalsIgnoreCase(mapLayerConfig.Type)) {
                        MobileConfig.MapConfigInstance.IsVectorQueryOnline = true;
                    } else {
                        //离线地图，查询方式不变,最初始的方式
                        MobileConfig.MapConfigInstance.IsVectorQueryOnline = MyApplication.getInstance().getConfigValue("IsVectorQueryOnline", 0) == 1;
                    }
                    MapMenuRegistry.getInstance().registQueryMenu();
                }
            }
        });

        MyApplication.getInstance().putConfigValue("IsVectorQueryOnline", MobileConfig.MapConfigInstance.IsVectorQueryOnline ? 1 : 0);
        txt_gwName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDialogFragment.show(mapGISFrame.getSupportFragmentManager(), "");
            }
        });
    }

    /**
     * 管网展示初始化
     *
     * @param view
     */
    private void initGWView(View view) {

        //图层树
        code_list = (ListView) view.findViewById(R.id.code_list);
        code_list.setOnItemClickListener(this);

        if (GisQueryUtil.isofflineMap()) {
            appTree = LayereManageUtils.initOfflineLayerTree(mapView, false);
        } else {
            appTree = LayereManageUtils.initOnlineLayerTree(mapView, false);
        }

        TreeAdapter ta = new TreeAdapter(mapGISFrame, appTree);
        ta.setCheckBox(true);// 设置整个树是否显示复选框
        ta.setExpandedCollapsedIcon(R.drawable.tree_ex, R.drawable.tree_ec);// 设置展开和折叠时图标
        ta.setExpandLevel(2);// 设置默认展开级别
        code_list.setAdapter(ta);

        //取消和确定事件监听
        initClickEvent(view);
    }

    View contentView;

    private View initView() {
        LayoutInflater inflater = (LayoutInflater) mapGISFrame.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.layercontrol_popwindow, null);

        //底图
        initTilesView(contentView);

        //管网下拉列表
        initGWSpinner(contentView);

        //管网
        initGWView(contentView);

        return contentView;
    }


    View cancelBtn;

    //控制管网
    private void initClickEvent(View view) {
        cancelBtn = view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow == null) {
                    return;
                }
                if (!popupWindow.isShowing()) {
                    return;
                }
                popupWindow.dismiss();
            }
        });
        view.findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
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

                cancelBtn.performClick();
            }
        });
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

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        if (resultCode != ResultCode.LAYER_MANAGE_SELECTED) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String, Boolean> layerHashtable = (Map<String, Boolean>) intent.getSerializableExtra("layerHashtable");

        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
        layerEnum.moveToFirst();
        MapLayer mapLayer;
        while ((mapLayer = layerEnum.next()) != null) {
            if (layerHashtable.containsKey(mapLayer.getName()))
                mapLayer.setVisible(layerHashtable.get(mapLayer.getName()));
        }

        mapView.refresh();

        return true;
    }

    @Override
    public View initTitleView() {
        return null;
    }
}
