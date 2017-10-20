package com.repair.zhoushan.module.casemanage;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.Node;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.eventreport.EventReportTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FlowInfoPostParam;
import com.repair.zhoushan.module.HandoverUserFragment;
import com.repair.zhoushan.module.casemanage.casedetail.GetHandoverUsersTask;

import java.util.UUID;

/**
 * Created by liuyunfan on 2016/5/10.
 */
public class FlowNodeHandler extends Handler {
    // 移交（需选择承办人）
    public final static int SELECT_TO_NEXT = 1;

    //跨站移交
    public final static int SELECT_TO_NEXT_KZ = 11;
    //处理站点移交 (未实现)
    public final static int SELECT_TO_NEXT_CLZD = 111;
    // 自处理
    // public final static int SELECT_TO_NEXT_DEFAULT = 2;

    //首节点自处理
    public final static int FIRSTNODE_HAND_SELF = 8;

    //非首节点自处理
    public final static int NON_FIRSTNODE_HAND_SELF = 9;

    public final static int HANDOVER_TO_NEXT = 3;
    // 结案
    public final static int GO_TO_END = 4;
    // 保存
    public final static int SAVE_CURRENT_NODE = 5;
    // 移交默认人
    public final static int SELECT_TO_DEFAULT_PERSON = 7;

    BaseActivity context;
    FlowHandSucessCallBack sucessCallBack;

    //工单信息
    protected CaseItem caseItemEntity;
    // 上报信息体
    protected FlowInfoPostParam flowInfoPostParam;

    public FlowInfoPostParam getFlowInfoPostParam() {
        return flowInfoPostParam;
    }

    public void setSucessCallBack(FlowHandSucessCallBack sucessCallBack) {
        this.sucessCallBack = sucessCallBack;
    }

    public void setFlowInfoPostParam(FlowInfoPostParam flowInfoPostParam) {
        this.flowInfoPostParam = flowInfoPostParam;
    }

    String absolutePaths;
    String relativePaths;

    public FlowNodeHandler(BaseActivity context, CaseItem caseItemEntity, FlowInfoPostParam flowInfoPostParam, String absolutePaths, String relativePaths) {
        this.context = context;
        this.caseItemEntity = caseItemEntity;
        this.flowInfoPostParam = flowInfoPostParam;
        this.absolutePaths = absolutePaths;
        this.relativePaths = relativePaths;
    }

    public FlowNodeHandler(BaseActivity context, CaseItem caseItemEntity, FlowInfoPostParam flowInfoPostParam) {
        this(context, caseItemEntity, flowInfoPostParam, "", "");
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                //选择人
                case SELECT_TO_NEXT:
                case SELECT_TO_NEXT_KZ:
                case SELECT_TO_NEXT_CLZD:
                    getHandoverUsersTree(msg.what);
                    break;
                case FIRSTNODE_HAND_SELF: {
                    flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                    flowInfoPostParam.caseInfo.Direction = 1;
                    // 自处理：首节点分派或者上报的时候 caseInfo 中的 Undertakeman传自己的ID即可，
                    // 不需要下一个节点的ID（服务已处理）,但是中间节点还是需要传入下一个节点ID(保持原样).
                    flowInfoPostParam.caseInfo.Undertakeman = String.valueOf(MyApplication.getInstance().getUserId());
                    flowInfoPostParam.caseInfo.Opinion = "";
                    reportEvent();
                }
                break;
                case NON_FIRSTNODE_HAND_SELF:
                    flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                    flowInfoPostParam.caseInfo.Direction = 1;
                    // 自处理：首节点分派或者上报的时候 caseInfo 中的 Undertakeman传自己的ID即可，
                    // 不需要下一个节点的ID（服务已处理）,但是中间节点还是需要传入下一个节点ID(保持原样).
                    flowInfoPostParam.caseInfo.Undertakeman = caseItemEntity.NextNodeID + "/" + MyApplication.getInstance().getUserId();
                    flowInfoPostParam.caseInfo.Opinion = "";
                    reportEvent();
                    break;
                //从面板选择人后移交
                case HANDOVER_TO_NEXT:
                    flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                    flowInfoPostParam.caseInfo.Direction = 1;
                    flowInfoPostParam.caseInfo.Undertakeman = msg.getData().getString("undertakeman");
                    flowInfoPostParam.caseInfo.Opinion = msg.getData().getString("option");
                    reportEvent();
                    break;

                case SELECT_TO_DEFAULT_PERSON:
                    // 移交默认人 Undertakeman传空，只会移交给自己站点下面的人
                    flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                    reportEvent();
                    break;

                case GO_TO_END:
                    flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();
                    reportEvent();
                    break;

                case SAVE_CURRENT_NODE:
                    saveEvent();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportEvent() {

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/PostFlowNodeData";

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                this.caseItemEntity.ActiveName,
                absolutePaths,
                relativePaths);

        new EventReportTask(context, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    // 服务 PostFlowNodeData 的返回的状态码为新插入记录的 Id
                    if (result.ResultCode > 0) {
                        Toast.makeText(context, "上传成功!", Toast.LENGTH_SHORT).show();
                        if (sucessCallBack != null) {
                            sucessCallBack.sucessCallBack();
                        }
                    } else {
                        Toast.makeText(context, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    private void getHandoverUsersTree(int mode) {
        CaseInfo ci = caseItemEntity.mapToCaseInfo();

        switch (mode) {
            case SELECT_TO_NEXT_KZ: {
                ci.Station = "";
            }
            break;
            case SELECT_TO_NEXT_CLZD: {

            }
            break;
            default: {
            }
        }

        new GetHandoverUsersTask(context, true, new MmtBaseTask.OnWxyhTaskListener<Node>() {
            @Override
            public void doAfter(Node node) {
                if (node != null) {
                    HandoverUserFragment fragment = new HandoverUserFragment(FlowNodeHandler.this, HANDOVER_TO_NEXT, node);
                    fragment.setIsContainNodeId(true);
                    fragment.show(context.getSupportFragmentManager(), "");
                } else {
                    Toast.makeText(context, "获取移交人员失败", Toast.LENGTH_SHORT).show();
                }
            }
        }).mmtExecute(new Gson().toJson(ci));
    }

    private void saveEvent() {

        flowInfoPostParam.caseInfo = caseItemEntity.mapToCaseInfo();

        // 创建服务路径
        String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/SaveFlowNodeData";

        // 将对信息转换为JSON字符串
        String data = new Gson().toJson(flowInfoPostParam, new TypeToken<FlowInfoPostParam>() {
        }.getType());

        // 将所有信息封装成后台上传的数据模型
        final ReportInBackEntity entity = new ReportInBackEntity(
                data,
                MyApplication.getInstance().getUserId(),
                ReportInBackEntity.REPORTING,
                uri,
                UUID.randomUUID().toString(),
                this.caseItemEntity.ActiveName,
                absolutePaths,
                relativePaths);

        new EventReportTask(context, true, new MmtBaseTask.OnWxyhTaskListener<ResultData<Integer>>() {
            @Override
            public void doAfter(ResultData<Integer> result) {
                try {
                    if (result.ResultCode == 200) {
                        Toast.makeText(context, "上传成功!", Toast.LENGTH_SHORT).show();
                        if (sucessCallBack != null) {
                            sucessCallBack.sucessCallBack();
                        }
                    } else {
                        Toast.makeText(context, "上报失败：" + result.ResultMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).mmtExecute(entity);
    }

    public interface FlowHandSucessCallBack {
        void sucessCallBack();
    }
}


