package com.mapgis.mmt.module.gis.toolbar.query.spatial;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

/**
 * 高级查询 自定义拼接查询条件
 *
 * @author Administrator
 */
public class WhereQueryMapMenu extends SpatialQueryMapMenu {

    /**
     * 查询的图层名称
     */
    private String selectedLayerName;

    public WhereQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        Intent intent = new Intent(mapGISFrame, LayerSelectActivity.class);

        intent.putExtra("title", "高级查询");
        intent.putStringArrayListExtra("layers", GisQueryUtil.getVisibleVectorLayerNames(mapView));

        // 打开选择图层界面， 通过setResult 返回 选择的图层名称
        mapGISFrame.startActivityForResult(intent, 0);

        mapView.setTapListener(null);

        return true;
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent data) {
        try {
            boolean isLocate = false;
            switch (resultCode) {
                case Activity.RESULT_CANCELED:
                    mapGISFrame.findViewById(R.id.mapviewClear).performClick();
                    break;
                case ResultCode.RESULT_LAYER_SELECTED:
                    if (!findLayerByName(data)) {
                        MyApplication.getInstance().showMessageWithHandle("地形图无属性可查询");
                        return true;
                    }
                    this.selectedLayerName = data.getStringExtra("layer");
                    Intent intent = new Intent(mapGISFrame, WhereQueryActivity.class);
                    intent.putExtra("layer", layer);
                    mapGISFrame.startActivityForResult(intent, 0);
                    break;
                case ResultCode.RESULT_WHERE_FETCHED:
                    this.layer = (MapLayer) data.getSerializableExtra("layer");
                    this.where = data.getStringExtra("where");

                    this.rect = null;

                    ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText("选择图层: " + layer.getName());

                    if (selectedLayerName != null && selectedLayerName.length() > 0) {
                        LayerEnum layerEnum = mapView.getMap().getLayerEnum();
                        layerEnum.moveToFirst();
                        MapLayer mapLayer;
                        while ((mapLayer = layerEnum.next()) != null) {
                            if (!(mapLayer instanceof VectorLayer) || !mapLayer.getName().equals(selectedLayerName)) {
                                continue;
                            } else {
                                layer = mapLayer;
                                break;
                            }
                        }
                    }
                    super.task.executeOnExecutor(MyApplication.executorService, "");
                    break;
                case ResultCode.RESULT_PIPE_LOCATE:
                    isLocate = true;
                case ResultCode.RESULT_PIPE_REFREASH:
                    int pageNum = data.getIntExtra("page", 1);
                    int clickedIndex = data.getIntExtra("clickWhichIndex", -1);
                    if (currentPage != pageNum || clickedIndex != listener.clickWhichIndex) {
                        currentPage = data.getIntExtra("page", 1);
                        listener.clickWhichIndex = data.getIntExtra("clickWhichIndex", -1);
                        showPageResultOnMap(isLocate);
                    }
                    break;
                default:
                    return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }
}
