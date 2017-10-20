package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.List;

//{"Success":true
// ,"Result":
// [{"layerId":15,"layerName":"水表","displayFieldName":null,"fieldAliases":{"LayerID":"LayerID","OID":"OID","编号":"编号","电话":"电话"},"fields":[{"name":"LayerID","type":"civFieldTypeString","alias":"LayerID","nullable":false,"editable":false,"domain":null,"visible":false},{"name":"OID","type":"civFieldTypeString","alias":"OID","nullable":false,"editable":false,"domain":null,"visible":false},{"name":"编号","type":"civFieldTypeString","alias":"编号","nullable":false,"editable":false,"domain":null,"visible":true},{"name":"电话","type":"civFieldTypeString","alias":"电话","nullable":false,"editable":false,"domain":null,"visible":true}],"relationships":null,"hasM":false,"hasZ":false,"spatialReference":null,"geometryType":null,"features":[{"attributes":{"LayerID":"0","OID":"1","编号":"SB00002","电话":"13814143120"},"geometry":null,"symbol":null}]}]}

/**
 * Created by liuyunfan on 2016/4/14.
 * 最终需要的附属数据结构
 *
 * 虽然webgis2.0和3.0的AuxDataResult数据结构不同，但手持需要的信息该AuxDataResult 就可以表示了
 */
public class AuxDataResult {
    /// <summary>
    /// 操作是否成功
    /// </summary>
    public boolean Success;


    /// <summary>
    /// 结果集
    /// </summary>
    public List<FindResult> Result;

    public void setAuxDataResult(AuxDataResult auxDataResult) {
        this.Success = auxDataResult.Success;
        this.Result = auxDataResult.Result;
    }


//    http://192.168.12.7/cityinterface/rest/services/AuxDataServer.svc/zsgw/6/DevQueryByAuxInfo?strOID=146531&bRecord=0&auxTableName=%E7%94%A8%E6%88%B7%E4%BF%A1%E6%81%AF%E8%A1%A8&%5Fts=1461121471338
//    strOID:146531
//    bRecord:0
//    auxTableName:用户信息表

    /**
     * 通过单个oid获取附属数据信息（含坐标）
     * 通过附属数据反查设备
     *
     * @param strOID
     * @param bRecord
     * @param auxTableName
     */
    public void getSingleAuxDataResult(Context context, final String layerID, final String strOID, final String bRecord, final String auxTableName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl() + layerID + "/DevQueryByAuxInfo", "strOID", strOID, "bRecord", bRecord, "auxTableName", auxTableName);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据坐标查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                //webgis2.0 需要替换掉\
                s = s.replace("\\", "");
                AuxDataResult temp = new Gson().fromJson(s, AuxDataResult.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据坐标解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.Success) {
                    Toast.makeText(context, "附属数据坐标查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                setAuxDataResult(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();
    }


    public void getAuxDataResultFromGisServer(Context context, final List<String> OIDs, final String layerID, final String auxTableName, final int pageNum, final int pageSize, final AuxUtils.AfterOnsucess listener, final AuxUtils.RetrurnBefore retrurnBefore) {
        if (OIDs == null) {
            MyApplication.getInstance().showMessageWithHandle("无附属数据");
            if (retrurnBefore != null) {
                retrurnBefore.retrurnBefore();
            }
            return;
        }
        if (OIDs.size() == 0) {

            MyApplication.getInstance().showMessageWithHandle("无附属数据");
            if (retrurnBefore != null) {
                retrurnBefore.retrurnBefore();
            }
            return;
        }
        //规定每次取5条记录
        int sum = OIDs.size();
        int start = pageNum * pageSize;
        int end = (pageNum + 1) * pageSize;
        if (start >= sum) {
            MyApplication.getInstance().showMessageWithHandle("无更多数据");
            if (retrurnBefore != null) {
                retrurnBefore.retrurnBefore();
            }
            return;
        }
        if (end > sum) {
            end = sum;
        }
        final String[] oids = OIDs.subList(start, end).toArray(new String[end - start]);
        //  http://192.168.12.6:8090/cityinterface/rest/services/AuxDataServer.svc/qdzhgw/15/QueryAuxAttByIDs?auxTableName=%E6%B0%B4%E8%A1%A8%E9%99%84%E5%B1%9E%E6%95%B0%E6%8D%AE2&OIDs=1&%5Fts=1460716318777

        new MmtBaseTask<Void, Void, String>(context, false) {
            @Override
            protected String doInBackground(Void... params) {
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl() + layerID + "/QueryAuxAttByIDs", "auxTableName", auxTableName, "OIDs", TextUtils.join(",", oids));

            }

            @Override
            protected void onSuccess(String s) {

                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据查询错误", Toast.LENGTH_SHORT).show();
                    if (retrurnBefore != null) {
                        retrurnBefore.retrurnBefore();
                    }
                    return;
                }
                //webgis2.0 需要替换掉\
                s = s.replace("\\", "");
                AuxDataResult temp = new Gson().fromJson(s, AuxDataResult.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据解析错误", Toast.LENGTH_SHORT).show();
                    if (retrurnBefore != null) {
                        retrurnBefore.retrurnBefore();
                    }
                    return;
                }
                if (!temp.Success) {
                    Toast.makeText(context, "附属数据查询错误", Toast.LENGTH_SHORT).show();
                    if (retrurnBefore != null) {
                        retrurnBefore.retrurnBefore();
                    }
                    return;
                }
                setAuxDataResult(temp);
                if (listener != null) {
                    listener.afterSucess();
                }
                if (retrurnBefore != null) {
                    retrurnBefore.retrurnBefore();
                }
            }
        }.mmtExecute();

    }
}
