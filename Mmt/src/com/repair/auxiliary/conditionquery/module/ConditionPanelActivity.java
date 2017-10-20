package com.repair.auxiliary.conditionquery.module;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxLayers;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxTblAttStru;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.MapGISField;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.module.Aux_TablesInterface;
import com.repair.zhoushan.module.BaseDialogActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/14.
 */
public class ConditionPanelActivity extends BaseDialogActivity {
    Aux_TablesInterface aux_tables = AuxUtils.getAux_Tables();
    OnlineLayerInfo[] layerInfos;
    //附属条件view
    ImageButtonView keyview;
    //区域view
    ImageDotView areaview;
    //附属数据view
    ImageButtonView auxImageButtonView;
    //图层view
    ImageButtonView layerImageButtonView;
    AuxLayers auxLayers = new AuxLayers();
    //可选的附属数据查询条件
    AuxTblAttStru auxTblAttStru = new AuxTblAttStru();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layerInfos = MapServiceInfo.getInstance().getLayers();
        if (layerInfos == null || layerInfos.length == 0) {
            super.onCreate(savedInstanceState);
            return;
        }
        GDFormBean gdFormBean = GDFormBean.generateSimpleForm(
                new String[]{"DisplayName", "选择图层", "Name", "选择图层", "Type", "值选择器", "Validate", "1"},
                new String[]{"DisplayName", "附属数据", "Name", "附属数据", "Type", "值选择器", "Validate", "1"},
                //  new String[]{"DisplayName", "空间范围", "Name", "空间范围", "Type", "区域控件", "Validate", "1"},
                new String[]{"DisplayName", "附属条件", "Name", "附属条件", "Type", "值选择器", "Validate", "1"});
        getIntent().putExtra("GDFormBean", gdFormBean);
        getIntent().putExtra("Title", "查询条件");
        super.onCreate(savedInstanceState);
    }

//    @Override
//    protected void onResume() {
//        if (getIntent().hasExtra("area")) {
//            String choseArea = getIntent().getStringExtra("area");
//            areaview.setValue(choseArea);
//        }
//        super.onResume();
//    }

    @Override
    protected void onViewCreated() {
        auxLayers.getAuxLayersFromGisServer(this, new AuxUtils.AfterOnsucess() {
            @Override
            public void afterSucess() {
                final List<String> layers = new ArrayList<>();
                for (String layerID : auxLayers.layerIds) {
                    OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerById(Integer.valueOf(layerID));
                    if (onlineLayerInfo == null) {
                        continue;
                    }
                    layers.add(onlineLayerInfo.name);
                }
                if (layers.size() == 0) {
                    return;
                }
                View view = getFlowBeanFragment().findViewByName("选择图层");
                if (view instanceof ImageButtonView) {
                    layerImageButtonView = (ImageButtonView) view;
                    layerImageButtonView.setValue(layers.get(0));
                    layerImageButtonView.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListDialogFragment fragment = new ListDialogFragment("选择图层", layers);
                            fragment.show(ConditionPanelActivity.this.getSupportFragmentManager(), "");
                            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    layerImageButtonView.setValue(value);
                                    setAuxList(value);
                                }
                            });
                        }
                    });
                }
                setAuxList(layers.get(0));
            }
        });
