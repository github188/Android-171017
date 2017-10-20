package com.repair.zhoushan.module.flowcenter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;

import java.util.ArrayList;

public class FlowCenterNavigationActivity extends BaseActivity {

    protected LinearLayout mainContentView;
    protected ArrayList<FlowCenterData> flowCenterDataList;

    private int titleTextColor;
    private int itemTextColor;
    private int itemBgColor;

    private int titlePaddingVertical;
    private int itemPaddingVertical;
    private int itemPaddingHorizontal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.titleTextColor = Color.parseColor("#FF333333");
        this.itemTextColor = Color.parseColor("#FF666666");
        this.itemBgColor = Color.parseColor("#FFFFFFFF");

        this.titlePaddingVertical = DimenTool.dip2px(this, 14.0f);
        this.itemPaddingVertical = DimenTool.dip2px(this, 16.0f);
        this.itemPaddingHorizontal = DimenTool.dip2px(this, 18.0f);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setDefaultContentView() {
        setContentView(R.layout.flow_center_activity);
        setSwipeBackEnable(false);

        this.mainContentView = (LinearLayout) findViewById(R.id.mainContentView);
        getBaseTextView().setText("流程中心");
        addBackBtnListener(getBaseLeftImageView());

        this.flowCenterDataList = getIntent().getParcelableArrayListExtra("FlowCenterDataList");
        if (flowCenterDataList != null) {
            initView();
        } else {
            new MmtBaseTask<String, Void, String>(FlowCenterNavigationActivity.this, true) {
                @Override
                protected String doInBackground(String... params) {
                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + userID
                            + "/GetFlowCenterData?type=mobile";
                    return NetUtil.executeHttpGet(url);
                }

                @Override
                protected void onSuccess(String jsonResult) {
                    String defErrorMsg = "获取流程中心信息失败";
                    ResultData<FlowCenterData> result = Utils.json2ResultDataToast(
                            FlowCenterData.class, FlowCenterNavigationActivity.this, jsonResult, defErrorMsg, true);
                    if (result == null) return;

                    if (result.DataList == null || result.DataList.size() == 0) {
                        Toast.makeText(FlowCenterNavigationActivity.this, "流程中心信息为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    flowCenterDataList = result.DataList;
                    initView();
                }
            }.mmtExecute();
        }
    }

    protected void initView() {

        if (flowCenterDataList == null || flowCenterDataList.size() == 0) {
            return;
        }

        // 对FlowCenterData列表按照业务类型分组
        ArrayList<ArrayList<FlowCenterData>> bizGroups = new ArrayList<>();
        ArrayList<FlowCenterData> curFlowGroups = new ArrayList<>();
        bizGroups.add(curFlowGroups);

        ArrayList<String> bizTypeList = new ArrayList<>();
        String currentBizType = flowCenterDataList.get(0).BusinessType;
        bizTypeList.add(currentBizType);

        for (FlowCenterData item : flowCenterDataList) {
            if (!item.BusinessType.equals(currentBizType)) {
                currentBizType = item.BusinessType;
                if (bizTypeList.contains(currentBizType)) {
                    curFlowGroups = bizGroups.get(bizTypeList.indexOf(currentBizType));
                } else {
                    curFlowGroups = new ArrayList<>();
                    bizGroups.add(curFlowGroups);
                    bizTypeList.add(currentBizType);
                }
            }
            curFlowGroups.add(item);
        }

        for (ArrayList<FlowCenterData> flowGroups : bizGroups) {
            addItem(flowGroups);
        }
    }

    /**
     * 使用默认的点击事件
     *
     * @param flowGroups
     */
    protected void addItem(ArrayList<FlowCenterData> flowGroups) {
        addItem(flowGroups, null);
    }

    /**
     * 可以自定义一个点击事件，并且可以根据自定义的onClick事件的返回值来确定是否继续执行默认操作
     *
     * @param flowGroups
     * @param callBackListener
     */
    protected void addItem(final ArrayList<FlowCenterData> flowGroups, final CallBackListener callBackListener) {
        // The title of the group.@maoshoubei本次分组的标题呢？
        LinearLayout groupTitle = new LinearLayout(this);
        groupTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        groupTitle.setOrientation(LinearLayout.HORIZONTAL);
        groupTitle.setPadding(itemPaddingHorizontal, titlePaddingVertical, itemPaddingHorizontal, titlePaddingVertical);
        groupTitle.setBackgroundColor(itemBgColor);

        // title text  @maoshoubei为标题设置text
        TextView groupTitleText = new TextView(this);
        groupTitleText.setText(flowGroups.get(0).BusinessType);
        groupTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
        groupTitleText.setTextColor(titleTextColor);
        groupTitle.addView(groupTitleText);

        mainContentView.addView(groupTitle);

        for (FlowCenterData flowCenterData : flowGroups) {

            // Divider line @maoshoubei添加分割线
            View dividerView = new View(this);
            dividerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimenTool.dip2px(this, 0.5f)));
            dividerView.setBackgroundColor(Color.parseColor("#FFDCDCDC"));
            mainContentView.addView(dividerView);

            //@maoshoubei 分组下某一item的布局
            RelativeLayout flowItemView = new RelativeLayout(this);
            flowItemView.setPadding(itemPaddingHorizontal, itemPaddingVertical, itemPaddingHorizontal, itemPaddingVertical);
            flowItemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            flowItemView.setClickable(true);
            flowItemView.setBackgroundResource(R.drawable.flowcenter_item_selector);
            flowItemView.setTag(flowCenterData);

            flowItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlowCenterData flowCenterData = (FlowCenterData) view.getTag();

                    //目前流程中心不支持自定义
                    if (callBackListener != null) {
                        boolean flag = callBackListener.onClick(view, flowGroups.indexOf(flowCenterData));
                        // 如果flag事件为true，则表示消耗掉此事件并且不再执行下面的事件
                        if (flag) {
                            return;
                        }
                    }

                    String activity = "";
                    if (flowCenterData.EventName.contains("GIS属性上报")) {
                        activity = "com.repair.gisdatagather.enn.GISDataGatherActivity";
                    } else if (flowCenterData.EventName.contains("管网采集")) {
                        activity = "com.repair.gisdatagather.product.projectlist.ProjectListActivity";
                    }
                    if (!TextUtils.isEmpty(activity)) {
                        try {
                            Class cs = Class.forName(activity);
                            Intent intent = new Intent(FlowCenterNavigationActivity.this, cs);
                            intent.putExtra("FlowCenterData", flowCenterData);
                            startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            FlowCenterNavigationActivity.this.showToast("自定义activity不存在");
                        }
                        return;
                    }

                    Intent intent = new Intent(FlowCenterNavigationActivity.this, ZSEventReportActivity.class);
                    intent.putExtra("FlowCenterData", flowCenterData);
                    boolean isFromNavigation = getIntent().getBooleanExtra(ZSEventReportActivity.FLAG_FROM_NAVIGATION_MENU, false);
                    intent.putExtra(ZSEventReportActivity.FLAG_FROM_NAVIGATION_MENU, isFromNavigation);
                    Bundle bundle = getIntent().getBundleExtra("gisInfo");
                    if (bundle != null) {
                        intent.putExtra("gisInfo", bundle);
                    }
                    startActivity(intent);
                }
            });

            //@maoshoubei 某一item的key
            TextView textView = new TextView(this);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textView.setLayoutParams(layoutParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f);
            textView.setTextColor(itemTextColor);
            textView.setText(flowCenterData.EventName);

            flowItemView.addView(textView);

            //@maoshoubei 某一item的 右边的更多箭头
            ImageView imageView = new ImageView(this);
            RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParam.addRule(RelativeLayout.CENTER_VERTICAL);
            imageView.setLayoutParams(layoutParam);
            imageView.setImageDrawable(this.getResources().getDrawable(R.drawable.button_for_more));

            flowItemView.addView(imageView);

            mainContentView.addView(flowItemView);
        }

        // Divider line @maosoubei 分组与分组之间的分割线
        View dividerBottom = new View(this);
        LinearLayout.LayoutParams lpBottom = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DimenTool.dip2px(this, 0.5f));
        lpBottom.setMargins(0, 0, 0, DimenTool.dip2px(this, 8f));
        dividerBottom.setLayoutParams(lpBottom);
        dividerBottom.setBackgroundColor(Color.parseColor("#FFDCDCDC"));
        mainContentView.addView(dividerBottom);
    }

    protected interface CallBackListener {
        boolean onClick(View view, int index);
    }

}
