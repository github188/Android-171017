package com.patrolproduct.module.nearbyquery;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class NearbyLayerResultFragment extends Fragment {
    public static final String ARG_RADIUS = "radius";
    public static final String ARG_LAYER_NAME = "layer_name";

    private final int mPageNumber = 20;
    private String mRadius;
    private String mLayerName;

    private PullToRefreshListView mPullToRefreshListView;
    private FrameLayout mFrameEmptyData;
    private TextView mTvEmptyData;

    private FrameLayout mLoadingData;
    private ArrayList<Feature> mFeatureResultList;
    private QueryResultAdapter mAdapter;
    private Dot mCurrentDot;                            // 当前位置
    private int mCurrentPageIndex;

    private GetDataAsyncTask mmtBaseTask;

    public NearbyLayerResultFragment() {
        // Required empty public constructor
    }

    public static NearbyLayerResultFragment newInstance(String radius, String layerName) {
        NearbyLayerResultFragment fragment = new NearbyLayerResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RADIUS, radius);
        args.putString(ARG_LAYER_NAME, layerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRadius = getArguments().getString(ARG_RADIUS);
            mLayerName = getArguments().getString(ARG_LAYER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_layer_result, container, false);

        initView(view);

        return view;
    }

    private void initView(View view) {
        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.resultRefreshListView);
        mFrameEmptyData = (FrameLayout) view.findViewById(R.id.emptyData);
        mTvEmptyData = (TextView) view.findViewById(R.id.tv_empty_data);
        mTvEmptyData.setVisibility(View.VISIBLE);
        mTvEmptyData.setTextSize(14);
        mLoadingData = (FrameLayout) view.findViewById(R.id.loadingData);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
    }

    private void initData() {
        mFeatureResultList = new ArrayList<>();
        mAdapter = new QueryResultAdapter();
        mPullToRefreshListView.setAdapter(mAdapter);

        mPullToRefreshListView.getLoadingLayoutProxy(true, false)
                .setPullLabel("下拉刷新");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false)
                .setRefreshingLabel("正在刷新...");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("释放以刷新");

        mPullToRefreshListView.getLoadingLayoutProxy(false, true)
                .setPullLabel("上拉加载");
        mPullToRefreshListView.getLoadingLayoutProxy(false, true)
                .setRefreshingLabel("正在加载...");
        mPullToRefreshListView.getLoadingLayoutProxy(false, true)
                .setReleaseLabel("释放以加载");

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 下拉刷新
                pullDownToRefresh(mLayerName, mRadius);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 上拉加载
                pullUpToRefresh();
                mPullToRefreshListView.onRefreshComplete();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feature feature = mFeatureResultList.get(position - 1);
                LinkedHashMap<String, String> map = QueryUtils.featureToMap(feature);
                QueryUtils.showElemOnMap(getActivity(), feature);
                QueryUtils.enterDetailActivity(getActivity(), map,true);
            }
        });

        mPullToRefreshListView.setRefreshing();
    }

    /**
     * 下拉刷新
     */
    public void pullDownToRefresh(String layerName, String radius) {
        try {
            mCurrentPageIndex = 0;

            mLayerName = layerName;
            mRadius = radius;

            GpsXYZ gpsXYZ = GpsReceiver.getInstance().getLastLocalLocation();

            if (gpsXYZ == null) {
                Toast.makeText(getActivity(), "未获取到当前位置,请稍后再试！", Toast.LENGTH_LONG).show();
                return;
            }
            mCurrentDot = new Dot(gpsXYZ.getX(), gpsXYZ.getY());
            if (mmtBaseTask != null) {
                mmtBaseTask.cancel(true);
            }
            mmtBaseTask = new GetDataAsyncTask();
            mmtBaseTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, ArrayList<Feature>> {
        @Override
        protected void onPreExecute() {
            mFrameEmptyData.setVisibility(View.GONE);
            mLoadingData.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<Feature> doInBackground(Void... params) {
            try {
                if ("全部".equals(mRadius)) {
                    mRadius = "-1";
                }
                return QueryUtils.searchLayerWithCircle(MyApplication.getInstance().mapGISFrame.getMapView()
                        , mLayerName
                        , mCurrentDot
                        , Double.valueOf(mRadius));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Feature> features) {
            try {
                mPullToRefreshListView.onRefreshComplete();
                if (features == null) {
                    mFrameEmptyData.setVisibility(View.VISIBLE);
                    mTvEmptyData.setText("查询失败");
                    return;
                }

                if (features.size() == 0) {
                    mFrameEmptyData.setVisibility(View.VISIBLE);
                    if ("-1".equals(mRadius) || "全部".equals(mRadius)) {
                        mTvEmptyData.setText(String.format("在地图中没有找到%s", mLayerName));
                    } else {
                        mTvEmptyData.setText(String.format("在%s米范围内没有找到%s", mRadius, mLayerName));
                    }
                    return;
                }
                mFeatureResultList.clear();
                mFeatureResultList.addAll(features);
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            if(mPullToRefreshListView.isRefreshing()){
                mPullToRefreshListView.onRefreshComplete();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mmtBaseTask != null) {
            mmtBaseTask.cancel(true);
            mmtBaseTask = null;
        }
    }

    /**
     * 上拉加载
     */
    private void pullUpToRefresh() {
        if ((mCurrentPageIndex + 1) * mPageNumber >= mFeatureResultList.size()) {
            Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentPageIndex++;
        mAdapter.notifyDataSetChanged();
    }

    public String getLayerName() {
        return mLayerName;
    }

    /**
     * 将所有的结果都显示到地图上面
     */
    public void showAllOnMap() {
        if (mFeatureResultList == null || mFeatureResultList.size() == 0){
            Toast.makeText(getActivity(),"当前结果为空！",Toast.LENGTH_SHORT).show();
            return;
        }
        QueryUtils.showAllElemOnMap(getActivity(),mFeatureResultList);
        Intent intent = new Intent(getActivity(),MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        getActivity().startActivity(intent);
    }

    /**
     * 查询结果展示的适配器
     */
    class QueryResultAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int count = mFeatureResultList.size();
            if (count >= (mCurrentPageIndex + 1) * mPageNumber) {
                count = (mCurrentPageIndex + 1) * mPageNumber;
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return mFeatureResultList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Feature feature = mFeatureResultList.get(position);
            final LinkedHashMap<String, String> attMap = QueryUtils.featureToMap(feature);

            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.query_result_item_view, null);
                viewHolder = new ViewHolder();
                viewHolder.tvIndex = (TextView) convertView.findViewById(R.id.itemIndex);
                viewHolder.tvElemNum = (TextView) convertView.findViewById(R.id.tv_elemNum);
                viewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tv_distance);
//                viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tv_Address);
                viewHolder.ibLocate = (ImageButton) convertView.findViewById(R.id.tv_locate);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvIndex.setText(String.format("%s.", position + 1));
            String elemId = attMap.get("编号");
            if (BaseClassUtil.isNullOrEmptyString(elemId)){
                elemId = attMap.get("GIS编号");
            }
            viewHolder.tvElemNum.setText(getFormatString(elemId));
            viewHolder.tvDistance.setText(QueryUtils.getFormatDistance(feature.toGraphics(false).get(0).getCenterPoint(), mCurrentDot));
//            viewHolder.tvAddress.setText(String.format("位置：%s", getFormatString(attMap.get("位置"))));
            viewHolder.ibLocate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    QueryUtils.onLocated(getActivity(), feature, attMap);
                    Intent intent = new Intent(getActivity(), MapGISFrame.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    getActivity().startActivity(intent);
                }
            });
            return convertView;
        }
    }

    /**
     * 格式化字符串 如果为空就用-替代
     */
    public static String getFormatString(String src) {
        return BaseClassUtil.isNullOrEmptyString(src) ? "-" : src;
    }

    static class ViewHolder {
        public TextView tvIndex;        // 序号
        public TextView tvElemNum;      // 设备编号
        public TextView tvDistance;     // 距离
//        public TextView tvAddress;      // 位置
        public ImageButton ibLocate;       // 定位
    }
}
