package com.repair.gisdatagather.common.entity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.PainGisDatas2MapViewThread;
import com.repair.gisdatagather.common.PainGisdata2MapViewHander;
import com.repair.gisdatagather.common.utils.GisDataGatherUtils;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.EventInfoPostParam;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.repair.zhoushan.module.casemanage.FlowNodeHandler;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2016/2/25.
 * 手持端GIS工程实体
 * 分为今日采集的gis数据和非今日采集的gis数据
 * 今日采集的只存在todayGISData中
 */
public class GISDataProject extends GisProjectWithoutGisData {

    //一个工程中非今日采集的管点管线
    private List<TextDot> textDots = new ArrayList<>();
    private List<TextLine> textLines = new ArrayList<>();
    //一个工程中今日采集的管点管线
    private TodayGISData todayGISData = new TodayGISData();

    //工程的工单信息
    private CaseItem caseItem;
    private FlowNodeHandler flowNodeHandler;

    //上一步操作是否添点操作
    private boolean hasAddDoted = false;

    public void setHasAddDoted(boolean hasAddDoted) {
        this.hasAddDoted = hasAddDoted;
    }

    public boolean getHasAddDoted() {
        return hasAddDoted;
    }

    public void setCaseItem(CaseItem caseItem) {
        this.caseItem = caseItem;
    }

    public List<TextDot> getTextDots() {
        return textDots;
    }

    public List<TextLine> getTextLines() {
        return textLines;
    }

