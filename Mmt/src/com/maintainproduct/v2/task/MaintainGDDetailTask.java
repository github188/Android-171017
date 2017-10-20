package com.maintainproduct.v2.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.maintainproduct.entity.ResultDataWC;
import com.maintainproduct.v2.caselist.GDItem;
import com.maintainproduct.v2.caselist.MaintainConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.util.ArrayList;

/**
 * 获取工单详情任务
 */
public class MaintainGDDetailTask extends AsyncTask<String, Integer, String> {
    private final BaseActivity activity;
    private final Handler handler;

    public MaintainGDDetailTask(BaseActivity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        activity.setBaseProgressBarVisibility(true);
    }

    @Override
    protected String doInBackground(String... params) {
        String jsonStr = null;
        try {
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/CaseManage/GongDanInfo";

            jsonStr = NetUtil.executeHttpGet(url, "CaseID", params[0]);
        } catch (Exception e) {
            return null;
        }

        return jsonStr;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            return;
        }
        activity.setBaseProgressBarVisibility(false);

        if (result == null || result.length() == 0) {
            activity.showErrorMsg("未正确获取信息");
            return;
        }

        ResultDataWC<GDItem> resultObj = new Gson().fromJson(result, new TypeToken<ResultDataWC<GDItem>>() {
        }.getType());

        if (resultObj.getMe == null || resultObj.getMe.size() == 0) {
            activity.showErrorMsg(resultObj.say.errMsg);
            return;
        }

        GDItem item = resultObj.getMe.get(0);

        ArrayList<GDControl> controls = new ArrayList<GDControl>();

        controls.add(new GDControl("工单编号", "标签", item.BillCode));
        controls.add(new GDControl("上报时间", "标签", item.OccurTime));
        controls.add(new GDControl("上报人员", "标签", item.ReportMan));
        controls.add(new GDControl("所属部门", "标签", item.ReportGroup));

        if (!TextUtils.isEmpty(item.UserTel)) {
            controls.add(new GDControl("客户姓名", "标签", item.UserTrueName));
            controls.add(new GDControl("客户电话", "标签", item.UserTel));
            controls.add(new GDControl("客户编号", "标签", item.UserCode));
        }

        controls.add(new GDControl("事件类型", "标签", item.ReportType));
        controls.add(new GDControl("事件内容", "标签", item.ReportContent));
        controls.add(new GDControl("事件来源", "标签", item.EventSource));
        controls.add(new GDControl("处理级别", "标签", item.Level));
        controls.add(new GDControl("派单人员", "标签", item.DispatchMan));
        controls.add(new GDControl("事件地址", "标签", item.Address));
        controls.add(new GDControl("事件描述", "标签", item.Description));
        controls.add(new GDControl("受理备注", "标签", item.Remark));
        controls.add(new GDControl("现场照片", "图片", item.Picture));
        controls.add(new GDControl("现场录音", "录音", item.Recording));

        GDControl control = new GDControl("地图位置", "坐标", item.Position);

        control.IsVisible = "false";

        controls.add(control);

        GDFormBean bean = new GDFormBean();

        GDGroup group = new GDGroup();

        group.Name = "工单详情";
        group.Controls = controls.toArray(new GDControl[controls.size()]);

        bean.Groups = new GDGroup[]{group};

        GDCaseInfo info = new GDCaseInfo();

        info.Item = item;
        info.Bean = bean;

        Message message = handler.obtainMessage();
        message.what = MaintainConstant.SERVER_GET_DETAIL_SUCCESS;
        message.obj = info;
        handler.sendMessage(message);
    }
}
