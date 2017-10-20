package com.repair.auxiliary.conditionquery.changsha;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxLayers;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxTblAttStru;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.MapGISField;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.Aux_TablesInterface;
import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/5/20.
 */
public class SC_ConditionChoseActivity extends BaseActivity {
    Aux_TablesInterface aux_tables = AuxUtils.getAux_Tables();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CS_InfoQueryFragment fragment = new CS_InfoQueryFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);
        ft.commit();
        getBaseTextView().setText("用户查询");

        //取图层
        auxLayers.getAuxLayersFromGisServer(SC_ConditionChoseActivity.this, new AuxUtils.AfterOnsucess() {
            @Override
            public void afterSucess() {
                for (String layerID : auxLayers.layerIds) {
                    OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerById(Integer.valueOf(layerID));
                    if (onlineLayerInfo == null) {
                        continue;
                    }
                    layers.add(onlineLayerInfo.name);
                }
                if (layers.size() == 0) {
                    showErrorMsg("未找到附属数据关联的图层");
                    return;
                }

                //取附属数据表
                //长沙只有一个附属数据表，分布在多个图层，用任意图层id获取的都一样
                aux_tables.getAux_TablesFromGisServer(SC_ConditionChoseActivity.this, auxLayers.layerIds.get(0), new AuxUtils.AfterOnsucess() {
                    @Override
                    public void afterSucess() {

                        if (aux_tables.getAux_TableList().size() == 0) {
                            showErrorMsg("未找到附属数据表");
                            return;
                        }
                        //取查询条件字段名列表
                        auxTblAttStru.getAuxTblAttStruFromGisServer(SC_ConditionChoseActivity.this, auxLayers.layerIds.get(0), aux_tables.getAux_Table(0), new AuxUtils.AfterOnsucess() {
                            @Override
                            public void afterSucess() {
                                if(auxTblAttStru==null||auxTblAttStru.Fields.size()==0){
                                    showErrorMsg("未找到附属数据表的结构");
                                    return;
                                }
                                List<String> conditons = new ArrayList<String>();
                                for (MapGISField field : auxTblAttStru.Fields) {
                                    conditons.add(field.name);
                                }
                                fragment.setData(conditons, auxLayers.layerIds.get(0),aux_tables.getAux_Table(0),getIntent().getStringExtra("Envelope"));
                                fragment.setLayerIDs(auxLayers.layerIds);
                            }
                        });


                    }
                });
            }
        });

    }

    //可选的附属数据查询条件
    AuxTblAttStru auxTblAttStru = new AuxTblAttStru();

//    //枚举值
//    private void setauxTblAttStru(String auxTbl, String layername, AuxUtils.AfterOnsucess listener) {
//        if (TextUtils.isEmpty(auxTbl)) {
//            return;
//        }
//
//        OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layername);
//        if (onlineLayerInfo == null) {
//            return;
//        }
//
//        auxTblAttStru.getAuxTblAttStruFromGisServer(this, onlineLayerInfo.id, auxTbl, listener);
//    }


//    GetAuxDataIDsResult getAuxDataIDsResult = new GetAuxDataIDsResult();
//    private void getAuxDataOIDs(){
//        getAuxDataIDsResult.getGetAuxDataIDsResultFromGisServer(SC_ConditionChoseActivity.this, layerID, geometry, strCon, strAuxTableName, new AuxUtils.AfterOnsucess() {
//            @Override
//            public void afterSucess() {
//                if (getAuxDataIDsResult.OIDs.size() == 0) {
//                    SC_ConditionChoseActivity.this.showErrorMsg("当前条件无附属数据");
//                    return;
//                }
//                listView.setRefreshing(false);
//            }
//        });
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                adapter.lookDeatil(dataList.get(position - 1).atts);
//            }
//        });
//    }

    AuxLayers auxLayers = new AuxLayers();
    List<String> layers = new ArrayList<>();
//    private void getAuxDataLayers(){
//        auxLayers.getAuxLayersFromGisServer(this, new AuxUtils.AfterOnsucess() {
//            @Override
//            public void afterSucess() {
//                for (String layerID : auxLayers.layerIds) {
//                    OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerById(Integer.valueOf(layerID));
//                    if (onlineLayerInfo == null) {
//                        continue;
//                    }
//                    layers.add(onlineLayerInfo.name);
//                }
//                if (layers.size() == 0) {
//                    return;
//                }
//            }
//        });
//    }
}
