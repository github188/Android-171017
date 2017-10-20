package com.mapgis.mmt.module.gis.toolbar.analyzer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.SearchHistoryFragment;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.baidu.BDAddressSearchResultCallback;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.GisAddressSearchResultCallback;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.LocatorGeocodeResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchCallback;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult;
import com.zondy.mapgis.android.mapview.MapView;

public class AddressSearchTask extends AsyncTask<String, Integer, Object> {
    private ProgressDialog loadingBar;
    private final Activity activity;

    private String where;
    private int page = 0;
    private MapView mapView;

    public AddressSearchTask(Activity activity) {
        this.activity = activity;
    }

    public AddressSearchTask(Activity activity, String where, int page) {
        this.activity = activity;
        this.page = page;
        this.where = where;
    }

    public AddressSearchTask(MapView mapView, Activity activity, String where, int page) {
        this(activity, where, page);
        this.mapView = mapView;
    }

    @Override
    protected void onPreExecute() {
        loadingBar = MmtProgressDialog.getLoadingProgressDialog(activity, "正在搜索信息");
        loadingBar.show();
    }

    @Override
    protected Object doInBackground(String... params) {
        return BDGeocoder.locFromAddressUtil(mapView, where, page);
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result == null) {
            return;
        }
        try {
            SearchHistoryFragment.getInstance().notifyDataSetChanged(where);

            if (result instanceof BDPlaceSearchResult) {// 百度结果
                PlaceSearch.SEARCH_RESULT = result;
                MyApplication.getInstance().sendToBaseMapHandle(new BDAddressSearchResultCallback((BDPlaceSearchResult) result));
                SearchHistoryFragment.getInstance().notifyDataSetChanged(where);

                if (activity instanceof AddressSearchResultActivity) {
                    AddressSearchResultActivity resultActivity = (AddressSearchResultActivity) activity;
                    resultActivity.resetBDFragmentAdapter();
                }

            } else if (result instanceof LocatorGeocodeResult) {// GIS服务器结果
                PlaceSearch.SEARCH_RESULT = result;
                MyApplication.getInstance().sendToBaseMapHandle(new GisAddressSearchResultCallback((LocatorGeocodeResult) result));
                SearchHistoryFragment.getInstance().notifyDataSetChanged(where);
            } else if (result instanceof LocalPlaceSearchResult) { // 本地db文件地名搜索结果
                if (((LocalPlaceSearchResult) result).dataList.size() > 0) { // 查询到结果时才重绘地图
                    PlaceSearch.SEARCH_RESULT = result;
                    MyApplication.getInstance().sendToBaseMapHandle(new LocalPlaceSearchCallback((LocalPlaceSearchResult) result));
                }
                SearchHistoryFragment.getInstance().notifyDataSetChanged(where);

                if (activity instanceof AddressSearchResultActivity) {
                    boolean hasNewData = ((LocalPlaceSearchResult) result).dataList.size() > 0;
                    ((AddressSearchResultActivity) activity).resetNativeFragmentAdapter(hasNewData, page);
                }
            } else {
                Toast.makeText(activity, "未查询到结果!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(activity, "查询结果异常!", Toast.LENGTH_SHORT).show();
        } finally {
            if (null != loadingBar) {
                loadingBar.dismiss();
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        }
    }
}
