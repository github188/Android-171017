package com.repair.scada;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StationManageMapMenu extends BaseMapMenu {

    private final Context context;

    private final Set<Station> shownStations = new HashSet<>();
    private final Map<StationType, List<Station>> data = new LinkedHashMap<>(4);

    private LinearLayout containerView;
    private TextView tvTipMsg;
    private final Rect rect = new Rect();
    private int[] annotationBitmapRes;

    public StationManageMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);

        this.context = mapGISFrame;
        View bottomViewBar = initBottomBarView(R.layout.mapmenu_station_manage_bottom_view);
        this.containerView = (LinearLayout) bottomViewBar.findViewById(R.id.container_view);
        this.tvTipMsg = (TextView) bottomViewBar.findViewById(R.id.tv_tip_msg);
        this.annotationBitmapRes = new int[]{
                R.drawable.map_patrol_arrived,
                R.drawable.map_patrol_feedbacked,
                R.drawable.map_patrol_unarrived
        };

        mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                try {
                    super.mapViewClickAnnotationView(mapview, annotationview);
                    String stationName = annotationview.getCalloutTitleTextView().getText().toString();
                    String stationTypeName = annotationview.getCalloutDescriptionTextView().getText().toString();
                    if (TextUtils.isEmpty(stationName) || TextUtils.isEmpty(stationTypeName)) {
                        return;
                    }
                    for (Station station : shownStations) {
                        if (stationName.equals(station.stationName)
                                && stationTypeName.equals(station.stationTypeName)) {
                            naviToRealTime(station);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected() {
        fetchStationTypeList();
        return false;
    }

    private void naviToRealTime(Station station) {
        Intent intent = new Intent(context, ScadaWebView.class);
        intent.putExtra("Station", station.stationID);
        context.startActivity(intent);
    }

    private void fetchStationTypeList() {
        MmtBaseTask<String, Void, ArrayList<StationType>> baseTask
                = new MmtBaseTask<String, Void, ArrayList<StationType>>(mapGISFrame) {
            @Override
            protected ArrayList<StationType> doInBackground(String... params) {

//                String url = ServerConnectConfig.getInstance().getBaseServerPath()
//                        + "/rest/services/scadaserver.svc/GetStationType";
                String url = "http://192.168.16.200/cityinterface/rest/services/scadaserver.svc"
                        + "/GetStationType";

                ArrayList<StationType> results = null;
                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        return null;
                    }
                    results = new Gson().fromJson(jsonResult,
                            new TypeToken<ArrayList<StationType>>() {
                            }.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return results;
            }

            @Override
            protected void onSuccess(ArrayList<StationType> stationTypes) {
                super.onSuccess(stationTypes);
                if (stationTypes == null) {
                    Toast.makeText(context, "获取站点类型列表失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                resolveData(stationTypes);
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    private void resolveData(ArrayList<StationType> stationTypes) {
        if (stationTypes.isEmpty()) {
            containerView.setVisibility(View.GONE);
            tvTipMsg.setVisibility(View.VISIBLE);
            tvTipMsg.setText("站点类型列表为空");

        } else {
            containerView.setVisibility(View.VISIBLE);
            tvTipMsg.setVisibility(View.GONE);

            LinearLayout.LayoutParams spaceLp
                    = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            spaceLp.gravity = Gravity.CENTER;
            LinearLayout.LayoutParams checkBoxLp
                    = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            checkBoxLp.gravity = Gravity.CENTER;

            for (int i = 0, length = stationTypes.size(); i < length; i++) {
                StationType stationType = stationTypes.get(i);
                stationType.setBitmapRes(annotationBitmapRes[i % annotationBitmapRes.length]);

                data.put(stationType, null);

                if (i == 0) {
                    Space space = new Space(context);
                    containerView.addView(space, spaceLp);
                }
                CheckBox stationItem = new CheckBox(context);
                stationItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f);
                stationItem.setText(stationType.getStationTypeName());
                stationItem.setTextColor(ContextCompat.getColorStateList(context,
                        R.color.color_station_manage_selector));
                stationItem.setOnCheckedChangeListener(checkedChangeListener);
                containerView.addView(stationItem, checkBoxLp);
                Space space = new Space(context);
                containerView.addView(space, spaceLp);
            }

            // Default checked the first.
            for (int i = 0, length = containerView.getChildCount(); i < length; i++) {
                View childView = containerView.getChildAt(i);
                if (childView instanceof CheckBox) {
                    ((CheckBox) childView).setChecked(true);
                    break;
                }
            }
        }
    }

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String stationTypeName = buttonView.getText().toString();
            StationType stationType = null;
            for (StationType item : data.keySet()) {
                if (stationTypeName.equals(item.getStationTypeName())) {
                    stationType = item;
                    break;
                }
            }
            List<Station> stationList = data.get(stationType);
            if (isChecked) {
                if (stationList == null) {
                    fetchStationList(stationType);
                } else {
                    refreshShownStations(stationList, true);
                }
            } else {
                if (stationList != null) {
                    refreshShownStations(stationList, false);
                }
            }
        }
    };

    private void refreshShownStations(List<Station> stationList, boolean isAdd) {
        if (isAdd) {
            shownStations.addAll(stationList);
        } else {
            shownStations.removeAll(stationList);
        }
        refreshMap();
    }

    private void fetchStationList(final StationType stationType) {

        MmtBaseTask<String, Void, ArrayList<Station>> baseTask
                = new MmtBaseTask<String, Void, ArrayList<Station>>(context) {
            @Override
            protected ArrayList<Station> doInBackground(String... params) {

//                String url = ServerConnectConfig.getInstance().getBaseServerPath()
//                        + "/rest/services/scadaserver.svc/GetStationList?stationTypeID="
//                        + stationType.getStationTypeID();
                String url = "http://192.168.16.200/cityinterface/rest/services/scadaserver.svc"
                        + "/GetStationList?stationTypeID="
                        + stationType.getStationTypeID();

                ArrayList<Station> results = null;
                try {
                    String jsonResult = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(jsonResult)) {
                        return null;
                    }
                    results = new Gson().fromJson(jsonResult, new TypeToken<ArrayList<Station>>() {
                    }.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return results;
            }

            @Override
            protected void onSuccess(ArrayList<Station> stations) {
                super.onSuccess(stations);
                if (stations == null) {
                    Toast.makeText(context, "获取站点列表失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                data.put(stationType, stations);
                refreshShownStations(stations, true);
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    private void refreshMap() {

        mapView.getAnnotationLayer().removeAllAnnotations();
        mapView.getGraphicLayer().removeAllGraphics();

        List<Station> stationList = new ArrayList<>(shownStations);
        Collections.sort(stationList, stationComparator);

        String stationTypeId = "";
        Bitmap bitmap = null;
        boolean initialized = false;
        for (Station station : stationList) {
            if (!initialized) {
                rect.xMax = rect.xMin = station.x;
                rect.yMax = rect.yMin = station.y;
                initialized = true;
            }

            if (station.x < rect.xMin) {
                rect.xMin = station.x;
            } else if (station.x > rect.xMax) {
                rect.xMax = station.x;
            }
            if (station.y < rect.yMin) {
                rect.yMin = station.y;
            } else if (station.y > rect.yMax) {
                rect.yMax = station.y;
            }

            if (!station.stationTypeID.equals(stationTypeId)) {
                for (StationType stationType : data.keySet()) {
                    if (stationType.getStationTypeID().equals(station.stationTypeID)) {
                        bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), stationType.getBitmapRes());
                        break;
                    }
                }
                stationTypeId = station.stationTypeID;
            }
            MmtAnnotation annotation = new MmtAnnotation("", station.stationName,
                    station.stationTypeName, new Dot(station.x, station.y), bitmap);
            mapView.getAnnotationLayer().addAnnotation(annotation);
        }

        double offset = (rect.xMax - rect.xMin) / 3;
        mapView.zoomToRange(new Rect(rect.xMin - offset, rect.yMin - offset, rect.xMax + offset, rect.yMax + offset), true);
        mapView.refresh();
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("运维管理");
        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
        return view;
    }

    @Override
    public boolean onBackPressed() {
        backActivity();
        return true;
    }

    private void backActivity() {
        try {
            Class<?> navigationActivityClass = ActivityClassRegistry.getInstance().getActivityClass("主界面");
            Intent intent = new Intent(mapGISFrame, navigationActivityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            mapGISFrame.startActivity(intent);
            mapGISFrame.resetMenuFunction();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final Comparator<Station> stationComparator = new Comparator<Station>() {
        @Override
        public int compare(Station lhs, Station rhs) {
            return String.CASE_INSENSITIVE_ORDER.compare(lhs.stationTypeID, rhs.stationTypeID);
        }
    };
}
