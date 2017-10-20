package com.repair.reporthistory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment.OnDateSelectPositiveClick;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class PatrolEventListViewFragment extends Fragment {
    private int startIndex = 0;
    private int endIndex = 10;
    private final int pageSize = 10;
    private Boolean hasNativeData = true;
    private static PullToRefreshListView mPullRefreshListView;
    private static final int SET_DATA = 1;
    private static final int NOTIFICATION = 2;
    private static final int NOMORE_NOTIFICATION = 3;
    private static final int FAILE = 0;
    private Boolean isNotBack = false;
    private Boolean isLoadMore = false;//是否是上拉加载更多
    private String url = ServerConnectConfig.getInstance().getBaseServerPath()
            + "/services/MapgisCity_WXYH_GL_MOBILE/REST/CaseManageREST.svc/FetchPatrolList2";
    private static PatrolEventViewAdapter adapter;
    private String currentData = new SimpleDateFormat("yyyy-MM-dd")
            .format(System.currentTimeMillis());
    private String startTime;
    private String endTime;
    public static List<PatrolEventEntityTrue> parametersListTemp = new ArrayList<PatrolEventEntityTrue>();
    public static List<PatrolEventEntityTrue> parametersList = new ArrayList<PatrolEventEntityTrue>();
    private final Handler handler = new MyHandler(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isNotBack = true;
    }


    private static class MyHandler extends Handler {
        private final WeakReference<PatrolEventListViewFragment> context;

        public MyHandler(PatrolEventListViewFragment context) {
            this.context = new WeakReference<PatrolEventListViewFragment>(context);

        }

        @Override
        public void handleMessage(Message msg) {
            if (context.get() == null) {
                return;
            }
            try {
                switch (msg.what) {
                    case SET_DATA: {
                        parametersList.addAll(parametersListTemp);
                        adapter.notifyDataSetChanged();
                        parametersListTemp.clear();
                    }
                    break;

                    case NOTIFICATION: {
                        Toast.makeText(context.get().getActivity(), "没有上报记录！",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case FAILE: {
                        Toast.makeText(context.get().getActivity(), "获取数据失败！", Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
                    case NOMORE_NOTIFICATION: {
                        Toast.makeText(context.get().getActivity(), "没有更多数据！", Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;

                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mPullRefreshListView.onRefreshComplete();
            }
        }
    }

    /**
     * 将本地数据一次性取出
     *
     * @param params
     * @return
     */
    List<PatrolEventEntityTrue> fetchDataFromNative(String... params) {
        //现在上报时没有向TaskControlEntity插入数据
//        String sql = "select log.reportTime,main.data from TaskControlEntity as log "
//                + " left join ReportInBack as main on log.taskId=main.id"
//                + " where log.userId="
//                + MyApplication.getInstance().getUserId()
//                + " and log.type='"
//                + params[0] + "'";
        String sql = "select * from  ReportInBack where userId=" + MyApplication.getInstance().getUserId() + " and type='" + params[0] + "'";
//ReportInBack表没有时间字段，无法使用时间条件
//        if (params[1] != null && params[1].length() > 0) {
//            sql = sql + " and log.reportTime>='" + params[1] + "'";
//        }
//        if (params[2] != null && params[2].length() > 0) {
//            sql = sql + " and log.reportTime<='" + params[2] + "'";
//        }
        List<ReportInBackEntity> listNavite = DatabaseHelper.getInstance().queryBySql(ReportInBackEntity.class, sql);
//        List<PatrolEventEntityNavite> listNavite = DatabaseHelper.getInstance()
//                .queryBySql(PatrolEventEntityNavite.class, sql);
        List<PatrolEventEntityTrue> listNet = new ArrayList<PatrolEventEntityTrue>();
        for (ReportInBackEntity item : listNavite) {
            PatrolEventEntityTrue data = new Gson().fromJson(
                    item.getData(),
                    new TypeToken<PatrolEventEntityTrue>() {
                    }.getType());

            listNet.add(data);
        }
        return listNet;
    }

    /**
     * 从网络中每次取pagesize条数据
     *
     * @param params
     * @return
     */
    List<PatrolEventEntityTrue> fetchDataFromNet(String... params) {
        String result = NetUtil.executeHttpGet(url, "uid",
                String.valueOf(MyApplication.getInstance().getUserId()),
                "type", params[0], "startTime", params[1], "endTime",
                params[2], "startIndex", params[3], "endIndex", params[4]);
        if (result != null && result.trim().length() > 0) {
            result = result.replace("[\"[", "[[").replace("]\"]", "]]");
            result = result.replace("\\", "");

            ResultData<List<PatrolEventEntityTrue>> datatemp = new Gson().fromJson(result,
                    new TypeToken<ResultData<List<PatrolEventEntityTrue>>>() {
                    }.getType());
            if(datatemp.ResultCode>0){
                return datatemp.getSingleData();
            }
        }
        return null;
    }

    private void getDataFromNetAndSetData(String... params) {
        List<PatrolEventEntityTrue> temp = fetchDataFromNet(params);
        if (temp == null) {
            handler.sendEmptyMessage(FAILE);
        } else if (temp.size() == 0) {
            //刷新时
            if (!isLoadMore) {
                handler.sendEmptyMessage(NOTIFICATION);
            } else {
                handler.sendEmptyMessage(NOMORE_NOTIFICATION);
            }
        } else {
            parametersListTemp.addAll(temp);
            handler.sendEmptyMessage(SET_DATA);
            startIndex = startIndex + temp.size() + 1;
            endIndex = startIndex + pageSize - 1;
        }
    }

    class UpdatePlanListTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                //没有本地数据时，默认有
                if (!hasNativeData) {
                    getDataFromNetAndSetData(params);
                } else {
                    List<PatrolEventEntityTrue> nativeData = fetchDataFromNative(params);
                    if (nativeData == null || nativeData.size() == 0) {
                        hasNativeData = false;
                    }

                    if (!hasNativeData) {
                        getDataFromNetAndSetData(params);
                    } else {
                        //有本地数据时
                        hasNativeData = true;

                        //本地数据第一次加载已经全部取出,加载更多时不取本地数据
                        if (!isLoadMore) {
                            parametersListTemp.addAll(nativeData);
                        }
                        if (parametersListTemp.size() < pageSize) {
                            endIndex = pageSize - parametersList.size();
                            List<PatrolEventEntityTrue> temp = fetchDataFromNet(params);
                            if (temp != null && temp.size() > 0) {
                                parametersListTemp.addAll(temp);
                                startIndex = startIndex + temp.size() + 1;
                                endIndex = startIndex + pageSize - 1;
                            }
                        }
                        handler.sendEmptyMessage(SET_DATA);
                    }
                }
            } catch (Exception e) {

                handler.sendEmptyMessage(FAILE);
            }

            return null;
        }
    }

    public void addData() {
        new UpdatePlanListTask().execute("巡线上报", this.startTime, this.endTime,
                "" + this.startIndex, "" + this.endIndex);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patrol_reporter_event_list_view,
                null);
        ((BaseActivity) getActivity()).getBaseRightImageView().setVisibility(
                View.VISIBLE);
        ((BaseActivity) getActivity()).getBaseRightImageView()
                .setImageResource(R.drawable.icon_more);
        ((BaseActivity) getActivity()).getBaseRightImageView()
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final ListDialogFragment fragment = new ListDialogFragment(
                                "选择时间段",
                                new String[]{"全部", "本日", "本周", "自定义"});
                        fragment.show(
                                getActivity().getSupportFragmentManager(), "1");
                        fragment.setListItemClickListener(new OnListItemClickListener() {
                            @Override
                            public void onListItemClick(int arg2, String value) {
                                switch (arg2) {
                                    case 0: {
                                        PatrolEventListViewFragment.this.startTime = "";
                                        PatrolEventListViewFragment.this.endTime = "";
                                        PatrolEventListViewFragment.this.dataInit();
                                        PatrolEventListViewFragment.this.addData();
                                    }
                                    break;
                                    case 1: {
                                        PatrolEventListViewFragment.this.startTime = currentData
                                                + " 00:00:00";
                                        PatrolEventListViewFragment.this.endTime = currentData
                                                + " 23:59:59";
                                        PatrolEventListViewFragment.this.dataInit();
                                        PatrolEventListViewFragment.this.addData();
                                    }
                                    break;
                                    case 2: {
                                        PatrolEventListViewFragment.this.startTime = new SimpleDateFormat(
                                                "yyyy-MM-dd").format(System
                                                .currentTimeMillis() - 7 * 86400000)
                                                + " 00:00:00";
                                        PatrolEventListViewFragment.this.endTime = currentData
                                                + " 23:59:59";
                                        PatrolEventListViewFragment.this.dataInit();
                                        PatrolEventListViewFragment.this.addData();
                                    }
                                    break;
                                    case 3: {
                                        DateSelectDialogFragment dateSelectDialogFragment = new DateSelectDialogFragment();
                                        dateSelectDialogFragment.show(getActivity()
                                                .getSupportFragmentManager(), "2");
                                        dateSelectDialogFragment
                                                .setOnDateSelectPositiveClick(new OnDateSelectPositiveClick() {

                                                    @Override
                                                    public void setOnDateSelectPositiveClickListener(
                                                            View view,
                                                            String startDate,
                                                            String endDate,
                                                            long startTimeLong,
                                                            long endTimeLong) {
                                                        startTime = startDate
                                                                + " 00:00:00";
                                                        endTime = endDate
                                                                + " 23:59:59";
                                                        PatrolEventListViewFragment.this
                                                                .dataInit();
                                                        PatrolEventListViewFragment.this
                                                                .addData();
                                                    }
                                                });
                                    }
                                    break;
                                }

                            }
                        });
                    }
                });
        ((BaseActivity) getActivity()).getBaseTextView().setText(
                ((PatrolReportHistoryActivity) getActivity()).getListTitle());

        mPullRefreshListView = (PullToRefreshListView) view
                .findViewById(R.id.patrolEventViewList);
        adapter = new PatrolEventViewAdapter(this, mPullRefreshListView);
        mPullRefreshListView.setAdapter(adapter);

        mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel(
                "下拉刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false)
                .setRefreshingLabel("正在刷新");
        mPullRefreshListView.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("放开以刷新");
        // 上拉加载更多时的提示文本设置
        mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel(
                "上拉加载");
        mPullRefreshListView.getLoadingLayoutProxy(false, true)
                .setRefreshingLabel("正在加载...");
        mPullRefreshListView.getLoadingLayoutProxy(false, true)
                .setReleaseLabel("放开以加载");
        mPullRefreshListView
                .setOnRefreshListener(new OnRefreshListener2<ListView>() {

                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        // TODO Auto-generated method stub
                        String label = DateUtils.formatDateTime(getActivity()
                                        .getApplicationContext(), System
                                        .currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_ABBREV_ALL);
                        mPullRefreshListView.getLoadingLayoutProxy(true, false)
                                .setLastUpdatedLabel(label);
                        mPullRefreshListView.getLoadingLayoutProxy(false, true)
                                .setLastUpdatedLabel(label);
                        dataInit();
                        addData();
                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        // TODO Auto-generated method stub
                        String label = DateUtils.formatDateTime(getActivity()
                                        .getApplicationContext(), System
                                        .currentTimeMillis(),
                                DateUtils.FORMAT_SHOW_TIME
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_ABBREV_ALL);
                        mPullRefreshListView.getLoadingLayoutProxy(false, true)
                                .setLastUpdatedLabel(label);
                        isLoadMore = true;
                        addData();
                    }

                });
        ListView actualListView = mPullRefreshListView.getRefreshableView();
        registerForContextMenu(actualListView);
        actualListView.setAdapter(adapter);
        if (isNotBack) {
            mPullRefreshListView.setRefreshing(false);
            isNotBack = false;
        }
        mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                OnItemClick(arg2 - 1, "list");
            }

        });
        return view;
    }

    public void OnItemClick(int index, String flag) {
        PatrolEventDetailFragment fragment = new PatrolEventDetailFragment(
                parametersList.get(index));
        fragment.setFlag(flag);

        FragmentTransaction ft = PatrolEventListViewFragment.this
                .getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.baseFragment, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void dataInit() {
        isLoadMore = false;
        this.startIndex = 0;
        this.endIndex = 10;
        parametersListTemp.clear();
        parametersList.clear();
        adapter.notifyDataSetChanged();
    }

}