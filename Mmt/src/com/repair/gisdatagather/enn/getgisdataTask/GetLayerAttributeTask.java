package com.repair.gisdatagather.enn.getgisdataTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryService;
import com.repair.gisdatagather.enn.bean.GISDataBean;
import com.repair.gisdatagather.enn.editdata.EditDataActivity;
import com.repair.zhoushan.entity.FlowCenterData;

/**
 * Created by liuyunfan on 2015/12/16.
 * 根据图层id获取该图层属性列表
 */
public class GetLayerAttributeTask extends AsyncTask<String, String, String> {
    private ProgressDialog loadingDialog;
    private MapGISFrame mapGISFrame;
    FlowCenterData flowCenterData;
    GISDataBean gisDataBean;

    public GetLayerAttributeTask(MapGISFrame mapGISFrame, FlowCenterData flowCenterData, GISDataBean gisDataBean) {
        this.mapGISFrame = mapGISFrame;
        this.flowCenterData = flowCenterData;
        this.gisDataBean = gisDataBean;
    }

    @Override
    protected void onPreExecute() {
        loadingDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, "加载中...");
        loadingDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        // OnlineLayerInfo onlineLayer = MapServiceInfo.getInstance().getLayerByName(params[0]);
        String url = OnlineQueryService.getLayerAttributeService(params[0]);
        return NetUtil.executeHttpGet(url, "f", "json");
    }

    @Override
    protected void onPostExecute(String result) {

        try {
            if (result == null || result.length() == 0) {
                // mapGISFrame.showToast("δ?????GIS???!");
                return;
            }

            OnlineLayerInfo onlineLayer = new Gson().fromJson(result, OnlineLayerInfo.class);

            Intent intent = new Intent(mapGISFrame, EditDataActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("onlineLayer", new Gson().toJson(onlineLayer));
            bundle.putParcelable("FlowCenterData", flowCenterData);
            // bundle.putBoolean("isEdit",false);
            bundle.putInt("isEdit", 0);
            bundle.putString("gisDataBean", new Gson().toJson(gisDataBean));
            intent.putExtra("bundle", bundle);
            mapGISFrame.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadingDialog.cancel();
        }

    }

}
