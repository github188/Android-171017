package com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module;

import android.content.Context;

import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;

import java.util.List;

/**
 * Created by liuyunfan on 2016/4/26.
 * 目的是区分webgis3.0和2.0 的Aux_Tables(附属数据表)
 */
public interface Aux_TablesInterface {
    // void setAux_Tables(Aux_TablesInterface aux_tables);

    String getAux_Table(int index);
    List<String> getAux_TableList();
    void getAux_TablesFromGisServer(Context context, final String LayerId, final AuxUtils.AfterOnsucess listener);
}
