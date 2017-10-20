package com.repair.gisdatagather.product.layershow;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.GISDataBeanBase;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.simplecache.ACache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/5/5.
 */
public class LayerShowPanel implements LayerShowInterface, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    protected GisGather gisGather;
    public View view;
    public String uniquenessLineLayer;

    public LayerShowPanel(GisGather gisGather, View view) {
        this.gisGather = gisGather;
        this.view = view;
    }

    public void initLayerShowPanel() {
        view.findViewById(R.id.editDefaultAttrs).setOnClickListener(this);
        getCanEditLayer();
    }

    @Override
    public void onClick(View v) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }
        String text = ((TextView) v).getText().toString();
        switch (text) {
            case "默认属性": {
                editDefaultAttr();
            }
            break;
            default: {
                // Toast.makeText(gisGather.mapGISFrame, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void createRadioGroup(Context context, List<RadioButton> radioButtons) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb = (RadioButton) group.findViewById(group.getCheckedRadioButtonId());
        for (GISDeviceSetBean gisDeviceSetBean : gisGather.gisDeviceSetBeans) {
            if (rb.getText().equals(gisDeviceSetBean.alias)) {
                gisGather.hasChoseGISDeviceSetBean = gisDeviceSetBean;
                break;
            }
        }
    }

    @Override
    public void clickRadioButton(String layerName) {
        try {
            String alias = "";
            for (GISDeviceSetBean gisDeviceSetBean : gisGather.gisDeviceSetBeans) {
                if (gisDeviceSetBean.layerName.equals(layerName)) {
                    alias = gisDeviceSetBean.alias;
                    break;
                }
            }
            if (TextUtils.isEmpty(alias)) {
                Toast.makeText(gisGather.mapGISFrame, "图层别名和真实名比配错误", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gisGather.rg != null) {
                for (int i = 0; i < gisGather.rg.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) gisGather.rg.getChildAt(i);
                    if (rb.getText().equals(alias)) {
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(gisGather.mapGISFrame, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void editDefaultAttr() {
        GISDataBeanBase gisDataBeanBase = null;
        if (GisGather.layerDefaultAttrs.containsKey(gisGather.hasChoseGISDeviceSetBean.layerName)) {
            gisDataBeanBase = GisGather.layerDefaultAttrs.get(gisGather.hasChoseGISDeviceSetBean.layerName);
        }
        if (gisDataBeanBase == null) {
            gisDataBeanBase = new GISDataBeanBase();
        }
        gisDataBeanBase.LayerName = gisGather.hasChoseGISDeviceSetBean.layerName;

        GisDataGatherUtils.editAttrForProduct(gisGather.mapGISFrame, gisGather.hasChoseGISDeviceSetBean, true, gisDataBeanBase, -1, -1, gisGather.isPad);
    }

    @Override
    public void getCanEditLayer() {
        new MmtBaseTask<Void, Void, String>(gisGather.mapGISFrame, true) {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    ACache aCache = GisDataGatherUtils.getMCache(gisGather.mapGISFrame);
                    String layerInfo = aCache.getAsString("layerInfo");
                    if (TextUtils.isEmpty(layerInfo)) {
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GisUpdate/GetGISDeviceConfigList";

                        layerInfo = NetUtil.executeHttpGet(url);
                        aCache.put("layerInfo", layerInfo);
                    }
                    return layerInfo;
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);

                ResultData<GISDeviceSetBean> gisDeviceSetBeanResultData = com.repair.zhoushan.common.Utils.json2ResultDataToast(GISDeviceSetBean.class, gisGather.mapGISFrame, s, "网络异常", false);

                if (gisDeviceSetBeanResultData == null) {
                    return;
                }

                List<GISDeviceSetBean> gisDeviceSetBeans = gisDeviceSetBeanResultData.DataList;
                if (gisDeviceSetBeans == null) {
                    return;
                }
                layerInfoHand(gisDeviceSetBeans);

            }
        }.executeOnExecutor(MyApplication.executorService);
    }

    public void createRadioButtons(List<GISDeviceSetBean> gisDeviceSetBeans) {
        List<RadioButton> radioButtons = new ArrayList<RadioButton>();
        for (GISDeviceSetBean gisDeviceSetBean : gisDeviceSetBeans) {
            gisGather.gisDeviceSetBeans.add(gisDeviceSetBean);

            RadioButton rb = new RadioButton(gisGather.mapGISFrame);
            rb.setText(gisDeviceSetBean.alias);
            radioButtons.add(rb);
        }
        createRadioGroup(gisGather.mapGISFrame, radioButtons);
    }

    public void layerInfoHand(final List<GISDeviceSetBean> gisDeviceSetBeans) {
        createRadioButtons(gisDeviceSetBeans);

        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {

                for (GISDeviceSetBean gisDeviceSetBean : gisDeviceSetBeans) {
                    String tableName = gisDeviceSetBean.table;
                    Object object = GisDataGatherUtils.getMCache(gisGather.mapGISFrame).getAsObject(gisDeviceSetBean.layerName + "_view");
                    if (!(object instanceof FlowNodeMeta)) {
                        String url2 = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMetaV2?tableName=" + tableName;
                        String viewStr = NetUtil.executeHttpGet(url2);
                        if (TextUtils.isEmpty(viewStr)) {
                            continue;
                        }

                        Results<FlowNodeMeta> results = new Gson().fromJson(viewStr, new TypeToken<Results<FlowNodeMeta>>() {
                        }.getType());

                        if (results == null) {
                            continue;
                        }
                        ResultData<FlowNodeMeta> resultData = results.toResultData();
                        if (resultData == null) {
                            continue;
                        }
                        if (resultData.ResultCode <= 0) {
                            continue;
                            // MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
                        }
                        FlowNodeMeta flowNodeMeta = resultData.getSingleData();
                        GisDataGatherUtils.getMCache(gisGather.mapGISFrame).put(gisDeviceSetBean.layerName + "_view", flowNodeMeta);
                    }
                }

                //获取图层的默认属性
                getLayerDefaultAttr();
            }
        });

        //默认选中第一个图层
        gisGather.hasChoseGISDeviceSetBean = gisGather.gisDeviceSetBeans.get(0);
        //判断是否只有一个管线
        uniquenessLineLayer = getUniquenessLine();
    }

    public void getLayerDefaultAttr() {
        ACache aCache = GisDataGatherUtils.getMCache(gisGather.mapGISFrame);
        for (GISDeviceSetBean gisDeviceSetBean : gisGather.gisDeviceSetBeans) {
            String defaultAttrs = aCache.getAsString(gisDeviceSetBean.layerName);
            if (TextUtils.isEmpty(defaultAttrs)) {
                continue;
            }
            GISDataBeanBase gisDataBeanBase = new Gson().fromJson(defaultAttrs, GISDataBeanBase.class);
            GisGather.layerDefaultAttrs.put(gisDeviceSetBean.layerName, gisDataBeanBase);
        }
    }

    public String getUniquenessLine() {
        int i = 0;
        String layername = "";
        for (GISDeviceSetBean gisDeviceSetBean : gisGather.gisDeviceSetBeans) {
            if (gisDeviceSetBean.layerType == 2) {
                layername = gisDeviceSetBean.layerName;
                i++;
            }
        }
        if (i == 1) {
            return layername;
        }
        return null;
    }

}
