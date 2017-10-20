package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.Aux_TablesInterface;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/25.
 * 非3.0的webgis返回的数据结构
 */
public class Aux_Tables2 implements Aux_TablesInterface {
    public boolean isSuccess;
    public String Msg;
    public List<String> Names = new ArrayList<>();
    public List<ReferenceFields> ReferenceFields;

    @Override
    public String getAux_Table(int index) {
        if (Names.size() > index) {
            return Names.get(index);
        }
        return "";
    }

    @Override
    public List<String> getAux_TableList() {
        return Names;
    }

    public void setAux_Tables2(Aux_Tables2 Aux_Tables2) {
        this.isSuccess = Aux_Tables2.isSuccess;
        this.Msg = Aux_Tables2.Msg;
        this.Names.clear();
        this.Names.addAll(Aux_Tables2.Names);
        this.ReferenceFields = Aux_Tables2.ReferenceFields;
    }

    @Override
    public void getAux_TablesFromGisServer(Context context, final String LayerId, final AuxUtils.AfterOnsucess listener) {
        new MmtBaseTask<Void, Void, String>(context, true) {
            @Override
            protected String doInBackground(Void... params) {
                String url = AuxUtils.getBaseUrl() + LayerId;
                // if (!AuxUtils.isWebgis3()) {
                url += "/GetAuxDataTablesInfo";
                // }
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(context, "附属数据表查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                Aux_Tables2 temp = new Gson().fromJson(s, Aux_Tables2.class);
                if (temp == null) {
                    Toast.makeText(context, "附属数据表解析错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!temp.isSuccess) {
                    Toast.makeText(context, "附属数据表查询错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                setAux_Tables2(temp);

                if (listener != null) {
                    listener.afterSucess();
                }
            }
        }.mmtExecute();

    }

    public class ReferenceFields {
        public String TableName;
        public String ReferenceField;
    }
}
