package com.repair.gisdatagather.product.editdata;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.GISDataBeanBase;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.common.entity.TextLine;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.enn.bean.GISDeviceSetBean;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2015/12/16.
 */
public class EditDataFragment extends Fragment {
    BaseActivity bcontext;
    static EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();
    FlowBeanFragment formBeanFragment;
    private static List<FeedItem> feedbackItems = null;

    static GISDataBeanBase gisDataBeanBase;
    boolean isEditDefaultAttr = false;
    GISDeviceSetBean gisDeviceSetBean;
    int dataIndex = -1;
    int from = GisDataGatherUtils.GisDataFrom.todayProject;
    HashMap<String, String> oldAttrValues = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bcontext = (BaseActivity) getActivity();

        bcontext.getBaseRightImageView().setImageResource(R.drawable.ic_scan_qrcode_24dp);
        bcontext.getBaseRightImageView().setVisibility(View.VISIBLE);

        Bundle bundle = getArguments();

        isEditDefaultAttr = bundle.getBoolean("isEditDefaultAttr");
        dataIndex = bundle.getInt("dataIndex");
        from = bundle.getInt("from");
        gisDeviceSetBean = new Gson().fromJson(bundle.getString("hasChoseGISDeviceSetBean"), GISDeviceSetBean.class);

        gisDataBeanBase = new Gson().fromJson(bundle.getString("gisDataBeanBase"), GISDataBeanBase.class);
        if (isEditDefaultAttr) {
            bcontext.setTitleAndClear("<" + gisDeviceSetBean.alias + ">默认属性编辑");
        } else {
            bcontext.setTitleAndClear("<" + gisDeviceSetBean.alias + ">GIS属性编辑");
        }