    public TodayGISData getTodayGISData() {
        return todayGISData;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public GISDataProject(MapView mapView, GisProjectWithGisData gisProjectWithGisData) {
        GisDataGatherUtils.handgisDatas(mapView.getContext(), gisProjectWithGisData.gisDatas, GisDataGatherUtils.GisDataFrom.currentProject, textDots, textLines, todayGISData);
        positionxy = gisProjectWithGisData.positionxy;
        if (TextUtils.isEmpty(positionxy)) {
            GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();
            if (xy != null) {
                positionxy = xy.getX() + "," + xy.getY();
            }
        }
        BoundRect = gisProjectWithGisData.BoundRect;
        if (TextUtils.isEmpty(BoundRect)) {
            List<TextDot> allTextDots = new ArrayList<TextDot>() {{
                addAll(textDots);
                addAll(todayGISData.textDots);
            }};
            Rect rect = GisDataGatherUtils.getRectFromTextDots(allTextDots);
            if (rect != null) {
                BoundRect = rect.toString();
            }
        }

        this.ID = gisProjectWithGisData.ID;
        this.EventCode = gisProjectWithGisData.EventCode;
        this.CaseNo = gisProjectWithGisData.CaseNo;
        this.EventState = gisProjectWithGisData.EventState;
        this.EventName = gisProjectWithGisData.EventName;
        this.ReporterName = gisProjectWithGisData.ReporterName;
        this.ReporterDepart = gisProjectWithGisData.ReporterDepart;
        this.ReportTime = gisProjectWithGisData.ReportTime;
        this.UpdateTime = gisProjectWithGisData.UpdateTime;
        this.UpdateStatus = gisProjectWithGisData.UpdateStatus;
        this.IsUpdate = gisProjectWithGisData.IsUpdate;
        this.IsGatherFinish = gisProjectWithGisData.IsGatherFinish;
        this.IsExportedPointLineTable = gisProjectWithGisData.IsExportedPointLineTable;
        this.ProjectName = gisProjectWithGisData.ProjectName;
    }

    public GISDataProject(String projectName) {
        this.ProjectName = projectName;
        this.IsUpdate = 0;//监控端是否提交到gis服务器了
        this.IsGatherFinish = 0;
        this.IsExportedPointLineTable = "否";
        GpsXYZ xy = GpsReceiver.getInstance().getLastLocalLocation();
        if (xy != null) {
            this.positionxy = xy.getX() + "," + xy.getY();
        }
        List<TextDot> allTextDots = new ArrayList<TextDot>() {{
            addAll(textDots);
            addAll(todayGISData.textDots);
        }};
        Rect rect = GisDataGatherUtils.getRectFromTextDots(allTextDots);
        if (rect != null) {
            this.BoundRect = rect.toString();
        }
    }

    private String reportGISData(int ID) {
        List<GISDataBeanBase> gisDataBeanBases = new ArrayList<>();
        for (TextDot textDot : textDots) {
            gisDataBeanBases.add(textDot.gisDataBean);
        }
        for (TextLine textLine : textLines) {
            gisDataBeanBases.add(textLine.gisDataBean);
        }

        String result = "";
        try {
            result = NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostGISDatas/" + ID, new Gson().toJson(gisDataBeanBases));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public ReportInBackEntity getProjectReportInBackEntity(final Context context, FlowCenterData flowCenterData) {
        if (context == null) {
            MyApplication.getInstance().showMessageWithHandle("上下文错误");
            return null;
        }

        final EventInfoPostParam eventInfoPostParam = new EventInfoPostParam();

       //默认就是自处理
        eventInfoPostParam.DataParam.caseInfo.Undertakeman = MyApplication.getInstance().getUserId() + "";

        eventInfoPostParam.DataParam.caseInfo.FlowName = flowCenterData.FlowName;
        eventInfoPostParam.DataParam.caseInfo.NodeName = flowCenterData.NodeName;
        eventInfoPostParam.DataParam.caseInfo.TableGroup = "";
        eventInfoPostParam.DataParam.caseInfo.UserID = MyApplication.getInstance().getUserId();
        eventInfoPostParam.DataParam.caseInfo.IsCreate = flowCenterData.IsCreate;
        eventInfoPostParam.DataParam.caseInfo.BizCode = flowCenterData.BizCode;
        eventInfoPostParam.DataParam.caseInfo.EventMainTable = flowCenterData.TableName;
        eventInfoPostParam.DataParam.caseInfo.EventName = flowCenterData.EventName;
        eventInfoPostParam.DataParam.caseInfo.UpdateEvent = flowCenterData.HandoverMode;
        eventInfoPostParam.DataParam.caseInfo.AddappointHandRols = "管网审核人员";

        eventInfoPostParam.BizCode = flowCenterData.BizCode;
        eventInfoPostParam.EventName = flowCenterData.EventName;
        eventInfoPostParam.TableName = flowCenterData.TableName;

        if (eventInfoPostParam.DataParam.flowNodeMeta == null) {
            eventInfoPostParam.DataParam.flowNodeMeta = new FlowNodeMeta();
        }

        eventInfoPostParam.DataParam.flowNodeMeta.Values.clear();
        eventInfoPostParam.DataParam.flowNodeMeta.Values.addAll(new GISProjectReportEntity(GISDataProject.this).convert2TableValue());

        String uri;
        String reportData = "";
        if (flowCenterData.IsCreate == 1) {
            // 创建流程
            //移交
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

            reportData = new Gson().toJson(eventInfoPostParam.DataParam, new TypeToken<FlowInfoPostParam>() {
            }.getType());
        } else {
            // 不创建流程
            //只上报
            uri = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostEventData";
            // 将信息转换为JSON字符串
            reportData = new Gson().toJson(eventInfoPostParam, new TypeToken<EventInfoPostParam>() {
            }.getType());
        }

        // 将所有信息封装成后台上传的数据模型
        return new ReportInBackEntity(
                reportData,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                flowCenterData.BusinessType,
                "",
                "");
    }

    public boolean hasnonEditGisData() {
        for (TextDot textDot : textDots) {
            if (textDot.state == 0) {
                return true;
            }
        }
        for (TextLine textLine : textLines) {
            if (textLine.state == 0) {
                return true;
            }
        }
        return false;
    }

    private FlowInfoPostParam createFlowInfoPostParam() {
        //这里空的FlowInfoPostParam的即可，flowNodeHandler里会初始化FlowInfoPostParam的caseinof
        FlowInfoPostParam flowInfoPostParam = new FlowInfoPostParam();
        return flowInfoPostParam;
    }

    public void handoverrPoj(final GisGather gisGather) {
        final BaseActivity context = gisGather.mapGISFrame;
        if (caseItem == null) {
            Toast.makeText(context, "下次进入方可提交", Toast.LENGTH_SHORT).show();
            return;
        }

        FlowInfoPostParam flowInfoPostParam = createFlowInfoPostParam();
        if (flowNodeHandler == null) {
            flowNodeHandler = new FlowNodeHandler(context, caseItem, flowInfoPostParam);
        }
        flowNodeHandler.setFlowInfoPostParam(flowInfoPostParam);

        flowNodeHandler.setSucessCallBack(new FlowNodeHandler.FlowHandSucessCallBack() {
            @Override
            public void sucessCallBack() {
                updatePojInfo(context);
                submitAddressLayer(context);

                gisGather.mapGISFrame.findViewById(R.id.mapviewClear)
                        .performClick();
                gisGather.restroeMapFrame();
                gisGather.mapGISFrame.backByReorder(true);
            }
        });
        flowNodeHandler.setFlowInfoPostParam(flowInfoPostParam);

        //分三步：1.移交流程，2.更改工程范围，3.更新地名库
        //写死：移交默认人
        flowNodeHandler.sendEmptyMessage(FlowNodeHandler.SELECT_TO_DEFAULT_PERSON);
    }

    private void updatePojInfo(BaseActivity context) {
        List<TextDot> allTextDots = new ArrayList<TextDot>() {{
            addAll(textDots);
            addAll(todayGISData.textDots);
        }};
        Rect rect = GisDataGatherUtils.getRectFromTextDots(allTextDots);
        if (rect != null) {
            BoundRect = rect.toString();
        }
        if (TextUtils.isEmpty(BoundRect)) {
            MyApplication.getInstance().showMessageWithHandle("工程范围异常");
            return;
        }
        if (ID <= 0) {
            MyApplication.getInstance().showMessageWithHandle("工程ID异常");
            return;
        }
        new MmtBaseTask<Void, Void, String>(context) {
            @Override
            protected String doInBackground(Void... params) {
                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SetPoj2Finished";
                return NetUtil.executeHttpGet(uri, "ID", ID + "", "BoundRect", BoundRect);
            }

            @Override
            protected void onSuccess(String s) {
                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "工程信息更新失败", "工程信息更新成功");
                if (resultWithoutData == null) {
                    return;
                }
            }
        }.mmtExecute();
    }

    //更新地名库
    private void submitAddressLayer(BaseActivity context) {
        //更新地名库
        new MmtBaseTask<Void, Void, String>(context) {
            @Override
            protected String doInBackground(Void... params) {
                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/GisGatherOperLocName";
                return NetUtil.executeHttpGet(uri, "projectID", ID + "");
            }

            @Override
            protected void onSuccess(String s) {
                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, " 地名库操作失败", "地名库操作成功");
                if (resultWithoutData == null) {
                    return;
                }
            }
        }.mmtExecute();
    }

