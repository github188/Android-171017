package com.repair.shaoxin.water.highrisesearch;

import java.util.Arrays;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.patrolproduct.module.projectquery.queryutil.QueryLayerUtil;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

/**
 * 通过表卡号获取需要关闭的阀门
 */
public class GetCloseValveInfoTask extends AsyncTask<String, Void, String> {
//    private final SherlockActivity activity;
    private final Context context;

    private ProgressDialog loadingDialog;

    public GetCloseValveInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        loadingDialog = MmtProgressDialog.getLoadingProgressDialog(context, "正在查询用户阀门");
        loadingDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_HighFloor_SX/REST/HighFloorREST.svc/GetCloseValveInfo?svrName="
                + MobileConfig.MapConfigInstance.VectorService + "&meterNo=" + params[0];

        try {
//            HttpClient httpClient = new DefaultHttpClient();
//            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
//            HttpGet doGet = new HttpGet(uri);
//            HttpResponse response = httpClient.execute(doGet);
//
//             = EntityUtils.toString(response.getEntity(), "utf-8");

            String result = NetUtil.executeHttpGet(uri);

            if (result == null) {
                return null;
            }

            if (result.contains("\"")) {
                result = result.replace("\"", "");
            }

            HighRiseCloseValveConstant.queryValve.clear();

            if (result.contains("|")) {
                HighRiseCloseValveConstant.queryValve.addAll(Arrays.asList(result.split("\\|")));
            } else {
                HighRiseCloseValveConstant.queryValve.add(result);
            }

            HighRiseCloseValveConstant.VALVE_GRAPHICS.clear();

            for (String valves : HighRiseCloseValveConstant.queryValve) {

                // 一个图层下的查询条件
                String oneWhere = "";

                // 该图层的名称
                String layerName = valves.split(":")[0];
                // 该图层包含的设备编号
                String nums = valves.split(":")[1];

                String pipeNo = "";

                // 包含多个编号
                if (nums.contains(",")) {
                    String[] numArr = nums.split(",");
                    for (String n : numArr) {
                        pipeNo = pipeNo + "" + n + "" + ",";
                    }
                    pipeNo = pipeNo.substring(0, pipeNo.length() - 1);
                } else {// 只有一个编号
                    pipeNo = "" + nums + "";
                }

                oneWhere = "编号" + " in (" + pipeNo + ")";

                MapLayer layer = new QueryLayerUtil(context,MyApplication.getInstance().mapGISFrame).findLayerByName(layerName);

                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, oneWhere,null
                            /*rect == null ? null : new FeatureQuery.QueryBound(rect)*/, FeatureQuery.SPATIAL_REL_OVERLAP, true,
                        true, "", 10);

                HighRiseCloseValveConstant.VALVE_GRAPHICS.addAll(HighRiseCloseValveConstant.exchangeToGraphics(featurePagedResult));
//                String strJson = ((VectorLayer) layer).GetFeature("", "", "", oneWhere, "", true, 0, 30, "json");
//
//                if (strJson != null && strJson.length() > 0) {
//                    ESFeatureSet featureSet = ESFeatureSet.fromJson(strJson, layerName);
//                    Graphic[] graphics = featureSet.getGraphics();
//
//                    HighRiseCloseValveConstant.VALVE_GRAPHICS.addAll(Arrays.asList(graphics));
//                }
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {

        try {

            // new
            // GetStopUserInfoTask(SessionManager.MainActivity).execute(HighRiseCloseValveConstant.queryValve);

            if (result == null) {
                Toast.makeText(context, "未查询到结果...", Toast.LENGTH_SHORT).show();
            } else {
                HighRiseCloseValveConstant.showQueryValve();
                HighRiseCloseValveConstant.showOnMap();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadingDialog.cancel();
        }

    }
}
