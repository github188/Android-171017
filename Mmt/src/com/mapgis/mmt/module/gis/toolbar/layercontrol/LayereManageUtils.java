package com.mapgis.mmt.module.gis.toolbar.layercontrol;

import android.os.Message;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MapLayerConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.onliemap.dynamic.MmtDynamicMapServer;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.map.GroupLayer;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.ServerLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mapgis.mmt.ReaderTast.count;

/**
 * Created by lyunfan on 17/3/2.
 */

public class LayereManageUtils {

    //管网图层名列表
    public static List<String> gwLayerNames;
    //底图图层名列表
    public static List<String> dtLayerNames;


    private static List<String> getGWLayerNames() {
        if (gwLayerNames == null) {
            initLayerNames();
        }
        return gwLayerNames;
    }

    private static List<String> getDTLayerNames() {
        if (dtLayerNames == null) {
            initLayerNames();
        }
        return dtLayerNames;
    }

    private static void initLayerNames() {
        List<MapLayerConfig> Layers = MobileConfig.MapConfigInstance.Layers;
        if (Layers == null) {
            return;
        }
        if (gwLayerNames != null && dtLayerNames != null) {
            return;
        }

        if (gwLayerNames == null) {
            gwLayerNames = new ArrayList<>();
        }
        gwLayerNames.clear();

        if (dtLayerNames == null) {
            dtLayerNames = new ArrayList<>();
        }
        dtLayerNames.clear();

        for (int i = 0; i < Layers.size(); i++) {
            MapLayerConfig layer = MobileConfig.MapConfigInstance.Layers.get(i);

            String layerType = layer.Type;
            if (layerType.equals(MapConfig.MOBILE_DYNAMIC) || layerType.equals(MapConfig.MOBILE_EMS)) {
                gwLayerNames.add(layer.Name);
            } else {
                dtLayerNames.add(layer.Name);
            }
        }
    }

    private static List<String> labels = Arrays.asList("地形图", "影像图", "矢量图", "管网图");

    public static int getImageResourceID(String label) {
        int drawable = R.drawable.dx_1;
        if (labels.contains(label)) {
            switch (label) {
                case "地形图":
                    drawable = R.drawable.dx_1;
                    break;
                case "影像图":
                    drawable = R.drawable.dx_4;
                    break;
                case "矢量图":
                    drawable = R.drawable.dx_2;
                    break;
                case "管网图":
                    drawable = R.drawable.dx_3;
                    break;
            }
            return drawable;
        }
        return drawable;
    }

    public static Node initOnlineLayerTree(MapView mapView,Boolean isScope) {
        //Node appTree = MyApplication.getInstance().getConfigValue("appTree", Node.class);
        Node appTree;
        //if (appTree == null) {
            appTree = new Node(MyApplication.getInstance().getString(R.string.app_name), "000000");
            appTree.setChecked(false);

            Node treeRoot = new Node(mapView.getMap().getName(), "-1");
            treeRoot.setParent(appTree);
            treeRoot.setChecked(true);
            appTree.add(treeRoot);
            OnlineLayerManage(MapServiceInfo.getInstance().getLayers(), treeRoot,isScope);

            MyApplication.getInstance().putConfigValue("appTree", appTree);
        //}
        return appTree;
    }


    public static Node initOfflineLayerTree(MapView mapView,Boolean isScope) {

        //Node appTree = MyApplication.getInstance().getConfigValue("appTree", Node.class);
        Node appTree;
        //if (appTree == null) {
            appTree = new Node(MyApplication.getInstance().getString(R.string.app_name), "000000");
            appTree.setCheckBox(false);

            Node treeRoot = new Node(mapView.getMap().getName(), "-1");
            treeRoot.setParent(appTree);
            treeRoot.setChecked(true);
            appTree.add(treeRoot);

            com.zondy.mapgis.map.Map map = mapView.getMap();
            for (int i = 0; i < map.getLayerCount(); i++) {
                recursionInitTree(map.getLayer(i), treeRoot,isScope);
            }

            MyApplication.getInstance().putConfigValue("appTree", appTree);
        //}
        return appTree;

    }




    //控制离线管网
    public static void reShowMapForOffline(final List<String> checkedNodeNames) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                LayerEnum layerEnum = mapView.getMap().getLayerEnum();

                layerEnum.moveToFirst();

                MapLayer mapLayer;
                List<String> dtLayerNames = getDTLayerNames();

                while ((mapLayer = layerEnum.next()) != null) {
                    String layerName = mapLayer.getName();
                    //不对底图进行控制
                    if (dtLayerNames != null && dtLayerNames.contains(layerName)) {
                        continue;
                    }

                    if (checkedNodeNames.contains(layerName)) {
                        mapLayer.setVisible(true);
                    } else {
                        mapLayer.setVisible(false);
                    }

                }

