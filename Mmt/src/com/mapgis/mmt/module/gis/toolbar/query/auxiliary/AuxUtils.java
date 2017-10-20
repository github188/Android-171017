package com.mapgis.mmt.module.gis.toolbar.query.auxiliary;


import android.content.Context;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxDataResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.Aux_Tables;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.Aux_Tables2;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.ConditionQueryAdapterData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.Feature;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.FindResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.Aux_TablesInterface;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxDic;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.GetAuxDataResult;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/4/13.
 */
public class AuxUtils {

    private static String webgisFlag = MyApplication.getInstance().getConfigValue("webgisVersion");

    //webgis默认为3.0版本
    public static boolean isWebgis3() {

        if (TextUtils.isEmpty(webgisFlag)) {
            return true;
        }
        return webgisFlag.contains("3");
    }

    public static void openAuxQuery(Context context, final AfterOnsucess afterOnsucess) {
        if (TextUtils.isEmpty(webgisFlag)) {
            new MmtBaseTask<Void, Void, Void>(context) {
                @Override
                protected Void doInBackground(Void... params) {
                    webgisFlag = "3.0";
                    // 只有webgis3.0才有此服务
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/rest/services/AuxDataServer.svc/" + MobileConfig.MapConfigInstance.VectorService + "/Layers";
                    if (!NetUtil.testServiceExist(url)) {
                        webgisFlag = "2.0";
                    }
                    if (afterOnsucess != null) {
                        afterOnsucess.afterSucess();
                    }
                    return null;
                }
            }.mmtExecute();
        } else {
            if (afterOnsucess != null) {
                afterOnsucess.afterSucess();
            }
        }
    }

    // http://192.168.12.127:8088/CityInterface/services/zondy_mapgiscitysvr_auxdata/rest/AuxDataREST.svc/flexweb/AuxDataServer/Layers
    //只针对条件查询
    //webgis版本3.0和非3.0的区分
    public static String getBaseUrl() {
        if (!isWebgis3()) {
            return ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/services/zondy_mapgiscitysvr_auxdata/rest/auxdatarest.svc/" + MobileConfig.MapConfigInstance.VectorService + "/AuxDataServer/";
        }
        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/rest/services/AuxDataServer.svc/" + MobileConfig.MapConfigInstance.VectorService + "/";
    }

    public static Aux_TablesInterface getAux_Tables() {
        if (isWebgis3()) {
            return new Aux_Tables();
        } else {
            return new Aux_Tables2();
        }
    }

    public interface AfterOnsucess {
        void afterSucess();
    }

    public interface RetrurnBefore {
        void retrurnBefore();
    }

    public static List<AuxData> getAuxDataListFromGetAuxDataResult(List<GetAuxDataResult> getAuxDataResults) {
        List<AuxData> auxDataLists = new ArrayList<>();
        for (GetAuxDataResult getAuxDataResult : getAuxDataResults) {
            for (List<AuxDic> hm : getAuxDataResult.attributes) {
                AuxData auxData = new AuxData(getAuxDataResult.displayFieldList, hm, getAuxDataResult.tableName);
                auxDataLists.add(auxData);
            }
        }
        return auxDataLists;
    }

    public static List<AuxData> findAuxDataListByType(List<AuxData> bakAllDataLists, String type) {
        if (type.equals("全部")) {
            return bakAllDataLists;
        }
        List<AuxData> auxDatas = new ArrayList<>();
        for (AuxData auxData : bakAllDataLists) {
            if (type.equals(auxData.name)) {
                auxDatas.add(auxData);
            }
        }
        return auxDatas;
    }

    public static HashMap<String, String> dic2HM(List<AuxDic> dics) {
        HashMap<String, String> hm = new HashMap<>();
        for (AuxDic dic : dics) {
            hm.put(dic.Key, dic.Value);
        }
        return hm;
    }

//    public static void auxDataResult2AdapterDataList(List<ConditionQueryAdapterData> dataList, AuxDataResult auxDataResult, String auxTableName,String layerID) {
//        if (dataList == null) {
//            return;
//        }
//        if (auxDataResult == null) {
//            return;
//        }
//        if (auxDataResult.Result == null) {
//            return;
//        }
//        for (FindResult findResult : auxDataResult.Result) {
//            if (findResult.features == null) {
//                continue;
//            }
//            for (Feature feature : findResult.features) {
//                if (feature.attributes == null) {
//                    continue;
//                }
//                //服务总是返回pagesize个，需要通过OID过滤掉没用的
//                String OID = feature.attributes.get("OID");
//                if (TextUtils.isEmpty(OID) || "0".equals(OID)) {
//                    continue;
//                }
//                dataList.add(new ConditionQueryAdapterData(layerID, auxTableName, feature.attributes, null, null));
//            }
//        }
//    }

    public static void auxDataResult2AdapterDataList(List<ConditionQueryAdapterData> dataList, AuxDataResult auxDataResult, String auxTableName,Map<String, String> OIDLayerID) {
        if (dataList == null) {
            return;
        }
        if (auxDataResult == null) {
            return;
        }
        if (auxDataResult.Result == null) {
            return;
        }
        for (FindResult findResult : auxDataResult.Result) {
            if (findResult.features == null) {
                continue;
            }
            for (Feature feature : findResult.features) {
                if (feature.attributes == null) {
                    continue;
                }
                //服务总是返回pagesize个，需要通过OID过滤掉没用的
                String OID = feature.attributes.get("OID");
                if (TextUtils.isEmpty(OID) || "0".equals(OID)) {
                    continue;
                }
                dataList.add(new ConditionQueryAdapterData(OIDLayerID.get(OID), auxTableName, feature.attributes, null, null));
            }
        }
    }
}
