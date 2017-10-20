package com.mapgis.mmt.module.gis.spatialquery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.investigation.ProtertyEditDialog;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.accident.AccidentCheckFragment;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxTablesInfo;
import com.patrolproduct.module.spatialquery.PipeDetailToolbarFeedbackFragment;
import com.repair.zhoushan.module.flowcenter.FlowCenterNavigationActivity;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.geometry.Dot;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PipeDetailFragment extends BackHandledFragment implements PipeDetailToolbarFragment.EventCallback {

    protected HashMap<String, String> graphicMap;

    protected LinkedHashMap<String, String> infos;
    protected Graphic graphic;

    protected EmsPipeDetailActivityAdapter adapter;
    private String layerName;
    /**
     * 返回时 指示 上一层 Activity的行为
     */
    private Boolean isSetResult = false;
    Fragment toolbarFragment;

    private Activity hostActivity;
    private Bundle arguments;

    private ArrayList<String> propertyData = new ArrayList<>();

    public ArrayList<String> getPropertyData() {
        return propertyData;
    }

    public static PipeDetailFragment createNewInstance(Bundle args) {
        Bundle arguments = new Bundle();
        arguments.putAll(args);

        PipeDetailFragment fragment = new PipeDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.hostActivity = (Activity) context;
        this.arguments = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pipe_detail, container, false);
        init(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        afterViewCreated(view);
    }

    protected void afterViewCreated(final View view) {
    }

    @SuppressWarnings("unchecked")
    private void init(View view) {
        try {
            String toolbar = MyApplication.getInstance().getConfigValue("MyPipeDetailToolbar");

            if (arguments.getBoolean("unvisiable_detail_fragment", false)
                    || (!TextUtils.isEmpty(toolbar) && toolbar.equals("unvisiable_detail_fragment"))) {
                view.findViewById(R.id.frag_pipe_detail_toolbar).setVisibility(View.GONE);
            } else {
                if (getArguments().getBoolean("fromPlan", false)) {
                    toolbarFragment = new PipeDetailToolbarFeedbackFragment((HashMap<String, String>) getArguments().getSerializable(
                            "graphicMap"));
                } else {
                    if (TextUtils.isEmpty(toolbar))
                        toolbarFragment = new PipeDetailToolbarFragment();
                    else
                        toolbarFragment = (Fragment) Class.forName(toolbar).newInstance();
                }

                getChildFragmentManager().beginTransaction().replace(R.id.frag_pipe_detail_toolbar, toolbarFragment).commit();
            }

            if (!arguments.getBoolean("needLoc", false)) {//默认不显示详情界面的定位按钮
                view.findViewById(R.id.detail_loc_btn).setVisibility(View.GONE);
            }

            view.findViewById(R.id.detail_revert_btn).setOnClickListener(revertOnClickListener);
            view.findViewById(R.id.detail_loc_btn).setOnClickListener(locOnClickListener);

            if (arguments.containsKey("graphic") && !arguments.containsKey("graphicMap")) {
                graphic = (Graphic) arguments.getSerializable("graphic");
                graphicMap = new LinkedHashMap<>();
                for (int i = 0; i < graphic.getAttributeNum(); i++) {
                    graphicMap.put(graphic.getAttributeName(i), graphic.getAttributeValue(i));
                }
            } else {
                // graphic = (Graphic) getIntent().getSerializableExtra("graphic");
                graphicMap = (HashMap<String, String>) arguments.getSerializable("graphicMap");
                if (arguments.containsKey("graphicMapStr")) {
                    graphicMap = new Gson().fromJson(arguments.getString("graphicMapStr"), new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType());
                    infos = (LinkedHashMap<String, String>) graphicMap;
                }
            }

            if ((layerName = arguments.getString("layerName")) != null) {
                ((TextView) view.findViewById(R.id.textview_Title)).setText(layerName);
            }

            isSetResult = arguments.getBoolean("isSetResult", false);

            if (graphicMap != null) {

                Object[] columnNames = GisUtil.getGISFields(layerName);

                ArrayList<String> result = resolveGraphicMap(graphicMap, columnNames);
                propertyData.addAll(result);

                if (!BaseClassUtil.isNullOrEmptyString(resultKey)) {
                    ((TextView) view.findViewById(R.id.detail_title)).setText(MessageFormat.format("{0}:{1}", resultKey, resultValue));
                }
            }

            adapter = new EmsPipeDetailActivityAdapter(getContext(), propertyData);
            ((ListView) view.findViewById(R.id.ListView_asset_detail)).setAdapter(adapter);

            //判断是否含有附属数据
            queryAuxData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resultKey, resultValue;

    protected ArrayList<String> resolveGraphicMap(HashMap<String, String> graphicMap, Object[] columnNames) {

        ArrayList<String> arrayList = new ArrayList<>();

        if (columnNames == null) {
            columnNames = graphicMap.keySet().toArray();
        }

        for (Object str : columnNames) {
            String key = (String) str;

            if (!graphicMap.containsKey(key)) {
                continue;
            }

            String value = graphicMap.get(str);

            if ("emapgisid".equalsIgnoreCase(key)) {
                resultKey = key;
                resultValue = value;
                continue;
            }

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

            arrayList.add(key + "`" + ((BaseClassUtil.isNullOrEmptyString(value) || value.equalsIgnoreCase("null")) ? "-" : value));
        }

        return arrayList;
    }

    AuxTablesInfo auxTablesInfo = new AuxTablesInfo();

    private void queryAuxData() {

        if (graphicMap == null) {
            return;
        }

        String layerName = graphicMap.get("$图层名称$");
        if (TextUtils.isEmpty(layerName)) {
            return;
        }
        final OnlineLayerInfo onlineLayerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);
        if (onlineLayerInfo == null || TextUtils.isEmpty(onlineLayerInfo.id)) {
            return;
        }
        auxTablesInfo.getAuxTablesInfoFromGisServer(getActivity(), onlineLayerInfo.id, new AuxUtils.AfterOnsucess() {
            @Override
            public void afterSucess() {
                if (!auxTablesInfo.isSuccess) {
                    return;
                }

                if (auxTablesInfo.Names.size() == 0) {
                    return;
                }

                //确认含有附属数据
                if (toolbarFragment instanceof PipeDetailToolbarFragment)
                    ((PipeDetailToolbarFragment) toolbarFragment).toggleAuxView(true, auxTablesInfo, graphicMap, onlineLayerInfo.id);
            }
        });
    }

    @Override
    public void navigate() {
        try {

            GisUtil.callOuterNavigationApp(getActivity(), getDeviceLocDot());

            // TODO: 1/18/17 巡检计划地图内导航跳转有问题
//            Dot dot = getDeviceLocDot();
//            boolean innerNaviMode = (MyApplication.getInstance().getConfigValue("NavigationMode", 0) == 1);
//            if (innerNaviMode) {
//                if (dot == null) {
//                    MyApplication.getInstance().showMessageWithHandle("坐标无效，无法导航");
//                    return;
//                }
//                BaseMapCallback callback = new ShowAreaAndPointMapCallback(hostActivity,
//                        dot.toString(), "", -1, -1, layerName, "", -1);
//                MyApplication.getInstance().sendToBaseMapHandle(callback);
//            } else {
//                GisUtil.callOuterNavigationApp(getActivity(), dot);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Dot getDeviceLocDot() {

        Dot dot = null;

        try {
            if (arguments.containsKey("xy") && !TextUtils.isEmpty(arguments.getString("xy"))) {
                dot = GisUtil.convertDot(arguments.getString("xy"));
            }

            if (dot == null) {
                if (graphicMap.containsKey("横座标")) {
                    dot = new Dot(Double.valueOf(graphicMap.get("横座标")), Double.valueOf(graphicMap.get("纵座标")));
                } else if (graphicMap.containsKey("横坐标")) {
                    dot = new Dot(Double.valueOf(graphicMap.get("横坐标")), Double.valueOf(graphicMap.get("纵坐标")));
                } else if (graphicMap.containsKey("X坐标")) {
                    dot = new Dot(Double.valueOf(graphicMap.get("X坐标")), Double.valueOf(graphicMap.get("Y坐标")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dot;
    }

    @Override
    public void report() {

        // 从巡检跳过来的特殊判断处理
        if (arguments.containsKey("patrolNo") && (arguments.getInt("isArrive", 0) == 0)) {
            Toast.makeText(getContext(), "该巡线点还未到位，不能上报", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // GIS图层
        String layerName = arguments.getString("layerName");
        if (TextUtils.isEmpty(layerName))
            layerName = graphicMap.get("$图层名称$");
        if (TextUtils.isEmpty(layerName)) {
            sb.append("'图层名称',");
        }

        // GIS编号
        String gisNo = arguments.getString("pipeNo");
        if (TextUtils.isEmpty(gisNo))
            gisNo = graphicMap.get("编号");
        if (TextUtils.isEmpty(gisNo)) {
            sb.append("'编号',");
        }

        // 坐标
        String xy = arguments.getString("xy");
        if (TextUtils.isEmpty(xy)) {
            Dot dot = getDeviceLocDot();
            if (dot != null)
                xy = dot.toString();
        }
        if (TextUtils.isEmpty(xy)) {
            sb.append("'坐标',");
        }

        // 地址（不强制要求）
        String addr = arguments.getString("place");
        if (TextUtils.isEmpty(addr)) {
            addr = graphicMap.get("位置");
        }

        if (sb.length() > 0) {
            sb.insert(0, "缺少参数:");
            Toast.makeText(getActivity(), sb.substring(0, sb.length() - 1), Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(getActivity(), FlowCenterNavigationActivity.class);

        Bundle bundle = new Bundle();

        bundle.putString("layerName", layerName); // GIS图层
        bundle.putString("filedVal", gisNo); // GIS编号
        bundle.putString("position", xy); // 坐标
        bundle.putString("addr", addr); // 地址

        // 巡检的特殊处理
        if (arguments.containsKey("patrolNo")) {
            Object obj = arguments.get("patrolNo");
            if (obj != null) {
                bundle.putString("patrolNo", obj.toString());
            }
        }

        intent.putExtra("gisInfo", bundle);
        startActivity(intent);
    }

    /**
     * 返回地图
     */
    View.OnClickListener revertOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isSetResult) {
                hostActivity.setResult(ResultCode.RESULT_PIPE_GOBACK);
            } else {
                hostActivity.setResult(Activity.RESULT_OK);
            }

            hostActivity.onBackPressed();
        }
    };

    /**
     * 管件定位
     */
    View.OnClickListener locOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (!arguments.containsKey(AccidentCheckFragment.class.getSimpleName()) && !arguments.containsKey("xy")) {
                    hostActivity.setResult(ResultCode.RESULT_PIPE_LOCATE, new Intent());
                    hostActivity.finish();

                    return;
                }

                BaseMapCallback callback;
                Dot dot = null;

                if (arguments.containsKey("xy") && !TextUtils.isEmpty(arguments.getString("xy"))) {
                    dot = GisUtil.convertDot(arguments.getString("xy"));
                }

                if (dot == null) {
                    if (graphicMap.containsKey("横坐标")) {
                        dot = new Dot(Double.valueOf(graphicMap.get("横座标")), Double.valueOf(graphicMap.get("纵座标")));
                    } else if (graphicMap.containsKey("X坐标")) {
                        dot = new Dot(Double.valueOf(graphicMap.get("X坐标")), Double.valueOf(graphicMap.get("Y坐标")));
                    } else {
                        ((BaseActivity) hostActivity).showToast("坐标无效");

                        return;
                    }
                }

                String title = graphicMap.get("编号");

                callback = new ShowMapPointCallback(getActivity(), dot.toString(),
                        TextUtils.isEmpty(title) ? "-" : title, null, -1);

                MyApplication.getInstance().sendToBaseMapHandle(callback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    private void createDialog(String key, String value, int position) {
        Intent intent = new Intent(getActivity(), ProtertyEditDialog.class);
        intent.putExtra("isFromProtertyEditActivity", true);
        intent.putExtra("key", key);
        intent.putExtra("value", value);
        intent.putExtra("position", position);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public class EmsPipeDetailActivityAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<String> arrayList;

        public EmsPipeDetailActivityAdapter(Context context, ArrayList<String> arrayList) {
            mInflater = LayoutInflater.from(context);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position >= 0 && position < arrayList.size() ? arrayList.get(position) : ":";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, parent, false);
                holder = new ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);
                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                holder.editTextView = (TextView) convertView.findViewById(R.id.asset_value_text);
                holder.editTextView.setVisibility(View.GONE);
                holder.position = position;
                convertView.setTag(holder);

                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String value = ((TextView) v.findViewById(R.id.asset_value)).getText().toString();

                        Toast.makeText(getActivity(), value, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String keyValue = arrayList.get(position);
            String[] keyValueArr = keyValue.indexOf('`') >= 0 ? keyValue.split("`") : null;
            if (keyValueArr != null) {
                if (keyValueArr.length >= 2) {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText(null != keyValueArr[1] ? keyValueArr[1] : "");
                } else {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText("");
                }
            }

            holder.editTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(holder.assetKey.getText().toString(), holder.assetValue.getText().toString(), holder.position);
                }
            });

            convertView.setTag(holder);
            return convertView;
        }

        class ViewHolder {
            public TextView assetKey;
            public TextView assetValue;
            public TextView editTextView;
            public int position;
        }
    }

}