//        areaview = (ImageDotView) getFlowBeanFragment().findViewByName("空间范围");
//
//        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                Dots dots = new Dots();
//                Rect rect = mapView.getDispRange();
//                dots.append(new Dot(rect.xMin, rect.yMin));
//                dots.append(new Dot(rect.xMax, rect.yMin));
//                dots.append(new Dot(rect.xMax, rect.yMax));
//                dots.append(new Dot(rect.xMin, rect.yMax));
//                areaview.setValue(GisUtil.getFormatAreaByDots(dots));
//                return false;
//            }
//        });
        View auxview = getFlowBeanFragment().findViewByName("附属数据");
        if (auxview instanceof ImageButtonView) {
            auxImageButtonView = (ImageButtonView) auxview;
            auxImageButtonView.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = new ListDialogFragment("附属数据", aux_tables.getAux_TableList());
                    fragment.show(ConditionPanelActivity.this.getSupportFragmentManager(), "");
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            auxImageButtonView.setValue(value);
                            //选了附属数据后，查询可选的查询字段
                            setauxTblAttStru(value, new AuxUtils.AfterOnsucess() {
                                @Override
                                public void afterSucess() {
//                                    if (keyview != null) {
//                                        if (auxTblAttStru.Fields.size() > 0) {
//                                            keyview.setKey(auxTblAttStru.Fields.get(0).name + "▽");
//                                        }
//                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
        View keyviewtemp = getFlowBeanFragment().findViewByName("附属条件");
        if (keyviewtemp instanceof ImageButtonView) {
            keyview = (ImageButtonView) keyviewtemp;
            keyview.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (auxTblAttStru.Fields.size() == 0) {
                        if (auxImageButtonView == null) {
                            return;
                        }
                        setauxTblAttStru(auxImageButtonView.getValue(), new AuxUtils.AfterOnsucess() {
                            @Override
                            public void afterSucess() {
                                if (keyview != null) {
//                                    if (auxTblAttStru.Fields.size() > 0) {
//                                        keyview.setValue(auxTblAttStru.Fields.get(0).name + "▽");
//                                    }
                                    popKeyConditionDialog(auxTblAttStru);
                                }
                            }
                        });
                    } else {
                        popKeyConditionDialog(auxTblAttStru);
                    }
                }
            });
//            keyview.getEditText().setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (auxTblAttStru.Fields.size() == 0) {
//                        if (auxImageButtonView == null) {
//                            return;
//                        }
//                        setauxTblAttStru(auxImageButtonView.getValue(), new AuxUtils.AfterOnsucess() {
//                            @Override
//                            public void afterSucess() {
//                                if (keyview != null) {
////                                    if (auxTblAttStru.Fields.size() > 0) {
////                                        keyview.setValue(auxTblAttStru.Fields.get(0).name + "▽");
////                                    }
//                                    popKeyConditionDialog(auxTblAttStru);
//                                }
//                            }
//                        });
//                    } else {
//                        popKeyConditionDialog(auxTblAttStru);
//                    }
//                }
//            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == ResultCode.RESULT_WHERE_FETCHED) {
            String resultInfo = data.getStringExtra("where");
            if (resultInfo == null) {
                resultInfo = "";
            }
            keyview.setValue(resultInfo);
        }
    }

    private void popKeyConditionDialog(AuxTblAttStru auxTblAttStru) {
        List<String> conditons = new ArrayList<String>();
        for (MapGISField field : auxTblAttStru.Fields) {
            conditons.add(field.name);
        }
        if (conditons.size() == 0) {
            Toast.makeText(this, "无字段名，无法选择条件", Toast.LENGTH_SHORT).show();
            return;
        }
        if (layerImageButtonView == null) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        String layerName = layerImageButtonView.getValue();
        if (TextUtils.isEmpty(layerName)) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);
        if (onlineLayerInfo == null) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if (auxImageButtonView == null) {
            Toast.makeText(this, "附属数据未选择", Toast.LENGTH_SHORT).show();
            return;
        }
        String strAuxTableName = auxImageButtonView.getValue();
        if (TextUtils.isEmpty(strAuxTableName)) {
            Toast.makeText(this, "附属数据未选择", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ConditionChoseActivity.class);
        intent.putExtra("wordStrings", conditons.toArray(new String[conditons.size()]));
        intent.putExtra("layerID", onlineLayerInfo.id);
        intent.putExtra("auxTabName", strAuxTableName);
        intent.putExtra("Envelope", getIntent().getStringExtra("Envelope"));
        startActivityForResult(intent, 0);
    }

    private void setAuxList(String name) {
        OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(name);
        if (onlineLayerInfo == null) {
            return;
        }
        aux_tables.getAux_TablesFromGisServer(this, onlineLayerInfo.id, new AuxUtils.AfterOnsucess() {
            @Override
            public void afterSucess() {
                if (auxImageButtonView != null) {
                    auxImageButtonView.setValue(aux_tables.getAux_Table(0));
                }
            }
        });
    }

    private void setauxTblAttStru(String auxTbl, AuxUtils.AfterOnsucess listener) {
        if (TextUtils.isEmpty(auxTbl)) {
            return;
        }
        if (layerImageButtonView == null) {
            return;
        }
        String layername = layerImageButtonView.getValueTextView().getText().toString();
        if (TextUtils.isEmpty(layername)) {
            return;
        }
        OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layername);
        if (onlineLayerInfo == null) {
            return;
        }

        auxTblAttStru.getAuxTblAttStruFromGisServer(this, onlineLayerInfo.id, auxTbl, listener);
    }

    @Override
    protected void handleOkEvent(String tag, List<FeedItem> feedItemList) {
        switch (tag) {
            case "查询条件": {
                startQueryAux(feedItemList);
            }
            break;
        }
    }

    private void startQueryAux(List<FeedItem> feedItemList) {
        String layerID = "";
        if (layerImageButtonView == null) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        String layerName = layerImageButtonView.getValue();
        if (TextUtils.isEmpty(layerName)) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);
        if (onlineLayerInfo == null) {
            Toast.makeText(this, "图层不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        layerID = onlineLayerInfo.id;
        String geometry = "";
        //geometry 不传
//        String areatemp = "";
//        if (areaview != null && (areatemp = areaview.getValue()).length() > 0) {
//            geometry = GisUtil.getMinMaxAreaVal(areatemp);
//        }
        //(电话 = '13814113120') AND (编号 = 'SB00002')
        //  String strCon = getQueryKeyConditon(feedItemList);
        String strCon = keyview.getValue();
        //测试
        // strCon=" (编号 like '%1000%') ";
        String strAuxTableName = "";

        if (auxImageButtonView == null) {
            Toast.makeText(this, "附属数据不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        strAuxTableName = auxImageButtonView.getValue();
        if (TextUtils.isEmpty(strAuxTableName)) {
            Toast.makeText(this, "附属数据不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ConditionQueryAuxListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("layerID", layerID);
        bundle.putString("geometry", geometry);
        bundle.putString("strCon", strCon);
        bundle.putString("strAuxTableName", strAuxTableName);
        intent.putExtra("bundle", bundle);
        startActivity(intent);

    }
}
