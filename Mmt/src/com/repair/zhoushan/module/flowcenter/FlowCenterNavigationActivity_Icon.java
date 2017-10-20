package com.repair.zhoushan.module.flowcenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.FlowCenterData;
import com.repair.zhoushan.module.eventreport.ZSEventReportActivity;

import java.util.ArrayList;

/**
 * Drop the layout of BaseActivity.
 */
public class FlowCenterNavigationActivity_Icon extends BaseActivity {

    private String titleName;
    private ArrayList<FlowCenterData> flowCenterDataList;

    LinearLayout mainContentView;

    public final int[] imgResource = new int[]{
            R.drawable.flowcenter_user,
            R.drawable.flowcenter_tools,
            R.drawable.flowcenter_clock,
            R.drawable.flowcenter_flag,
            R.drawable.flowcenter_portfolio,
            R.drawable.flowcenter_announcement,
            R.drawable.flowcenter_security,
            R.drawable.flowcenter_box,
            R.drawable.flowcenter_options
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.titleName = getIntent().getStringExtra("Alias");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setDefaultContentView() {

        setContentView(R.layout.flow_center_activity);

        this.mainContentView = (LinearLayout) findViewById(R.id.mainContentView);
        getBaseTextView().setText(titleName);
        getBaseLeftImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new FetchFlowCenterDataTask(this, true, new MmtBaseTask.OnWxyhTaskListener<ArrayList<FlowCenterData>>() {
            @Override
            public void doAfter(ArrayList<FlowCenterData> flowCenterDatas) {
                if (flowCenterDatas == null) {
                    Toast.makeText(FlowCenterNavigationActivity_Icon.this, "获取流程中心信息失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (flowCenterDatas.size() == 0) {
                    Toast.makeText(FlowCenterNavigationActivity_Icon.this, "流程中心信息为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                flowCenterDataList = flowCenterDatas;

                initView();
            }
        }).mmtExecute(MyApplication.getInstance().getUserId() + "");

        setSwipeBackEnable(false);
    }

    private void initView() {

        if (flowCenterDataList.size() > 0) {

            // 对FlowCenterData列表进行分组
            ArrayList<ArrayList<FlowCenterData>> bizGroups = new ArrayList<>();
            ArrayList<FlowCenterData> curFlowGroups = new ArrayList<>();

            String currentBizType = flowCenterDataList.get(0).BusinessType;

            int index = 0;
            for (FlowCenterData item : flowCenterDataList) {
                if (!item.BusinessType.equals(currentBizType)) {
                    currentBizType = item.BusinessType;
                    bizGroups.add(curFlowGroups);

                    curFlowGroups = new ArrayList<>();
                }
                item.Icon = imgResource[index++ % imgResource.length];
                curFlowGroups.add(item);
            }
            bizGroups.add(curFlowGroups);

            // 构造界面
            for (ArrayList<FlowCenterData> flowGroups : bizGroups) {
                // The title of the group.
                LinearLayout groupTitle = new LinearLayout(this);
                groupTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                groupTitle.setOrientation(LinearLayout.VERTICAL);
                int padding = DimenTool.dip2px(this, 10.0f);
                groupTitle.setPadding(padding, padding, padding, padding);
                groupTitle.setBackgroundColor(Color.parseColor("#eaeeee"));

                TextView groupTitleText = new TextView(this);
                groupTitleText.setText(flowGroups.get(0).BusinessType);
                groupTitleText.setTextSize(DimenTool.dip2px(this, 10.0f));
                // groupTitleText.getPaint().setFakeBoldText(true); //字体加粗
                groupTitle.addView(groupTitleText);

                mainContentView.addView(groupTitle);

                GridView gridView = new CusGridView(this);
                gridView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
                gridView.setNumColumns(4);
                final GridViewAdapter adapter = new GridViewAdapter(this, flowGroups);
                gridView.setAdapter(adapter);
                gridView.setSelector(R.drawable.flowcenter_item_selector);
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FlowCenterData flowCenterData = (FlowCenterData) adapter.getItem(position);

                        Intent intent = new Intent(FlowCenterNavigationActivity_Icon.this, ZSEventReportActivity.class);
                        intent.putExtra("flowName", flowCenterData.FlowName);
                        intent.putExtra("nodeName", flowCenterData.NodeName);
                        intent.putExtra("tableGroup", "");

                        startActivity(intent);
                    }
                });
                mainContentView.addView(gridView);
            }
        }
    }

    private final class GridViewAdapter extends BaseAdapter{

        private Context mContext;
        private ArrayList<FlowCenterData> flowList;
        private LayoutInflater mLayoutInflater;

        public GridViewAdapter(Context context, ArrayList<FlowCenterData> flowList) {
            this.mContext = context;
            this.flowList = flowList;
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return flowList.size();
        }

        @Override
        public Object getItem(int position) {
            return flowList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = null;
            ViewHolder viewHolder = null;

            if (convertView == null) {
                view = mLayoutInflater.inflate(R.layout.flow_center_option_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) view.findViewById(R.id.item_img);
                viewHolder.textView = (TextView) view.findViewById(R.id.item_txt);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(flowList.get(position).FlowName);

//            Bitmap bitmap = ((BitmapDrawable) viewHolder.imageView.getDrawable()).getBitmap();
//            if (!bitmap.isRecycled()) {
//                bitmap.recycle();
//            }
            viewHolder.imageView.setImageDrawable(mContext.getResources().getDrawable(flowList.get(position).Icon));

            return view;
        }

        public final class ViewHolder {
            public ImageView imageView;
            public TextView textView;
        }
    }

    /**
     * 需要传入一个参数userId
     */
    final class FetchFlowCenterDataTask extends MmtBaseTask<String, Void, ArrayList<FlowCenterData>> {

        public FetchFlowCenterDataTask(Context context, boolean showLoading, OnWxyhTaskListener<ArrayList<FlowCenterData>> listener) {
            super(context, showLoading, listener);
        }

        @Override
        protected ArrayList<FlowCenterData> doInBackground(String... params) {

            try {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + params[0]
                        + "/GetFlowCenterData";

                String resultJson = NetUtil.executeHttpGet(url);

                if (BaseClassUtil.isNullOrEmptyString(resultJson)) {
                    return null;
                }

                Results<FlowCenterData> rawResult = new Gson().fromJson(resultJson, new TypeToken<Results<FlowCenterData>>() {
                }.getType());

                ResultData<FlowCenterData> result = rawResult.toResultData();
                if (result.ResultCode != 200) {
                    return null;
                }

                return result.DataList;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
