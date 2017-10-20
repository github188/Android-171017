package com.mapgis.mmt.module.gis.toolbar.accident.gas.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.accident.gas.BurstAnalysisMapMenu;
import com.zondy.mapgis.android.mapview.MapView;

import org.json.JSONObject;

public class QueryBurstTask extends AsyncTask<String, Integer, JSONObject> {
    private MapGISFrame mapGISFrame;
    private MapView mapView;
    private BurstAnalysisMapMenu menu;
    private Handler handler;

    private int mapwidth=0;
    private int mapheight=0;
    public QueryBurstTask(MapGISFrame mapGISFrame, BurstAnalysisMapMenu menu, Handler handler) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapGISFrame.getMapView();
        this.menu = menu;
        this.handler = handler;
        mapwidth=mapView.getMeasuredWidth();
        mapheight=mapView.getMeasuredHeight();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.menu.preAnalysis();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                    .append("/services/zondy_mapgiscitysvr_pipeaccident/rest/pipeaccidentrest.svc/")
                    .append(MobileConfig.MapConfigInstance.VectorService)
                    .append("/accidentserver/accidentForGas2")
                    .append("?geometry=").append(params[0])
                    .append("&f=").append("json")
                    .append("&imageDisplay=").append(mapwidth + "," + mapheight + ",96")
                    .append("&mapExtent=").append(params[1])
                    .append("&geometryType=").append("Point")
                    .append("&layers=").append("")
                    .append("&tolerance=").append("10");

            String json = NetUtil.executeHttpGet(sb.toString());

            return new JSONObject(json.replace("\\", ""));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result == null) {
                this.menu.errorAnalysis();
                Toast.makeText(mapGISFrame, "未返回数据或返回数据不正确", Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.get("errorMsg") == null || !BaseClassUtil.isNullOrEmptyString(result.get("errorMsg").toString())) {
                this.menu.errorAnalysis();
                Toast.makeText(mapGISFrame, result.get("errorMsg").toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            this.menu.parseDatas(result);


        } catch (Exception ex) {
            this.menu.errorAnalysis();
            Toast.makeText(mapGISFrame, ex.getMessage(), Toast.LENGTH_SHORT).show();

        } finally {
            this.menu.closeAnalysis();
        }
    }
}
