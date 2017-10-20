package com.repair.shaoxin.water.valveinstruction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.FlowNodeMeta;
import com.zondy.mapgis.android.graphic.Graphic;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ValveDevicePropertyActivity extends BaseActivity {

    private static final String REPORT_GROUP_NAME = "反馈信息";

    private ValveModel mValveModel;

    private String tableName = "开关阀指令执行记录表";
    private String paramKey = "id";
    private String paramValue;

    private FlowNodeMeta mFeedbackFlowNodeMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);

        getBaseTextView().setText("阀门设备属性");
        this.mValveModel = getIntent().getParcelableExtra("ListItemEntity");
        this.paramValue = String.valueOf(mValveModel.ID);

        initBottomView();

        String searchCriteria = "编号='" + mValveModel.gisCode + "'";
        fetchDevicePropertyInfo(mValveModel.gisLayer, searchCriteria);
    }

    private void fetchDevicePropertyInfo(final String layerName, final String searchCriteria) {

        MmtBaseTask<Void, Void, LinkedHashMap<String, String>> mmtBaseTask
                = new MmtBaseTask<Void, Void, LinkedHashMap<String, String>>(ValveDevicePropertyActivity.this) {
            @Override
            protected LinkedHashMap<String, String> doInBackground(Void... params) {

                LinkedHashMap<String, String> result = new LinkedHashMap<>();

                List<Graphic> graphics = GisQueryUtil.conditionQuery(layerName, searchCriteria);

                if (graphics == null || graphics.size() == 0) {
                    return result;
                }

                Graphic graphic = graphics.get(0);

                for (int m = 0; m < graphic.getAttributeNum(); m++) {
                    String key = graphic.getAttributeName(m);

                    // 跳过类似这种自定义的字段 $图层名称$
                    if (key.startsWith("$") && key.endsWith("$")) {
                        continue;
                    }

                    // 判断key是否包含中文，如果没有中文不做显示
                    boolean isExistChinese = false;
                    for (char k : key.toCharArray()) {
                        isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");
                        if (isExistChinese) {
                            break;
                        }
                    }
                    if (!isExistChinese) {
                        continue;
                    }

                    String value = graphic.getAttributeValue(m);
                    value = (TextUtils.isEmpty(value) || value.equalsIgnoreCase("null")) ? "-" : value;
                    result.put(key, value);
                }

                return result;
            }

            @Override
            protected void onSuccess(LinkedHashMap<String, String> graphicMap) {

                if (graphicMap.size() == 0) {
                    Toast.makeText(ValveDevicePropertyActivity.this,
                            "设备属性查询失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                initDevicePropertyView(graphicMap);
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void initDevicePropertyView(LinkedHashMap<String, String> graphicMap) {

        ListFragment listFragment = new ListFragment();
        DevicePropertyAdapter adapter = new DevicePropertyAdapter(this, graphicMap);
        listFragment.setListAdapter(adapter);
        addFragment(listFragment);
    }

    private void fetchFeedbackDataFromServer(final String tableName, final String paramKey, final String paramValue) {

        MmtBaseTask<Void, Void, ResultData<FlowNodeMeta>> mmtBaseTask =
                new MmtBaseTask<Void, Void, ResultData<FlowNodeMeta>>(ValveDevicePropertyActivity.this) {

                    @Override
                    protected ResultData<FlowNodeMeta> doInBackground(Void... params) {

                        StringBuilder sb = new StringBuilder();
                        sb.append(ServerConnectConfig.getInstance().getBaseServerPath())
                                .append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/GetTableDataInfo")
                                .append("?tableName=").append(tableName)
                                .append("&key=").append(paramKey)
                                .append("&value=").append(paramValue);

                        Results<FlowNodeMeta> rawData;
                        try {
                            String jsonResult = NetUtil.executeHttpGet(sb.toString());
                            if (TextUtils.isEmpty(jsonResult)) {
                                throw new Exception("获取信息失败：网络错误");
                            }
                            rawData = new Gson().fromJson(jsonResult, new TypeToken<Results<FlowNodeMeta>>() {
                            }.getType());
                        } catch (Exception e) {
                            e.printStackTrace();
                            rawData = new Results<>("-100", e.getMessage());
                        }
                        return rawData.toResultData();
                    }

                    @Override
                    protected void onSuccess(ResultData<FlowNodeMeta> resultData) {
                        if (resultData.ResultCode != 200) {
                            Toast.makeText(ValveDevicePropertyActivity.this,
                                    resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final FlowNodeMeta flowNodeMeta = resultData.getSingleData();

                        // 需要反馈的 Group可以编辑，其他的均不可编辑
                        mFeedbackFlowNodeMeta = new FlowNodeMeta();
                        for (FlowNodeMeta.TableGroup tableGroup : flowNodeMeta.Groups) {
                            if (!tableGroup.GroupName.equals(REPORT_GROUP_NAME)) {
                                continue;
                            }
                            tableGroup.GroupName = "";
                            mFeedbackFlowNodeMeta.Groups.add(tableGroup);
                            for (FlowNodeMeta.FieldSchema fieldSchema : tableGroup.Schema) {
                                for (FlowNodeMeta.TableValue tableValue : flowNodeMeta.Values) {
                                    if (fieldSchema.FieldName.equals(tableValue.FieldName)) {
                                        mFeedbackFlowNodeMeta.Values.add(tableValue);
                                    }
                                }
                            }
                            break;
                        }

                        initFeedbackView();
                    }
                };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void initFeedbackView() {

        GDFormBean formBean = mFeedbackFlowNodeMeta.mapToGDFormBean();

        Intent intent = new Intent(ValveDevicePropertyActivity.this, ValveInstructionFeedbackActivity.class);
        intent.putExtra("Tag", "信息跟踪");
        intent.putExtra("Title", "反馈指令：" + mValveModel.introductionContent);
        intent.putExtra("GDFormBean", formBean);

        intent.putExtra("FlowNodeMeta", new Gson().toJson(mFeedbackFlowNodeMeta));
        intent.putExtra("TableName", tableName);
        intent.putExtra("TableRecordID", paramValue);

        startActivityForResult(intent, 100);
    }

    private void initBottomView() {
        addBottomUnitView("反馈上报", false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchFeedbackDataFromServer(tableName, paramKey, paramValue);
            }
        });
    }

    private class DevicePropertyAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final List<String> keyList;
        private final List<String> valueList;

        public DevicePropertyAdapter(Context context, LinkedHashMap<String, String> graphicMap) {

            this.mInflater = LayoutInflater.from(context);

            this.keyList = new LinkedList<>();
            this.valueList = new LinkedList<>();
            for (LinkedHashMap.Entry<String, String> entry : graphicMap.entrySet()) {
                keyList.add(entry.getKey());
                valueList.add(entry.getValue());
            }
        }

        @Override
        public int getCount() {
            return keyList.size();
        }

        @Override
        public Object getItem(int position) {
            return keyList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final DevicePropertyAdapter.ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, parent, false);
                holder = new DevicePropertyAdapter.ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);
                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                convertView.findViewById(R.id.asset_value_text).setVisibility(View.GONE);
                holder.position = position;

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String value = ((TextView) v.findViewById(R.id.asset_value)).getText().toString();
                        Toast.makeText(ValveDevicePropertyActivity.this, value, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                holder = (DevicePropertyAdapter.ViewHolder) convertView.getTag();
            }

            holder.assetKey.setText(keyList.get(position));
            holder.assetValue.setText(valueList.get(position));

            convertView.setTag(holder);
            return convertView;
        }

        class ViewHolder {
            public TextView assetKey;
            public TextView assetValue;
            public int position;
        }
    }

}
