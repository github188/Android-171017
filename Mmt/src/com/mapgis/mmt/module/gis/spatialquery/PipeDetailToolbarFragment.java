package com.mapgis.mmt.module.gis.spatialquery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.flowreport.FlowReportActivity;
import com.mapgis.mmt.module.gis.investigation.AttributeEditActivity;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxTablesInfo;
import com.mapgis.mmt.module.relevance.RelevanceExplorerActivity;
import com.repair.auxiliary.pointquery.module.auxlist.AuxListActivity;

import java.util.HashMap;

/**
 * 增加属性编辑功能
 */
public class PipeDetailToolbarFragment extends Fragment {

    protected LinearLayout pipeDetailToolbarLeftLayout;
    protected ImageView pipeDetailToolbarLeftImage;
    protected TextView pipeDetailToolbarLeftText;

    protected LinearLayout pipeDetailToolbarRightLayout;
    protected ImageView pipeDetailToolbarRightImage;
    protected TextView pipeDetailToolbarRightText;

    protected LinearLayout attrEditLayout;

    protected Intent activityIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment pf = getParentFragment();
        if (pf instanceof EventCallback) {
            this.eventCallback = (EventCallback) pf;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activityIntent = getActivity().getIntent();

        View view = inflater.inflate(R.layout.pipe_detail_toolbar, null);

        try {
            pipeDetailToolbarLeftLayout = (LinearLayout) view.findViewById(R.id.pipeDetailToolbarLeftLayout);
            pipeDetailToolbarLeftImage = (ImageView) view.findViewById(R.id.pipeDetailToolbarLeftImage);
            pipeDetailToolbarLeftText = (TextView) view.findViewById(R.id.pipeDetailToolbarLeftText);

            pipeDetailToolbarRightLayout = (LinearLayout) view.findViewById(R.id.pipeDetailToolbarRightLayout);
            pipeDetailToolbarRightImage = (ImageView) view.findViewById(R.id.pipeDetailToolbarRightImage);
            pipeDetailToolbarRightText = (TextView) view.findViewById(R.id.pipeDetailToolbarRightText);

            // 临时事件
            if (Product.getInstance().hasNavFunction("临时事件")) {
                pipeDetailToolbarLeftLayout.setVisibility(View.VISIBLE);
                pipeDetailToolbarLeftText.setText("临时事件");
                pipeDetailToolbarLeftImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), FlowReportActivity.class);
                        intent.putExtras(activityIntent);
                        startActivity(intent);
                        MyApplication.getInstance().startActivityAnimation(getActivity());
                    }
                });

                MyApplication.getInstance().putConfigValue("GISQqueryEventReportHide","1");
            }

            // 多媒体查看
            String DeviceMediaExplore = MyApplication.getInstance().getConfigValue("DeviceMediaExplore").trim();

            if (!BaseClassUtil.isNullOrEmptyString(DeviceMediaExplore) && DeviceMediaExplore.equalsIgnoreCase("true")) {
                pipeDetailToolbarRightLayout.setVisibility(View.VISIBLE);
                pipeDetailToolbarRightImage.setImageResource(R.drawable.icon_cameral);
                pipeDetailToolbarRightText.setText("查看照片");
                pipeDetailToolbarRightImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seePipePhoto();
                    }
                });
            }

            // 巡线上报
            if (Product.getInstance().hasNavFunction("巡线上报")) {
                pipeDetailToolbarLeftLayout.setVisibility(View.VISIBLE);
                pipeDetailToolbarLeftText.setText("巡线上报");
                pipeDetailToolbarLeftImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        patrolReport();
                    }
                });

                MyApplication.getInstance().putConfigValue("GISQqueryEventReportHide","1");
            }

            attrEditLayout = (LinearLayout) view.findViewById(R.id.attrEditLayout);

            if (MyApplication.getInstance().getConfigValue("DeviceAttrEdit", 0) > 0) {
                attrEditLayout.setVisibility(View.VISIBLE);
                attrEditLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //对绍兴燃气属性编辑做特殊处理
                        String attrEditActivity = MyApplication.getInstance().getConfigValue("attrEditActivity");
                        Intent intent;
                        if (BaseClassUtil.isNullOrEmptyString(attrEditActivity)) {
                            intent = new Intent(getActivity(), AttributeEditActivity.class);
                        } else {
                            try {
                                Class activity = Class.forName(attrEditActivity);
                                intent = new Intent(getActivity(), activity);
                            } catch (Exception ex) {
                                intent = new Intent(getActivity(), AttributeEditActivity.class);
                            }
                        }
                        intent.putExtras(activityIntent);
                        startActivity(intent);
                        MyApplication.getInstance().startActivityAnimation(getActivity());
                    }
                });
            } else {
                attrEditLayout.setVisibility(View.GONE);
            }

            // 事件上报
            LinearLayout eventReportLayout = (LinearLayout) view.findViewById(R.id.deviceDetailToolbarEventReport);
            eventReportLayout.setVisibility(View.VISIBLE);
            eventReportLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (eventCallback != null) {
                        eventCallback.report();
                    }
                }
            });
            //点击查询页面上报事件按钮控制
            accessControl(eventReportLayout, "GISQqueryEventReportHide");

            // 导航
            LinearLayout navigationLayout = (LinearLayout) view.findViewById(R.id.deviceDetailToolbarNavigation);
            // 合肥经客户要求，去掉导航功能
            if (getActivity().getPackageName().contains("com.project.hefei")) {
                navigationLayout.setVisibility(View.GONE);
            } else {
                navigationLayout.setVisibility(View.VISIBLE);
                navigationLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (eventCallback != null) {
                            eventCallback.navigate();
                        }
                    }
                });

                //点击查询页面地图导航按钮控制
                accessControl(navigationLayout, "GISQqueryNavigationHide");
            }

            ViewGroup parent = (ViewGroup) attrEditLayout.getParent();
            boolean hasVisible = false;

            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);

                if (!(child instanceof ViewGroup))
                    continue;

                if (child.getVisibility() == View.VISIBLE) {
                    hasVisible = true;

                    break;
                }
            }

            if (!hasVisible) {
                getActivity().findViewById(R.id.frag_pipe_detail_toolbar).setVisibility(View.GONE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    private void accessControl(View view, String omsConfigKey) {
        view.setVisibility(View.VISIBLE);
        long isHideFlag = MyApplication.getInstance().getConfigValue(omsConfigKey, 0);
        if (isHideFlag == 1) {
            view.setVisibility(View.GONE);
        }
    }

    public void queryAuxData(AuxTablesInfo auxTablesInfo, HashMap<String, String> graphicMap, String layerID) {
        try {
            Intent intent = new Intent(getActivity(), AuxListActivity.class);
            intent.putExtra("auxTablesInfo", new Gson().toJson(auxTablesInfo));
            intent.putExtra("graphicMap", graphicMap);
            intent.putExtra("layerID", layerID);
            startActivity(intent);
        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleAuxView(boolean show, final AuxTablesInfo auxTablesInfo, final HashMap<String, String> graphicMap, final String layerID) {
        if (show) {
            View pipeDetailToolbarAuxDataBtn = getActivity().findViewById(R.id.pipeDetailToolbarAuxDataBtn);
            pipeDetailToolbarAuxDataBtn.setVisibility(View.VISIBLE);
            pipeDetailToolbarAuxDataBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    queryAuxData(auxTablesInfo, graphicMap, layerID);
                }
            });
        } else {
            View pipeDetailToolbarAuxDataBtn = getActivity().findViewById(R.id.pipeDetailToolbarAuxDataBtn);
            pipeDetailToolbarAuxDataBtn.setVisibility(View.GONE);
            pipeDetailToolbarAuxDataBtn.setOnClickListener(null);
        }
    }

    /**
     * 多媒体查看点击事件
     */
    private void seePipePhoto() {

        if (MobileConfig.MapConfigInstance == null) {
            Toast.makeText(getActivity(), "请配置手持参数信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (BaseClassUtil.isNullOrEmptyString(MobileConfig.MapConfigInstance.VectorService)) {
            Toast.makeText(getActivity(), "请配置<VectorService>节点信息", Toast.LENGTH_SHORT).show();
            return;
        }

        @SuppressWarnings("unchecked")
        HashMap<String, String> map = (HashMap<String, String>) activityIntent.getSerializableExtra("graphicMap");

        if (map == null || map.size() == 0) {
            Toast.makeText(getActivity(), "设备不含有属性信息", Toast.LENGTH_SHORT).show();
            return;
        }

        String layerName = map.get("$图层名称$");

        if (BaseClassUtil.isNullOrEmptyString(layerName)) {
            Toast.makeText(getActivity(), "设备不含有<图层名称>", Toast.LENGTH_SHORT).show();
            return;
        }

        String no = map.get("编号");
        String guid = map.get("GUID");

        if (BaseClassUtil.isNullOrEmptyString(no) && BaseClassUtil.isNullOrEmptyString(guid)) {
            Toast.makeText(getActivity(), "设备中不含有<编号>或<GUID>属性,或者属性值为空，不能使用该功能", Toast.LENGTH_SHORT).show();

            return;
        }

        Intent intent = new Intent(getActivity(), RelevanceExplorerActivity.class);
        intent.putExtra("layerName", layerName);

        if (!BaseClassUtil.isNullOrEmptyString(no)) {
            intent.putExtra("fieldName", "编号");
            intent.putExtra("deviceKey", no);
        } else {
            intent.putExtra("fieldName", "GUID");
            intent.putExtra("deviceKey", guid);
        }

        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(getActivity());

    }

    /**
     * 巡线上报点击事件
     */
    private void patrolReport() {
        Class<?> patrolReportActivity = ActivityClassRegistry.getInstance().getActivityClass(ActivityAlias.PATROL_REPORT_ACTIVITY);

        if (patrolReportActivity == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        HashMap<String, String> map = (HashMap<String, String>) activityIntent.getSerializableExtra("graphicMap");

        Intent intent = new Intent(getActivity(), patrolReportActivity);

        intent.putExtra("selectCoordinate", activityIntent.getStringExtra("xy"));
        intent.putExtra("address", GisUtil.getPlaceField(map));
        intent.putExtra("identityField", GisUtil.getIdentityField(map));

        startActivity(intent);

        MyApplication.getInstance().startActivityAnimation(getActivity());
    }

    public interface EventCallback {
        void report();

        void navigate();
    }

    private EventCallback eventCallback;
}
