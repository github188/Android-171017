package com.mapgis.mmt.module.gis.toolbar.online.query;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.zondy.mapgis.geometry.Rect;

public abstract class OnlineWhereQueryTask extends AsyncTask<String, String, String> {

    protected ProgressDialog loadDialog;

    protected final MapGISFrame mapGISFrame;
    protected final String layerName;

    private final String ExportFlag = "0";
    private final String where;
    private final String geometry;
    private final String geometryType = "Envelope";
    private final String f = "json";
    private String paging = "all";
    private final String returnGeometry = "true";

    public OnlineWhereQueryTask(MapGISFrame mapGISFrame, String layerName, String where) {
        this.layerName = layerName;
        this.mapGISFrame = mapGISFrame;
        this.where = where;

        Rect rect = mapGISFrame.getMapView().getMap().getEntireRange();

        this.geometry = "{\"xmin\":" + rect.getXMin() + ",\"xmax\":" + rect.getXMax() + ",\"ymin\":" + rect.getYMin()
                + ",\"ymax\":" + rect.getYMax() + ",\"spatialReference\":{\"wkid\":1}}";

        Log.d("Ace", geometry.toString());
    }

    /**
     * 重载的构造函数，实现分页查询
     *
     * @param mapGISFrame 地图界面
     * @param layerName   图层名称
     * @param where       查询的条件
     * @param pageBlock   分页查询的索引，在URL中需将paging参数拼成“mid:1,10”形式，表示查询区间
     */
    public OnlineWhereQueryTask(MapGISFrame mapGISFrame, String layerName, String where, String pageBlock) {
        this(mapGISFrame, layerName, where);
        paging = pageBlock;
    }

    @Override
    protected void onPreExecute() {
        loadDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, " 正在查询信息");
        loadDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String layerId = MapServiceInfo.getInstance().getLayerByName(layerName).id;

        String result = NetUtil.executeHttpGetAppointLastTime(180, OnlineQueryService.getOnlineQueryService(layerId), "ExportFlag", ExportFlag,
                "where", where, "geometry", geometry, "geometryType", geometryType, "f", f, "paging", paging, "returnGeometry",
                returnGeometry);

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (result == null || result.length() <= 2) {
                mapGISFrame.showToast("未查询到信息");
                return;
            }

            result = result.replace("\\", "");

            OnlineQueryResult data = new Gson().fromJson(result, OnlineQueryResult.class);

            if (data.features.length == 0) {
                mapGISFrame.showToast("未查询到信息!");
            } else {
                onTaskDone(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
            MyApplication.getInstance().showMessageWithHandle(e.getMessage());
        } finally {
            loadDialog.cancel();
        }
    }

    protected abstract void onTaskDone(OnlineQueryResult data);

}
