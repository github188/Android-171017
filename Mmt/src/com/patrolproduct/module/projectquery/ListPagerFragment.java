package com.patrolproduct.module.projectquery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.patrolproduct.module.projectquery.queryutil.QueryLayerUtil;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.featureservice.FeaturePagedResult;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class ListPagerFragment extends Fragment {
    /*
     * 查询结果
     */
    private FeaturePagedResult featurePagedResult;
    /*
     * 查询条件
     */
    private String where = "";

    /*
     * 图层名称
     */
    private String layerName;
    private final static String LAYER_NAME = "layerName";

    private PullToRefreshListView listViewResult;
    private ListPagerAdapter adapter;

    private ArrayList<Feature> listFeature;

    private QueryLayerUtil queryLayerUtil;

    private static ProgressDialog progressDialog;

    private int currentPage = 1;

    public ListPagerFragment() {
        // Required empty public constructor
    }

    public static ListPagerFragment newInstance(String layerName) {
        Bundle bundle = new Bundle();
        bundle.putString(LAYER_NAME, layerName);
        ListPagerFragment fragment = new ListPagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public PullToRefreshListView getListViewResult(){
        return listViewResult;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        if (!EventBus.getDefault().isRegistered(this)){
//            EventBus.getDefault().register(this);
//        }
        View view = inflater.inflate(R.layout.fragment_pager_list, container, false);

        if (getArguments() != null) {
            layerName = getArguments().getString(LAYER_NAME);
        }

        initView(view);

        return view;
    }

    private void initView(View view) {
        listViewResult = (PullToRefreshListView) view.findViewById(R.id.listViewResult);
        listViewResult.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        // 下拉刷新 文本提示
        listViewResult.getLoadingLayoutProxy(true, false).setPullLabel(
                "下拉刷新");
        listViewResult.getLoadingLayoutProxy(true, false)
                .setRefreshingLabel("正在刷新");
        listViewResult.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("放开以刷新");

        // 上拉加载更多时的提示文本设置
        listViewResult.getLoadingLayoutProxy(false, true).setPullLabel(
                "上拉加载");
        listViewResult.getLoadingLayoutProxy(false, true)
                .setRefreshingLabel("正在加载...");
        listViewResult.getLoadingLayoutProxy(false, true)
                .setReleaseLabel("放开以加载");

        queryLayerUtil = new QueryLayerUtil(getActivity(), MyApplication.getInstance().mapGISFrame);

        listFeature = new ArrayList<>();

        adapter = new ListPagerAdapter(getActivity(), listFeature);

        listViewResult.setAdapter(adapter);

        listViewResult.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            /**
             * 下拉刷新
             */
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity()
                                .getApplicationContext(), System
                                .currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                listViewResult.getLoadingLayoutProxy(true, false)
                        .setLastUpdatedLabel(label);
                listViewResult.getLoadingLayoutProxy(false, true)
                        .setLastUpdatedLabel(label);
                refreshData();
            }

            /**
             * 上拉加载
             */
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//                if (currentPage>= featurePagedResult.getPageCount()){
//                    listViewResult.onRefreshComplete();
//                    ((QueryProjectActivity)getActivity()).showToast("数据全部加载完毕");
//                    return;
//                }
//                currentPage++;
//                // 自动为用户添加下一页数据
//                if (featurePagedResult != null){
//                    listFeature.addAll(featurePagedResult.getPage(currentPage));
//                }
//                adapter.notifyDataSetChanged();
//                // 刷新完毕，这个时候不显示ProgressDialog对话框
//                listViewResult.onRefreshComplete();
            }
        });

        /*
         * 当用户滑动列表到最后一条时，自动加载下一页数据
         */
        listViewResult.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                if (currentPage>= featurePagedResult.getPageCount()){
                    listViewResult.onRefreshComplete();
                    ((QueryProjectActivity)getActivity()).showToast("数据全部加载完毕");
                    return;
                }
                currentPage++;
                // 自动为用户添加下一页数据
                listFeature.addAll(featurePagedResult.getPage(currentPage));
                adapter.notifyDataSetChanged();
                // 刷新完毕，这个时候不显示ProgressDialog对话框
                listViewResult.onRefreshComplete();
            }
        });

        // 点击事件
        listViewResult.setOnItemClickListener(onItemClickListener);

//        listViewResult.setRefreshing(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
    }

    private void initData() {
        listViewResult.setRefreshing(true);
    }

    /**
     * 获取数据
     */
    private void refreshData() {
        if (BaseClassUtil.isNullOrEmptyString(layerName)) {
            listViewResult.onRefreshComplete();
            dismissProgressDialog();
            ((QueryProjectActivity) getActivity()).showToast("图层名称不能为空");
            return;
        }

        String key = ((QueryProjectActivity) getActivity()).getFragemnt().getKeyValue();

        queryLayerUtil.asyncQueryLayerData(layerName, key, new QueryLayerUtil.QueryLayerCallBack() {
            @Override
            public void onHandData(FeaturePagedResult featurePagedResult) {
                ListPagerFragment.this.featurePagedResult = featurePagedResult;
                if (featurePagedResult == null) {
                    listViewResult.onRefreshComplete();
                    ((QueryProjectActivity) getActivity()).showToast("查询失败");
                    return;
                }

                if (featurePagedResult.getPageCount() == 0 && ListPagerFragment.this.getUserVisibleHint()) {
                    ((QueryProjectActivity) getActivity()).showToast("暂无" + layerName + "的信息");
                }
                // 初始化当前显示的页面为0；
                currentPage = 1;
                listFeature.clear();
                listFeature.addAll(featurePagedResult.getPage(currentPage));
                adapter.notifyDataSetChanged();
                listViewResult.onRefreshComplete();
            }
        });
    }

    /**
     * ListView每一项的点击事件
     */
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), PipeDetailActivity.class);
            // 包含头部和尾部
            Graphic itemClickGraphic = listFeature.get(position-1).toGraphics(true).get(0);

            HashMap<String, String> graphicMap = new LinkedHashMap<>();

            for (int m = 0; m < itemClickGraphic.getAttributeNum(); m++) {
                graphicMap.put(itemClickGraphic.getAttributeName(m), itemClickGraphic.getAttributeValue(m));
            }

            intent.putExtra("xy", itemClickGraphic.getCenterPoint().toString());
            intent.putExtra("graphicMap", graphicMap);
            intent.putExtra("graphicMapStr", new Gson().toJson(graphicMap));
            intent.putExtra("layerName", layerName);

            startActivityForResult(intent, 0);

            MyApplication.getInstance().startActivityAnimation(getActivity());
        }
    };

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }

        if (progressDialog.isShowing()) {
            return;
        }

        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }
        progressDialog.dismiss();
    }

    /**
     * 接收通知并刷新数据
     * @param bool 是否刷新
     */
    @Subscribe
    public void postRefreshData(boolean bool){
        if (bool){
            listViewResult.setRefreshing(true);
            Toast.makeText(getActivity(),"刷新"+layerName+"数据",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}