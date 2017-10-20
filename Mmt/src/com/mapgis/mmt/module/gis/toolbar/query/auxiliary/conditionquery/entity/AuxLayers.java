package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/15.
 */
public class AuxLayers {
    public boolean isSuccess;
    public String Msg;
    public List<String> layerIds = new ArrayList<>();

    public void setAuxLayers(AuxLayers auxLayers) {
        this.isSuccess = auxLayers.isSuccess;
        this.Msg = auxLayers.Msg;
        this.layerIds.clear();
        this.layerIds.addAll(auxLayers.layerIds);

    }

    //http://192.168.12.6:8090/cityinterface/rest/services/AuxDataServer.svc/qdzhgw/Layers
    public void getAuxLayersFromGisServer(Context context, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl()+ "Layers");
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuxLayers temp = new Gson().fromJson(s, AuxLayers.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.isSuccess) {
                    Toast.makeText(context, temp.Msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                setAuxLayers(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }
}
