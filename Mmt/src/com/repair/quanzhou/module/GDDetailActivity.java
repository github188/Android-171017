package com.repair.quanzhou.module;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.quanzhou.entity.QZResultData;
import com.repair.quanzhou.entity.WorkTaskBean;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuyunfan on 2016/1/21.
 */
public class GDDetailActivity extends BaseActivity {
    private static String caseno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndClear("工单详情");
        caseno = getIntent().getStringExtra("caseno");
        addFragment(new GDDetailFragment());
    }

    @Override
    public void onCustomBack() {
        super.backByReorder();
    }

    public static class GDDetailFragment extends Fragment {
        private BaseActivity context;
        private LinearLayout parentLayout;
        private QZResultData data;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            context = (BaseActivity) getActivity();
            ScrollView scrollView = new ScrollView(context);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentLayout = new LinearLayout(context);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(parentLayout);

            new MmtBaseTask<Void, Void, String>(context, true) {
                @Override
                protected String doInBackground(Void... params) {
                    // 外网访问路径
//                    StringBuilder sb = new StringBuilder(Utils.baseUrl);
//                    sb.append("/workTask.do?state=phoneShow")
//                            .append("&workTaskSeq=" + caseno);
                    // 内网
                    String url = ServerConnectConfig.getInstance().getBaseServerPath();
                    url += "/Services/CitySvr_Biz_QZ_HotLine/REST/BizQZHotLineRest.svc/GetHotLineWorkTask?workTaskSeq="
                            + caseno;
                    return NetUtil.executeHttpGetAppointLastTime(30, url);
                }

                @Override
                protected void onSuccess(String s) {
                    super.onSuccess(s);
                    if (BaseClassUtil.isNullOrEmptyString(s)) {
                        Toast.makeText(context, "网络错误", Toast.LENGTH_LONG).show();
                        return;
                    }
                    /*
                     * 去掉结果中两端的“以及字符串中的”\“
                     */
                    s = s.substring(1, s.length() - 1).replace("\\", "");

                    data = new Gson().fromJson(s, QZResultData.class);

                    if (data == null || !data.success) {
                        Toast.makeText(context, "未查询到工单号为" + caseno + "的信息", Toast.LENGTH_LONG).show();
                        return;
                    }

                    createView(data.workTaskBean);
                }
            }.executeOnExecutor(MyApplication.executorService);
            return scrollView;
        }

        public void createView(WorkTaskBean workTaskBean) {
            if (workTaskBean == null) {
                Toast.makeText(context, "无工单信息", Toast.LENGTH_LONG).show();
                return;
            }

            List<GDControl> controls = new ArrayList<>();

            controls.add(new GDControl("工单号：", "长文本", workTaskBean.workTaskSeq));
            controls.add(new GDControl("受理人：", "长文本", workTaskBean.workerIDName));
            controls.add(new GDControl("受理部门：", "长文本", workTaskBean.acceptDepartmentName));
            controls.add(new GDControl("当前状态：", "长文本", workTaskBean.stateIDName));
            controls.add(new GDControl("用户名：", "长文本", workTaskBean.customerName));
            controls.add(new GDControl("表号：", "长文本", workTaskBean.meterNumber));
            controls.add(new GDControl("表径：", "长文本", workTaskBean.meterSize));
            controls.add(new GDControl("用户号：", "长文本", workTaskBean.customerID));
            controls.add(new GDControl("来电号码：", "长文本", workTaskBean.complainCaller));
            controls.add(new GDControl("信息来源：", "长文本", workTaskBean.sourceIDName));
            controls.add(new GDControl("反映形式：", "长文本", workTaskBean.sourceTypeIDName));
            controls.add(new GDControl("联系人：", "长文本", workTaskBean.complainPerson));
            controls.add(new GDControl("联系电话：", "长文本", workTaskBean.relationTel));
            controls.add(new GDControl("紧急级别：", "长文本", workTaskBean.taskLayerName));
            controls.add(new GDControl("所属区域：", "长文本", workTaskBean.areaIDName));
            controls.add(new GDControl("受理时间：", "长文本", workTaskBean.acceptDate));
            controls.add(new GDControl("下单时间：", "长文本", workTaskBean.saveDate));
            controls.add(new GDControl("工单类别：", "长文本", workTaskBean.complainTypeIDName));
            controls.add(new GDControl("工单子类别：", "长文本", workTaskBean.complainTypeinfoIDName));
            controls.add(new GDControl("责任部门：", "长文本", workTaskBean.departmentIDName));
            controls.add(new GDControl("故障原因：", "长文本", workTaskBean.malfunctionTypeIDName));
            controls.add(new GDControl("责任人：", "长文本", workTaskBean.doingWorkerIDName));
            controls.add(new GDControl("责任人电话：", "长文本", workTaskBean.doingWorkerTel));
            controls.add(new GDControl("监督人：", "长文本", workTaskBean.leaderWorkerID1Name));
            controls.add(new GDControl("监督人电话：", "长文本", workTaskBean.leaderWorkerTel1));
            controls.add(new GDControl("详细地址：", "百度地址", workTaskBean.addressTablet));
            controls.add(new GDControl("派发部门：", "长文本", workTaskBean.moreDepartmentIDName));
            controls.add(new GDControl("受理内容：", "长文本", workTaskBean.questionMemo));
            controls.add(new GDControl("备注：", "长文本", workTaskBean.memo));

            for (GDControl control : controls) {
                control.setReadOnly(true);

                parentLayout.addView(control.createView(context));
            }

            BottomUnitView bottomUnitView1 = new BottomUnitView(context);
            bottomUnitView1.setBackgroundResource(R.drawable.button_radius);
            bottomUnitView1.setContent("现场处理");
            bottomUnitView1.textView.setTextColor(Color.WHITE);
            bottomUnitView1.textView.setTextSize(25);
            context.addBottomUnitView(bottomUnitView1, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handGD();
                }
            });

        }

        public void handGD() {
            Intent intent = new Intent(context, GDHandActivity.class);
            intent.putExtra("data", new Gson().toJson(data, QZResultData.class));
            context.startActivity(intent);
        }
    }
}