    /**
     * 将服务器上未采集完毕的工程置为已采集完毕，并填充相关信息
     */
    public void submit2Server(BaseActivity context) {

        List<TextDot> allTextDots = new ArrayList<TextDot>() {{
            addAll(textDots);
            addAll(todayGISData.textDots);
        }};
        Rect rect = GisDataGatherUtils.getRectFromTextDots(allTextDots);
        if (rect != null) {
            BoundRect = rect.toString();
        }
        if (TextUtils.isEmpty(BoundRect)) {
            MyApplication.getInstance().showMessageWithHandle("工程范围异常");
            return;
        }
        if (ID <= 0) {
            MyApplication.getInstance().showMessageWithHandle("工程ID异常");
            return;
        }
        new MmtBaseTask<Void, Void, String>(context) {
            @Override
            protected String doInBackground(Void... params) {
                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SetPoj2Finished";
                return NetUtil.executeHttpGet(uri, "ID", ID + "", "BoundRect", BoundRect);
            }

            @Override
            protected void onSuccess(String s) {
                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "工程提交失败", "工程提交成功");
                if (resultWithoutData == null) {
                    return;
                }
                //更新地名库
                new MmtBaseTask<Void, Void, String>(context) {
                    @Override
                    protected String doInBackground(Void... params) {
                        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/GisGatherOperLocName";
                        return NetUtil.executeHttpGet(uri, "projectID", ID + "");
                    }

                    @Override
                    protected void onSuccess(String s) {
                        ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, " 地名库操作失败", "");
                        if (resultWithoutData == null) {
                            return;
                        }
                        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                mapGISFrame.showMainFragment(false);
                                mapGISFrame.refreshOtherFragment();
                                return true;
                            }
                        });
                    }
                }.mmtExecute();

            }
        }.mmtExecute();
    }

    public void painPoj2MapView(MapView mapView) {
        //先跳转后绘制
        if (todayGISData.textDots.size() == 0 && todayGISData.textLines.size() == 0 && textDots.size() == 0 && textLines.size() == 0) {
            GpsXYZ gpsXYZdot = GpsReceiver.getInstance().getLastLocalLocation();
            if (gpsXYZdot != null) {
                mapView.panToCenter(new Dot(gpsXYZdot.getX(), gpsXYZdot.getY()), true);
            }

        } else {
            Rect rect = GisDataGatherUtils.getRectFromTextDots(todayGISData.textDots);
            if (rect == null) {
                rect = GisDataGatherUtils.getRectFromTextLines(todayGISData.textLines);
            }
            if (rect == null) {
                rect = GisDataGatherUtils.getRectFromTextDots(textDots);
            }
            if (rect == null) {
                rect = GisDataGatherUtils.getRectFromTextLines(textLines);
            }
            if (rect != null) {
                mapView.zoomToRange(new Rect(rect.getXMin() - 80, rect.getYMin() - 80, rect.getXMax() + 80, rect.getYMax() + 80), true);
            }
            Handler handler = new PainGisdata2MapViewHander(mapView);

            new PainGisDatas2MapViewThread(mapView, this.textDots, this.textLines, this.getTodayGISData(), handler).start();
        }
    }

    public void deletePoj(Context context, final MmtBaseTask.OnWxyhTaskListener lister) {
        new MmtBaseTask<Void, Void, String>(context, true, lister) {
            @Override
            protected String doInBackground(Void... params) {
                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeleteGisPoj";
                return NetUtil.executeHttpGet(uri, "caseno", CaseNo, "pojID", "" + ID, "from", "mobile");
            }
        }.mmtExecute();
    }

    /**
     * 删除工程所有的管点管线
     *
     * @param context
     */
    public void resetPoj(Context context) {
        new MmtBaseTask<Void, Void, String>(context) {
            @Override
            protected String doInBackground(Void... params) {
                String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DelPojAllGisData";
                return NetUtil.executeHttpGet(uri, "caseno", CaseNo, "pojID", "" + ID);
            }

            @Override
            protected void onSuccess(String s) {
                ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(context, s, "工程清空失败", "工程清空成功");
                if (resultWithoutData == null) {
                    return;
                }
                MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        for (Iterator<TextLine> it = todayGISData.textLines.iterator(); it.hasNext(); ) {
                            TextLine textLine = it.next();
                            textLine.deleteTextLine(mapView, false);
                            it.remove();
                        }

                        for (Iterator<TextDot> it = todayGISData.textDots.iterator(); it.hasNext(); ) {
                            TextDot textDot = it.next();
                            textDot.deleteTextDot(mapView, false);
                            it.remove();
                        }

                        for (Iterator<TextLine> it = textLines.iterator(); it.hasNext(); ) {
                            TextLine textLine = it.next();
                            textLine.deleteTextLine(mapView, false);
                            it.remove();
                        }

                        for (Iterator<TextDot> it = textDots.iterator(); it.hasNext(); ) {
                            TextDot textDot = it.next();
                            textDot.deleteTextDot(mapView, false);
                            it.remove();
                        }
                        mapView.refresh();
                        return true;
                    }
                });
            }
        }.mmtExecute();
    }

    /**
     * 取最新的一个点
     * 点的存储是按照时间顺序存储的
     *
     * @return
     */
    public TextDot getPojLastDot() {
        if (todayGISData.textDots.size() == 0 && textDots.size() == 0) {
            return null;
        }
        int todaySize = 0;
        if ((todaySize = todayGISData.textDots.size()) > 0) {
            return todayGISData.textDots.get(todaySize-1);
        }
        int nontodaySize = 0;
        if ((nontodaySize = textDots.size()) > 0) {
            return textDots.get(nontodaySize-1);
        }
        return null;
    }


}
