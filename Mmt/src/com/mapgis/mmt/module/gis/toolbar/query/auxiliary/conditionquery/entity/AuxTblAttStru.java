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
public class AuxTblAttStru {
    public boolean Success;
    public List<MapGISField> Fields = new ArrayList<>();

    public void setConditionSelect(AuxTblAttStru auxTblAttStru) {
        this.Success = auxTblAttStru.Success;
        this.Fields.clear();
        this.Fields.addAll(auxTblAttStru.Fields);
    }

    //  {"Success":true,"Fields":[{"name":"编号","type":"civFieldTypeString","alias":"","nullable":false,"editable":false,"domain":null,"visible":true},{"name":"电话","type":"civFieldTypeString","alias":"","nullable":false,"editable":false,"domain":null,"visible":true}]}
    //http://192.168.12.6:8090/cityinterface/rest/services/AuxDataServer.svc/qdzhgw/7/GetAuxTblAttStru?_ts=1460707911487&auxTabName=%E6%B0%B4%E8%A1%A8%E9%99%84%E5%B1%9E%E6%95%B0%E6%8D%AE2
    public void getAuxTblAttStruFromGisServer(Context context, final String layerID, final String auxTabName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl()+ layerID + "/GetAuxTblAttStru", "auxTabName", auxTabName);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属表字段查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuxTblAttStru temp = new Gson().fromJson(s, AuxTblAttStru.class);
                if (temp == null) {
                    Toast.makeText(context, "附属表字段解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.Success) {
                    Toast.makeText(context,"", Toast.LENGTH_SHORT).show();
                    return;
                }
                setConditionSelect(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }
}
