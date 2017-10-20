package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/13.
 */
public class AuxTablesInfo {

    /// <summary>
    /// 是否成功
    /// </summary>
    public boolean isSuccess;

    /// <summary>
    /// 操作过程返回的错误信息
    /// </summary>
    public String Msg;

    /// <summary>
    /// 设备与中间表挂接关系的列表
    /// </summary>
    public List<String> Names = new ArrayList<>();

    /// <summary>
    /// 设备与中间表挂接关系的列表
    /// </summary>
    public List<GisReferenceField> ReferenceFields = new ArrayList<>();

    public void setAuxTablesInfo(AuxTablesInfo auxTablesInfo) {
        this.isSuccess = auxTablesInfo.isSuccess;
        this.Msg = auxTablesInfo.Msg;
        this.Names.addAll(auxTablesInfo.Names);
        this.ReferenceFields.addAll(auxTablesInfo.ReferenceFields);
    }

    public void getAuxTablesInfoFromGisServer(Context context, final String LayerId, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/zondy_mapgiscitysvr_auxdata/rest/auxdatarest.svc/" + MobileConfig.MapConfigInstance.VectorService + "/auxdataserver/" + LayerId + "/GetAuxDataTablesInfo");
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                AuxTablesInfo temp = new Gson().fromJson(s, AuxTablesInfo.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                setAuxTablesInfo(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }


}