                mapView.refresh();

                return false;
            }
        });
    }

    //控制在线管网
    public static void reShowMapForOnline(MapView mapView, List<String> checkedNodeNames) {

        if (checkedNodeNames == null) {
            return;
        }

        OnlineLayerInfo[] allOnlineLayerInfos = MapServiceInfo.getInstance().getLayers();
        if (allOnlineLayerInfos == null) {
            return;
        }

        boolean hasChecked = false;
        for (OnlineLayerInfo onlineLayerInfo : allOnlineLayerInfos) {
            onlineLayerInfo.defaultVisibility = false;
            if (!checkedNodeNames.contains(onlineLayerInfo.name)) {
                continue;
            }
            onlineLayerInfo.defaultVisibility = true;
            hasChecked = true;
        }

        int dynamicMapIndex = MobileConfig.MapConfigInstance.getMapTypeIndex(MapConfig.MOBILE_DYNAMIC);
        if (dynamicMapIndex == -1) {
            return;
        }
        if (mapView.getMap().getLayerCount() <= dynamicMapIndex) {
            return;
        }
        MapLayer dynamicMapLayer = mapView.getMap().getLayer(dynamicMapIndex);
        if (!(dynamicMapLayer instanceof ServerLayer)) {
            return;
        }
        ServerLayer dynamicServereLayer = (ServerLayer) dynamicMapLayer;
        dynamicServereLayer.setVisible(true);
        if (!hasChecked) {
            dynamicServereLayer.setVisible(false);
            mapView.refresh();
            return;
        }

        MmtDynamicMapServer mmtDynamicMapServer = MyApplication.getInstance().getConfigValue("MmtDynamicMapServer", MmtDynamicMapServer.class);
        if (mmtDynamicMapServer == null) {
            return;
        }
        mmtDynamicMapServer.getInfo().setLayers(allOnlineLayerInfos);

        dynamicServereLayer.clearCache();
        mapView.refresh();
    }


    public static void recursionInitTree(MapLayer mapLayer, Node treeRoot,Boolean isScope) {

        //新版排除底图，新版底图和管网分开展示和控制
        String layerName = mapLayer.getName();
        List<String> dtLayerNames = getDTLayerNames();
        if (dtLayerNames != null && dtLayerNames.contains(layerName)) {
            return;
        }
        if(!mapLayer.getIsVisible()&&isScope){
            return;
        }
        if(mapLayer instanceof ServerLayer){
            return;
        }
        if (mapLayer instanceof VectorLayer) {
            VectorLayer vectorLayer = (VectorLayer) mapLayer;
            Node leafNode = new Node(layerName, "" + count++);
            leafNode.setParent(treeRoot);
            leafNode.setChecked(vectorLayer.getIsVisible());
            treeRoot.add(leafNode);
        }
        if (mapLayer instanceof GroupLayer) {
            GroupLayer groupLayer = (GroupLayer) mapLayer;
            Node groupLayerNode = new Node(layerName, "" + count++);
            groupLayerNode.setParent(treeRoot);
            groupLayerNode.setChecked(groupLayer.getIsVisible());
            treeRoot.add(groupLayerNode);

            for (int j = 0; j < groupLayer.getCount(); j++) {
                recursionInitTree(groupLayer.item(j), groupLayerNode,isScope);
            }
        }
    }

    public static void OnlineLayerManage(OnlineLayerInfo[] onlineLayerInfos, Node treeRoot,Boolean isScope) {
        if (onlineLayerInfos == null) {
            return;
        }

        List<Node> leafNodes = new ArrayList<>();
        for (OnlineLayerInfo onlineLayerInfo : onlineLayerInfos) {

            boolean isChecked = onlineLayerInfo.defaultVisibility;
            int parentLayerId = onlineLayerInfo.parentLayerId;
            String curLayerId = onlineLayerInfo.id;
            boolean isLeaf = parentLayerId == -1;
            Node node = new Node(onlineLayerInfo.name, curLayerId);
            node.setChecked(isChecked);

            if (isChecked==false&&isScope==true){
                continue;
            }
            if (isLeaf) {
                node.setParent(treeRoot);
                treeRoot.add(node);

                leafNodes.add(node);

                continue;
            }

            for (Node nodeItem : leafNodes) {
                if (!String.valueOf(parentLayerId).equals(nodeItem.getValue())) {
                    continue;
                }
                node.setParent(nodeItem);
                nodeItem.add(node);
            }

        }
    }
}
