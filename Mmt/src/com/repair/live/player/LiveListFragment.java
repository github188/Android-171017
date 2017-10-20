package com.repair.live.player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.MmtImageLoader;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.live.entity.LiveBean;
import com.repair.live.player.widget.DividerGridItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 直播列表
 */
public class LiveListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FrameLayout mEmptyDataLayout;
    private TextView mEmptyText;

    private List<LiveBean> mLiveList;
    private LiveAdapter mAdapater;

    private MmtBaseTask<String, Void, String> mBaseTask;
//    private LinearLayoutManager mLinearLayoutManager;

    private GridLayoutManager mGridLayoutManager;

    public LiveListFragment() {
    }

    public static LiveListFragment newInstance() {
        return new LiveListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO 获取bundle传递的参数
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_list, container, false);

        initByfindView(view);
        initListener();
        initData();

        return view;
    }

    /**
     * 初始化控件
     */
    private void initByfindView(View view) {
        // 初始化RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mGridLayoutManager = new GridLayoutManager(getActivity(),2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);

        mEmptyDataLayout = (FrameLayout) view.findViewById(R.id.emptyData);
        mEmptyText = (TextView) view.findViewById(R.id.tv_empty_data);

        mEmptyDataLayout.setVisibility(View.GONE);
    }

    /**
     * 初始化监听事件
     */
    private void initListener() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    /**
     * 初始化界面数据
     */
    private void initData() {
        mLiveList = new ArrayList<>();
        mAdapater = new LiveAdapter(mLiveList);
        mRecyclerView.setAdapter(mAdapater);

        mSwipeRefreshLayout.setRefreshing(true);
        refreshData();
    }

    private boolean isCanceled = true;

    /**
     * 刷新数据
     */
    private void refreshData() {
        if (!isCanceled && mBaseTask != null) {
            mBaseTask.cancel(true);
        }
        mBaseTask = new MmtBaseTask<String, Void, String>(getActivity()) {

            @Override
            protected void onPreExecute() {
                mEmptyDataLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            protected String doInBackground(String... params) {
                isCanceled = false;
                // TODO 请求网络数据
                String url = ServerConnectConfig.getInstance().getMobileBusinessURL() +
                        "/LiveREST.svc/GetIsLiveList";
                // http://192.168.1.115:9999/CityInterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST
                // /LiveREST.svc/GetIsLiveList
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    isCanceled = true;
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (BaseClassUtil.isNullOrEmptyString(s)) {
                        mEmptyDataLayout.setVisibility(View.VISIBLE);
                        mEmptyText.setText("网络异常");
                        return;
                    }

                    ResultData<LiveBean> resultData = new Gson().fromJson(s, new TypeToken<ResultData<LiveBean>>() {
                    }.getType());

                    if (resultData.DataList.size() == 0) {
                        mEmptyDataLayout.setVisibility(View.VISIBLE);
                        mEmptyText.setText("暂无直播");
                        return;
                    }

                    mEmptyDataLayout.setVisibility(View.GONE);
                    mLiveList.clear();
                    mLiveList.addAll(resultData.DataList);
                    mAdapater.notifyDataSetChanged();
                } catch (JsonSyntaxException e) {
                    Toast.makeText(getActivity(), "处理错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            protected void onCancelled(String s) {
                isCanceled = true;
            }
        };
        mBaseTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBaseTask != null) {
            mBaseTask.cancel(true);
        }
    }

    class LiveAdapter extends RecyclerView.Adapter<LiveViewHolder> {
        List<LiveBean> liveList;

        LiveAdapter(List<LiveBean> list) {
            this.liveList = list;
        }

        @Override
        public LiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LiveViewHolder(View.inflate(getActivity(), R.layout.item_live_list_view, null));
        }

        @Override
        public void onBindViewHolder(LiveViewHolder holder, final int position) {
            LiveBean liveBean = liveList.get(position);

            setImageView(holder.ivLiveKeyFrame, liveBean);
            holder.tvTitle.setText(String.format("抢修-%s", liveBean.userName));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener(v, position);
                }
            });

            holder.bottomView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemClickToDetail(v, position);
                }
            });

            holder.ibLocate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickToLocate(v, position);
                }
            });

            setItemPadding(holder,position);
        }

        /**
         * 设置布局的内边距
         */
        private void setItemPadding(LiveViewHolder holder, int position) {
            int spanCount  = mGridLayoutManager.getSpanCount();
            // topPadding
            int top = getTopPadding(position,spanCount);
            int right = getRightPadding(position,spanCount);
            int bottom = getBottomPadding(position,spanCount);
            int left = getLeftPadding(position,spanCount);

            holder.itemView.setPadding(left,top,right,bottom);
        }

        private int getLeftPadding(int position, int spanCount) {
            if (position % spanCount == 0){
                return 0;
            }
            return DimenTool.px2dip(getActivity(),4);
        }

        private int getBottomPadding(int position, int spanCount) {
            return DimenTool.px2dip(getActivity(),4);
        }

        private int getRightPadding(int position, int spanCount) {
            if ((position+1) % spanCount == 0){
                return 0;
            }
           return DimenTool.px2dip(getActivity(),4);
        }

        private int getTopPadding(int position,int spanCount) {
            if (position < spanCount){
                return 0;
            }
            return DimenTool.px2dip(getActivity(),4);
        }

        @Override
        public int getItemCount() {
            return liveList.size();
        }
    }

    public void setImageView(ImageView ib, LiveBean liveBean) {
        String userImg = liveBean.userImg;
        String preImg = liveBean.preImgUrl;
        if (!BaseClassUtil.isNullOrEmptyString(preImg)) {
            String userUrl = ServerConnectConfig.getInstance()
                    .getCityServerMobileBufFilePath()
                    + "/OutFiles/UpLoadFiles/PreImage/" + preImg;
            setImageView(ib, preImg, userUrl);
        } else if (!BaseClassUtil.isNullOrEmptyString(userImg) && BaseClassUtil.isImg(userImg)) {
            String preUrl = ServerConnectConfig.getInstance()
                    .getCityServerMobileBufFilePath()
                    + "/OutFiles/UpLoadFiles/UserImage/" + userImg;
            setImageView(ib, userImg, preUrl);
        } else {
            // default
        }
    }

    private void setImageView(ImageView ib, String userImg, String url) {
        Bitmap userIcoBitmap;
        try {
            if (ib != null) {
                if (userImg != null && userImg.length() > 0
                        && !userImg.equals("default_user.png")) {
                    File file = new File(
                            Battle360Util.getFixedPath("UserImage") + userImg);
                    if (file.exists()) {
                        userIcoBitmap = FileZipUtil.getBitmapFromFile(file);
                        ib.setImageBitmap(userIcoBitmap);
                    } else {
                        String localPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media,true)
                                + "live/preImg/";
                        MmtImageLoader.getInstance().showBitmap(url,localPath,ib);
                    }
                } else {
//                        ib.setImageResource(R.drawable.default_user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定位
     *
     * @param v        点击的布局
     * @param position 点击的索引
     */
    private void onClickToLocate(View v, int position) {
        LiveBean liveBean = mLiveList.get(position);
        if (BaseClassUtil.isNullOrEmptyString(liveBean.pos)) {
            Toast.makeText(getActivity(), "坐标无效", Toast.LENGTH_SHORT).show();
            return;
        }
        ShowMapPointCallback smpc = new ShowMapPointCallback(getActivity()
                , liveBean.pos
                , "抢修-" + liveBean.userName
                , "", 1);
        MyApplication.getInstance().sendToBaseMapHandle(smpc);
    }

    /**
     * 详情
     *
     * @param v        布局
     * @param position 索引
     */
    private void onItemClickToDetail(View v, int position) {
//        Toast.makeText(getActivity(), "进入第" + position + "个条目", Toast.LENGTH_SHORT).show();
    }

    /**
     * RecyclerView 的Item的点击事件，进入对应的直播界面
     *
     * @param v        点击的布局
     * @param position 点击的条目录的位置
     */
    private void onItemClickListener(View v, int position) {
        LiveBean liveBean = mLiveList.get(position);
        Intent intent = new Intent(getActivity(), LivePlayerActivity.class);
        // TODO 传递在直播界面所需要的参数
        intent.putExtra("rtmpUrl", ServerConnectConfig.getInstance().getRTMPLiveUrl(liveBean.streamName));
        startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(getActivity());
    }

    class LiveViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public RelativeLayout bottomView;
        public ImageView ivLiveKeyFrame;
        public TextView tvTitle;
        public TextView tvDesc;

        public ImageButton ibLocate;

        public LiveViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.bottomView = (RelativeLayout) itemView.findViewById(R.id.rl_bottom);
            this.ivLiveKeyFrame = (ImageView) itemView.findViewById(R.id.iv_live_frame);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tv_live_title);
            this.tvDesc = (TextView) itemView.findViewById(R.id.tv_live_desc);
            this.ibLocate = (ImageButton) itemView.findViewById(R.id.ib_locate);
        }
    }
}
