package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/4/14.
 */
public class GetAuxDataIDsResult {
    /// <summary>
    /// 是否成功
    /// </summary>
    public boolean isSuccess;

    /// <summary>
    /// 操作过程返回的错误信息
    /// </summary>
    public String Msg;

    /// <summary>
    /// 结果集合
    /// </summary>
    public List<String> OIDs = new ArrayList<>();

    public String tableName;

    //多个图层同时查询的时候需要事先记录附属数据oid和图层id的对应关系
    public Map<String, String> OIDLayerID = new HashMap<>();

    public void setGetAuxDataIDsResult(GetAuxDataIDsResult getAuxDataIDsResult, String layerID, boolean isSaveOIDLayerID) {

        //OIDs类似:[1,2-6,8]
        if (getAuxDataIDsResult.OIDs != null) {
            for (String oid : getAuxDataIDsResult.OIDs) {
                String[] nums;
                if ((nums = oid.split("-")).length > 1) {
                    int startNum = Integer.valueOf(nums[0]);
                    int endNum = Integer.valueOf(nums[1]);
                    while (startNum <= endNum) {
                        String oidtemp = String.valueOf(startNum);
                        this.OIDs.add(oidtemp);
                        if (isSaveOIDLayerID) {
                            OIDLayerID.put(oidtemp, layerID);
                        }
                        startNum++;
                    }
                } else {
                    this.OIDs.add(oid);
                    if (isSaveOIDLayerID) {
                        OIDLayerID.put(oid, layerID);
                    }
                }
            }
        }
    }


    public void setGetAuxDataResult(GetAuxDataIDsResult getAuxDataIDsResult) {
        this.isSuccess = getAuxDataIDsResult.isSuccess;
        this.Msg = getAuxDataIDsResult.Msg;
        this.OIDs.clear();
        this.tableName = getAuxDataIDsResult.tableName;
    }


    public void getGetAuxDataIDsResultFromGisServer(Context context, final String layerID, final String geometry, final String strCon, final String strAuxTableName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                List<String> args = new ArrayList<String>();
                args.add("layerIds");
                args.add(layerID);

                args.add("strCon");
                args.add(strCon);

                args.add("strAuxTableName");
                args.add(strAuxTableName);
//                //取oids
                return NetUtil.executeHttpGet(AuxUtils.getBaseUrl() + layerID + "/GetAuxDataIDs", args.toArray(new String[args.size()]));

            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    MyApplication.getInstance().showMessageWithHandle("取附属数据OID异常");
                    return;
                }
                GetAuxDataIDsResult temp1 = new Gson().fromJson(s, GetAuxDataIDsResult.class);
                if (temp1 == null) {
                    MyApplication.getInstance().showMessageWithHandle("附属数据OID解析错误");
                    return;
                }
                if (!temp1.isSuccess) {
                    MyApplication.getInstance().showMessageWithHandle(TextUtils.isEmpty(temp1.Msg) ? "OID查询错误" : temp1.Msg);
                    return;
                }

                setGetAuxDataResult(temp1);

                setGetAuxDataIDsResult(temp1, layerID, true);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }

    /**
     * 通过图层id循环查oids
     * @param context
     * @param layerIDs
     * @param geometry
     * @param strCon
     * @param strAuxTableName
     * @param listener
     */
    public void getGetAuxDataIDsResultFromGisServerByLayerIDS(Context context, final List<String> layerIDs, final String geometry, final String strCon, final String strAuxTableName, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, List<String>>(context, true) {
            @Override
            protected List<String> doInBackground(Void... params) {
                List<String> results = new ArrayList<String>();
                for (String layerID : layerIDs) {
                    List<String> args = new ArrayList<String>();
                    args.add("layerIds");
                    args.add(layerID);

                    args.add("strCon");
                    args.add(strCon);

                    args.add("strAuxTableName");
                    args.add(strAuxTableName);
//                //取oids
                    String result = NetUtil.executeHttpGet(AuxUtils.getBaseUrl() + layerID + "/GetAuxDataIDs", args.toArray(new String[args.size()]));
                    results.add(result);
                }

                return results;
            }

            @Override
            protected void onSuccess(List<String> results) {
                for (int i = 0; i < results.size(); i++) {
                    String result = results.get(i);
                    if (TextUtils.isEmpty(result)) {
                        MyApplication.getInstance().showMessageWithHandle("取附属数据OID出现部分异常");
                        continue;
                    }
                    GetAuxDataIDsResult temp1 = new Gson().fromJson(result, GetAuxDataIDsResult.class);
                    if (temp1 == null) {
                        MyApplication.getInstance().showMessageWithHandle("附属数据OID解析出现部分异常");
                        continue;
                    }
                    if (!temp1.isSuccess) {
                        MyApplication.getInstance().showMessageWithHandle(TextUtils.isEmpty(temp1.Msg) ? "OID查询错误" : temp1.Msg + "出现部分异常");
                        continue;
                    }
                    // 只要成功过一次就认为成功了
                    if (!GetAuxDataIDsResult.this.isSuccess) {
                        setGetAuxDataResult(temp1);
                    }

                    setGetAuxDataIDsResult(temp1, layerIDs.get(i), true);
                }
                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }
}