        new MmtBaseTask<Void, Void, FlowNodeMeta>(bcontext, true) {
            @Override
            protected FlowNodeMeta doInBackground(Void... params) {
                try {
                    Object object = GisDataGatherUtils.getMCache(bcontext).getAsObject(gisDataBeanBase.LayerName + "_view");
                    if (!(object instanceof FlowNodeMeta)) {
                        String url2 = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableGroupMetaV2?tableName=" + gisDeviceSetBean.table;
                        String viewStr = NetUtil.executeHttpGet(url2);
                        if (TextUtils.isEmpty(viewStr)) {
                            MyApplication.getInstance().showMessageWithHandle("服务请求错误");
                            return null;
                        }

                        Results<FlowNodeMeta> results = new Gson().fromJson(viewStr, new TypeToken<Results<FlowNodeMeta>>() {
                        }.getType());

                        if (results == null) {
                            MyApplication.getInstance().showMessageWithHandle("数据解析错误");
                            return null;
                        }
                        ResultData<FlowNodeMeta> resultData = results.toResultData();
                        if (resultData == null) {
                            MyApplication.getInstance().showMessageWithHandle("数据错误");
                            return null;
                        }
                        if (resultData.ResultCode <= 0) {
                            MyApplication.getInstance().showMessageWithHandle(resultData.ResultMessage);
                            return null;
                        }
                        FlowNodeMeta flowNodeMeta = resultData.getSingleData();
                        GisDataGatherUtils.getMCache(bcontext).put(gisDataBeanBase.LayerName + "_view", flowNodeMeta);

                        return flowNodeMeta;

                    }
                    return (FlowNodeMeta) object;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onSuccess(FlowNodeMeta flowNodeMeta) {

                if (flowNodeMeta == null) {
                    return;
                }

                eventInfoPostParam.DataParam.flowNodeMeta = handFlowNodeMeta(flowNodeMeta);

                GDFormBean gdFormBean = eventInfoPostParam.DataParam.flowNodeMeta.mapToGDFormBean();

                if (gdFormBean == null) {
                    return;
                }
                createView(gdFormBean);

            }
        }.executeOnExecutor(MyApplication.executorService);


        //提前保存原先的值
        boolean isEdit = "编辑".equals(gisDataBeanBase.Operation);
        if (isEdit) {
            HashMap<String, String> defaultAttrValues = GisDataGatherUtils.str2HashMap(gisDataBeanBase.OldAtt);
            oldAttrValues.putAll(defaultAttrValues);
        }
    }

    /**
     * 规定：
     * 1.编辑默认属性：属性存放在NewAtt中
     * 2.新增gis数据：NewAtt存放默认属性 oldAtt为空
     * 3.编辑gis数据：oldAtt存放原有属性 newAtt默认存放原有属性
     *
     * @param flowNodeMeta
     * @return
     */
    public FlowNodeMeta handFlowNodeMeta(FlowNodeMeta flowNodeMeta) {

        //通过数据库字段集合过滤一次
        filterGroup(flowNodeMeta.Groups);

        //处理默认值
        if (gisDataBeanBase == null) {
            return flowNodeMeta;
        }
        String viewAttrvalues = gisDataBeanBase.NewAtt;
        if (TextUtils.isEmpty(viewAttrvalues)) {
            return flowNodeMeta;
        }

        HashMap<String, String> defaultAttrValues = GisDataGatherUtils.str2HashMap(viewAttrvalues);

        if (defaultAttrValues != null && defaultAttrValues.size() > 0) {
            List<FlowNodeMeta.TableValue> values = flowNodeMeta.Values;
            for (int i = 0; i < values.size(); i++) {
                String field = values.get(i).FieldName;
                if (defaultAttrValues.containsKey(field)) {
                    values.get(i).FieldValue = defaultAttrValues.get(field);
                }
            }
        }
        return flowNodeMeta;
    }

    /**
     * 通过字段别名集合过滤需要的字段
     * 没必要再用gis属性过滤
     *
     * @param gisgroups
     * @return
     */
    public void filterGroup(List<FlowNodeMeta.TableGroup> gisgroups) {
        //字段别名集
        String fliters = gisDeviceSetBean.fileds;
        if (TextUtils.isEmpty(fliters)) {
            //字断集为空，默认显示全部
            if (gisgroups.size() == 1) {
                gisgroups.get(0).GroupName = "";
            }
            return;
        }

        for (FlowNodeMeta.TableGroup group : gisgroups) {

            int size = group.Schema.size();

            for (int i = size - 1; i >= 0; i--) {
                FlowNodeMeta.FieldSchema filedSchema = group.Schema.get(i);

                if (!fliters.contains(filedSchema.Alias) && !fliters.contains(filedSchema.FieldName)) {
                    group.Schema.remove(i);
                }
            }
        }
        if (gisgroups.size() == 1) {
            gisgroups.get(0).GroupName = "";
        }
    }


    protected void createView(GDFormBean formBean) {
        formBeanFragment = new FlowBeanFragment();
        formBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                //管顶标高 赋值
                View view = formBeanFragment.findViewByName("管顶标高");
                if (view == null) {
                    return;
                }
                if (view instanceof ImageEditView) {
                    ImageEditView imageEditView = (ImageEditView) view;
                    if (!TextUtils.isEmpty(imageEditView.getValue())) {
                        return;
                    }
                    double z = GpsReceiver.getInstance().getLastLocalLocation().getZ();

                    imageEditView.setValue(String.valueOf(Convert.FormatDouble(z)));
                }
                if (view instanceof ImageTextView) {
                    ImageTextView imageTextView = (ImageTextView) view;
                    if (!TextUtils.isEmpty(imageTextView.getValue())) {
                        return;
                    }
                    double z = GpsReceiver.getInstance().getLastLocalLocation().getZ();
                    ((ImageTextView) view).setValue(String.valueOf(Convert.FormatDouble(z)));
                }

            }
        });
        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", formBean);
        formBeanFragment.setArguments(args);

        formBeanFragment.setCls(ZSEventReportActivity.class);
        formBeanFragment.setAddEnable(true);
        formBeanFragment.setFragmentFileRelativePath(GisDataGatherUtils.getFlowCenterDataForProduct().BizCode);

        bcontext.replaceOtherFragment(formBeanFragment);
        bcontext.showMainFragment(false);


        createBottomView();
    }

    protected void createBottomView() {

        bcontext.clearAllBottomUnitView();
        BottomUnitView manageUnitView = new BottomUnitView(bcontext);
        manageUnitView.setContent("保存");
        manageUnitView.setImageResource(R.drawable.handoverform_report);
        bcontext.addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (formBeanFragment == null) {
                    return;
                }
                feedbackItems = formBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);

                if (feedbackItems == null) {
                    return;
                }
                if (isEditDefaultAttr) {
                    saveDefaultAttr();
                } else {
                    editGISDataBean();
                }
            }
        });

    }

    /**
     * 服务要求新旧属性个数一样
     */
    public void editGISDataBean() {
        if (dataIndex < 0) {
            Toast.makeText(bcontext, "保存异常", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> newKvs = new ArrayList<>();
        List<String> oldKvs = new ArrayList<>();

        for (FeedItem item : feedbackItems) {
            item.Value = GisDataGatherUtils.getRightGisVal(item.Value);
            newKvs.add(item.Name + ":" + item.Value);
            String oldVal = "";
            if (oldAttrValues.containsKey(item.Name)) {

                oldVal = oldAttrValues.get(item.Name);
                oldVal = GisDataGatherUtils.getRightGisVal(oldVal);
            }
            oldKvs.add(item.Name + ":" + oldVal);
        }

        gisDataBeanBase.NewAtt = GisDataGatherUtils.gisKVs2Str(newKvs);
        gisDataBeanBase.OldAtt = GisDataGatherUtils.gisKVs2Str(oldKvs);
        //地名库也当作管点
        if ("管点".equals(gisDataBeanBase.GeomType)) {

            final TextDot textDot = from == GisDataGatherUtils.GisDataFrom.todayProject ? GisGather.gisDataProject.getTodayGISData().textDots.get(dataIndex) : GisGather.gisDataProject.getTextDots().get(dataIndex);
            textDot.gisDataBean.OldAtt = gisDataBeanBase.OldAtt;
            textDot.gisDataBean.NewAtt = gisDataBeanBase.NewAtt;

            textDot.gisDataBean.updataGisData(bcontext, new MmtBaseTask.OnWxyhTaskListener<String>() {
                @Override
                public void doAfter(String s) {
                    ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(bcontext, s, "更新属性失败", "更新属性成功");
                    if (resultWithoutData == null) {
                        return;
                    }
                    AppManager.finishActivity(getActivity());
                    textDot.updateAttr();

                }
            });
        } else {
            final TextLine textLine = from == GisDataGatherUtils.GisDataFrom.todayProject ? GisGather.gisDataProject.getTodayGISData().textLines.get(dataIndex) : GisGather.gisDataProject.getTextLines().get(dataIndex);
            textLine.gisDataBean.OldAtt = gisDataBeanBase.OldAtt;
            textLine.gisDataBean.NewAtt = gisDataBeanBase.NewAtt;

            textLine.gisDataBean.updataGisData(bcontext, new MmtBaseTask.OnWxyhTaskListener<String>() {
                @Override
                public void doAfter(String s) {
                    ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(bcontext, s, "更新属性失败", "更新属性成功");
                    if (resultWithoutData == null) {
                        return;
                    }
                    AppManager.finishActivity(getActivity());
                    textLine.updateAttr();

                }
            });
        }

    }

    public void saveDefaultAttr() {
        gisDataBeanBase.NewAtt = GisDataGatherUtils.gisFeedback2Str(feedbackItems);
        GisGather.layerDefaultAttrs.put(gisDataBeanBase.LayerName, gisDataBeanBase);

        GisDataGatherUtils.getMCache(bcontext).put(gisDataBeanBase.LayerName, new Gson().toJson(gisDataBeanBase));

        Toast.makeText(bcontext, "保存成功", Toast.LENGTH_SHORT).show();
        AppManager.finishActivity(bcontext);
    }

}
