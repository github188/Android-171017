package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.Aux_TablesInterface;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxDic;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/15.
 */
public class Aux_Tables implements Aux_TablesInterface {

    public boolean Success;
    public List<AuxDic> List = new ArrayList<>();

    @Override
    public String getAux_Table(int index) {
        if (List.size() > index) {
            return List.get(index).Value;
        }
        return "";
    }

    @Override
    public List<String> getAux_TableList() {
        List<String> auxList = new ArrayList<String>();
        for (AuxDic auxDic : List) {
            auxList.add(auxDic.Value);
        }
        return auxList;
    }

    //webgis3.0 版本赋值
    public void setAux_Tables(Aux_Tables aux_tables) {
        this.Success = aux_tables.Success;
        this.List.clear();
        this.List.addAll(aux_tables.List);
    }

//    //将2.0版本桥接为3.0版本
//    public void setAux_Tables(Aux_Tables2 aux_tables) {
//        this.Success = aux_tables.isSuccess;
//        this.List.clear();
//        if (aux_tables.Names != null)
//            for (String name : aux_tables.Names) {
//                this.List.add(new AuxDic(name, name));
//            }
//
//    }

    public void getAux_TablesFromGisServer(Context context, final String LayerId, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                String url = AuxUtils.getBaseUrl() + LayerId;
//                if (!AuxUtils.isWebgis3()) {
//                    url += "/GetAuxDataTablesInfo";
//                }
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据表查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                Aux_Tables temp = new Gson().fromJson(s, Aux_Tables.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据表解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.Success) {
                    Toast.makeText(context, "附属数据表查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                setAux_Tables(temp);

//                if (AuxUtils.isWebgis3()) {
//                    Aux_Tables temp = new Gson().fromJson(s, Aux_Tables.class);
//                    if (temp == null) {
//                        Toast.makeText(context, "附属数据解析错误", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (!temp.Success) {
//                        Toast.makeText(context, "附属数据中间表查询错误", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    setAux_Tables(temp);
//                } else {
//                    Aux_Tables2 temp = new Gson().fromJson(s, Aux_Tables2.class);
//                    if (temp == null) {
//                        Toast.makeText(context, "附属数据解析错误", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (!temp.isSuccess) {
//                        Toast.makeText(context, "附属数据中间表查询错误", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    setAux_Tables(temp);
//                }
                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }
}
