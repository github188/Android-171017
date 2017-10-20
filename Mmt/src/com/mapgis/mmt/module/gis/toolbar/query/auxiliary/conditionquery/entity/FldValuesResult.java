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
 * Created by liuyunfan on 2016/4/21.
 */
public class FldValuesResult {

    // {"isSuccess":false,"Msg":"","rtnlist":null}
    public boolean isSuccess;

    /// <summary>
    /// 操作过程返回的错误信息
    /// </summary>
    public String Msg;

    /// <summary>
    /// 结果集合
    /// </summary>
    public List<String> rtnlist = new ArrayList<>();

    public void setFldValuesResult(FldValuesResult fldValuesResult) {
        this.isSuccess = fldValuesResult.isSuccess;
        this.Msg = fldValuesResult.Msg;
        this.rtnlist.clear();
        this.rtnlist.addAll(fldValuesResult.rtnlist);
    }

    //http://192.168.12.7/cityinterface/rest/services/AuxDataServer.svc/zsgw/6/GetAuxDataFldValues?layerIds=6&strAuxTableName=%E7%94%A8%E6%88%B7%E4%BF%A1%E6%81%AF%E8%A1%A8&%5Fts=1461206464222&where=&fldName=%E5%A4%84%E7%90%86%E6%97%A5%E6%9C%9F&returnGeometry=false
    public void getFldValuesResultFromServer(Context context, final String Envelope,final String layerID, final String auxTabName, final String fldName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true,"正在获取"+fldName+"枚举值") {
            @Override
            protected String doInBackground(Void... params) {
//                List<String> paramList=new ArrayList<String>();
//                if(!AuxUtils.isWebgis3()){
//                    paramList.add("geometryType");
//                    paramList.add(GeomeFilterTypeEnum.Envelope.toString());
//                    paramList.add("geometry");
//                    paramList.add(Envelope);
//                }
//                paramList.add("layerIds");
//                paramList.add(layerID);
//
//                paramList.add("strAuxTableName");
//                paramList.add(auxTabName);

               //默认查找当前范围的枚举值
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl()+ layerID + "/GetAuxDataFldValues","geometryType",  GeomeFilterTypeEnum.Envelope.toString(),"geometry",Envelope,"layerIds", layerID, "strAuxTableName", auxTabName, "fldName", fldName, "returnGeometry", "false");
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, fldName + "枚举值查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                FldValuesResult temp = new Gson().fromJson(s, FldValuesResult.class);
                if (temp == null) {
                    Toast.makeText(context, fldName + "解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.isSuccess) {
                    Toast.makeText(context, TextUtils.isEmpty(temp.Msg) ? "未查询到"+fldName+"枚举值" : temp.Msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                setFldValuesResult(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();
    }
}
