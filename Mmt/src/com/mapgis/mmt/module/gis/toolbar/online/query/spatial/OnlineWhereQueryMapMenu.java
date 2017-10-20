package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.Constants;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryResult;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineWhereQueryTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnlineWhereQueryMapMenu extends OnlineSpatialQueryMapMenu {
    protected String where;
    public OnlineWhereQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.showToast(mapGISFrame.getResources().getString(R.string.mapmenu_error));
            return false;
        }
        mOnlinFeatureList = new ArrayList<>();
        enterLayerSelectActivity();
        return true;
    }

    @Override
    public View initTitleView() {

        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.header_bar_plan_name, null);

        view.findViewById(R.id.tvPlanBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.resetMenuFunction();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText(R.string.online_query_title);

        view.findViewById(R.id.ivPlanDetail).setVisibility(View.VISIBLE);
        view.findViewById(R.id.ivPlanDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnlinFeatureList == null || mOnlinFeatureList.size() == 0) {
                    MyApplication.getInstance().showMessageWithHandle(mapGISFrame.getString(R.string.online_query_null));
                    return;
                }
                onlineFeatures = new OnlineFeature[mOnlinFeatureList.size()];
                mOnlinFeatureList.toArray(onlineFeatures);

                Intent intent = new Intent(mapGISFrame, OnlineWhereQueryResultActivity.class);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_data), onlineFeatures);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_datastr), new Gson().toJson(onlineFeatures));
                intent.putExtra(mapGISFrame.getString(R.string.online_query_layername), layerName);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_clickwhichindex), listener.clickWhichIndex);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_currentpage), currentPage);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_totalrcdnum), totalRcdNum);
                intent.putExtra(mapGISFrame.getString(R.string.online_query_where), where);
                mapGISFrame.startActivityForResult(intent, 0);
                MyApplication.getInstance().startActivityAnimation(mapGISFrame);
            }
        });

        return view;
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        switch (resultCode) {
            case ResultCode.RESULT_LAYER_SELECTED:
                layerName = intent.getStringExtra("layer");
                ((TextView) mapGISFrame.getCustomView().findViewById(R.id.tvTaskState)).setText(layerName + "查询");
                OnlineLayerInfo onlineLayer = MapServiceInfo.getInstance().getLayerByName(layerName);
                Intent intentcls = new Intent(mapGISFrame, OnlineWhereQueyActivity.class);
                intentcls.putExtra("layer", onlineLayer.id);
                mapGISFrame.startActivityForResult(intentcls, 0);
                break;

            case ResultCode.RESULT_WHERE_FETCHED:
                where = intent.getStringExtra("where");
                initFirstPageData();
                break;
            case ResultCode.RESULT_PIPE_LOCATE:
            case ResultCode.RESULT_PIPE_REFREASH:
                currentPage = intent.getIntExtra("page", currentPage);
                listener.clickWhichIndex = intent.getIntExtra("clickWhichIndex", listener.clickWhichIndex);

                if (intent.hasExtra("dataStr")) {
                    mOnlinFeatureList.clear();
                    List<OnlineFeature> tempList = new Gson().fromJson(intent.getStringExtra("dataStr")
                            , new TypeToken<List<OnlineFeature>>() {
                            }.getType());
                    mOnlinFeatureList.addAll(tempList);
                }
                showOnMap();
                break;
        }
        return true;
    }

    @Override
    protected void initFirstPageData(){
        new OnlineWhereQueryTask(mapGISFrame, layerName, where, "mid:1," + Constants.PAGE_ITEM_NUMBER){
            @Override
            protected void onTaskDone(OnlineQueryResult data) {
                mapView.setAnnotationListener(listener);
                totalRcdNum = data.totalRcdNum;
                // 将查询到的当前页的数据拷贝到集合中
                Collections.addAll(mOnlinFeatureList, data.features);
                showFeaturesOnMap(data.features);
            }
        }.executeOnExecutor(MyApplication.executorService);
    }
}
