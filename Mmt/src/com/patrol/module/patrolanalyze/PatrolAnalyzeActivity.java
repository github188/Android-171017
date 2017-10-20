package com.patrol.module.patrolanalyze;

import android.graphics.Point;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PatrolAnalyzeActivity extends BaseActivity implements View.OnClickListener {

    private final String[] patrolTimes = {"昨天", "今天", "上周", "本周", "上月", "本月"};

    private PullToRefreshListView mPullToRefreshListView;
    private PatrolAnalyzeAdapter adapter;
    private PopupWindow popupWindow;
    private TextView spTime;
    private TextView spAddress;

    private ArrayList<AnalyzeResultBean.ResultBody> mWorkStatics;
    private String[] mStationArray;
    private String dateFrom;
    private String dateTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrol_analyze);

        initView();
        initData();
    }

    private void getStations() {
        new MmtBaseTask<Void, Void, String>(this, false, "正在加载站点信息，请稍后...") {
            @Override
            protected String doInBackground(Void... params) {
                String url = new StringBuilder().append(ServerConnectConfig.getInstance().getBaseServerPath())
                        .append("/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/FetchPlanStationList?")
                        .append("userID=").append(MyApplication.getInstance().getUserId())
                        .append("&sysType=").append("业务系统")
                        .append("&_token=").append(new Random().nextInt(900) + 500)
                        .append("&_=").append(System.currentTimeMillis()).toString();
                return NetUtil.executeHttpGet(url);
            }

            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                mStationArray = new Gson().fromJson(s, AddressBean.class).getMe;
                if (mStationArray != null && mStationArray.length != 0) {
                    spAddress.setText(mStationArray[0]);
                    getWorkStatics();
                } else {
                    showToast("站点为空");
                }
            }
        }.execute();
    }

    /**
     * 获取巡检工作量统计结果
     */
    private void getWorkStatics() {
        String station = spAddress.getText().toString().trim();
        if (BaseClassUtil.isNullOrEmptyString(station) || "-".equals(station)) {
            showToast("请先选择站点");
        }
        new MmtBaseTask<String, Void, String>(this, false, "正在加载工作量统计信息...") {
            @Override
            protected String doInBackground(String... params) {
                // /langfang/CityInterface/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/PersonnelWorkReport?
                // station=&dateFrom=2017-5-22%2000:00:00&dateTo=2017-5-22%2023:59:59&sysType=%E4%B8%9A%E5%8A%A1%E7%B3%BB%E7%BB%9F
                // &_token=832&_=1495424439933
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/PersonnelWorkReport?";
                initDate();  // 获取时间
                StringBuilder sb = new StringBuilder(url);
                sb.append("station=").append(params[0])
                        .append("&sysType=").append("业务系统")
                        .append("&dateFrom=").append(dateFrom)
                        .append("&dateTo=").append(dateTo)
                        .append("&_token=").append(new Random().nextInt(1000))
                        .append("&_=").append(System.currentTimeMillis());

                /*String userName = "";
                String path = url + "station=" + params[0] + "&userName=" + userName + "&dateFrom=" + dateFrom + "&dateTo=" + dateTo;*/
                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onPostExecute(String s) {
                mPullToRefreshListView.onRefreshComplete();
                super.onPostExecute(s);
            }

            @Override
            protected void onSuccess(String result) {
                Toast.makeText(PatrolAnalyzeActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                ArrayList<AnalyzeResultBean.ResultBody> getMe = new Gson().fromJson(result, AnalyzeResultBean.class).getMe;
                if (result.contains("list")) {
                    mWorkStatics = getMe.get(0).list;
                } else {
                    mWorkStatics = getMe;
                }

                if (mWorkStatics == null || mWorkStatics.size() == 0) {
                    showToast("暂无数据");
                    return;
                }

                if (adapter == null) {
                    adapter = new PatrolAnalyzeAdapter(getApplicationContext(), mWorkStatics);
                    mPullToRefreshListView.setAdapter(adapter);
                } else {
                    adapter.setList(mWorkStatics);
                    adapter.notifyDataSetChanged();  // 通知ListView并刷新
                }
            }
        }.execute(station);
    }

    public void initDate() {
        String itemSelected = spTime.getText().toString().trim();
        Calendar today = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        switch (itemSelected) {
            case "昨天":
                today.add(Calendar.DAY_OF_MONTH, -1);
                dateTo = sdf.format(today.getTime());
                dateFrom = sdf.format(today.getTime());
                break;
            case "上周":
                today.add(Calendar.DAY_OF_MONTH, 0 - today.get(Calendar.DAY_OF_WEEK));
                dateTo = sdf.format(today.getTime());
                today.add(Calendar.DAY_OF_MONTH, -6);
                dateFrom = sdf.format(today.getTime());
                break;
            case "本周":
                today.add(Calendar.DAY_OF_MONTH, 2 - today.get(Calendar.DAY_OF_WEEK));
                dateFrom = sdf.format(today.getTime());
                today.add(Calendar.DAY_OF_MONTH, 6);
                dateTo = sdf.format(today.getTime());
                break;
            case "上月":
                today.add(Calendar.DAY_OF_MONTH, 0 - today.get(Calendar.DAY_OF_MONTH));
                dateTo = sdf.format(today.getTime());
                today.add(Calendar.DAY_OF_MONTH, 1 - today.get(Calendar.DAY_OF_MONTH));
                dateFrom = sdf.format(today.getTime());
                break;
            case "本月":
                today.add(Calendar.DAY_OF_MONTH, 1 - today.get(Calendar.DAY_OF_MONTH));
                dateFrom = sdf.format(today.getTime());
                today.add(Calendar.MONTH, 1);
                today.add(Calendar.DAY_OF_MONTH, -1);
                dateTo = sdf.format(today.getTime());

                break;
//            case "自定义":
//                // 暂时不考虑
//                break;
            case "今天":
            default:
                dateFrom = sdf.format(today.getTime()) + " 00:00:00";
                dateTo = sdf.format(today.getTime()) + " 23:59:59";
                break;
        }
    }

    private void initView() {
        spTime = (TextView) findViewById(R.id.patrolTime);
        spAddress = (TextView) findViewById(R.id.patrolAddress);
        ImageButton baseActionBarImageView = (ImageButton) findViewById(R.id.baseActionBarImageView);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.mPullToRefreshListView);

        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");

        String label = DateUtils.formatDateTime(PatrolAnalyzeActivity.this, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        mPullToRefreshListView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);

        findViewById(R.id.layoutTimeRange).setOnClickListener(this);
        findViewById(R.id.layoutAddress).setOnClickListener(this);
        baseActionBarImageView.setOnClickListener(this);
    }

    private void initData() {
        // 设置时间和区域的默认值
        spTime.setText(patrolTimes[1]);

        mWorkStatics = new ArrayList<>();
        adapter = new PatrolAnalyzeAdapter(getApplicationContext(), mWorkStatics);
        mPullToRefreshListView.setAdapter(adapter);
        mPullToRefreshListView.setRefreshing();

        getStations();   // 刷新区域下拉列表

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                getWorkStatics();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    /**
     * 更新数据
     */
    private void updateData() {
        mPullToRefreshListView.setRefreshing();
        getWorkStatics();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.layoutAddress) {
            if (mStationArray != null && mStationArray.length > 0) {
                showPopupWindow(spAddress, Arrays.asList(mStationArray));  // 显示区域列表
            }
        } else if (id == R.id.layoutTimeRange) {
            showPopupWindow(spTime, Arrays.asList(patrolTimes));  // 显示时间列表
        } else if (id == R.id.baseActionBarImageView) {
            finish();  // 退出巡检工作量统计界面
            // 执行退出动画 只能在活动关闭之后执行此动画
            // overridePendingTransition(in,out);
            MyApplication.getInstance().finishActivityAnimation(this);
        }
    }

    private void showPopupWindow(final TextView v, final List<String> list) {
        ListView listTime = (ListView) View.inflate(this, R.layout.pupup_view, null);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.popup_item_view, list);
        listTime.setAdapter(adapter);

        listTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                v.setText(list.get(position));
                updateData();   // 更新数据
                popupWindow.dismiss();
            }
        });

        popupWindow = new PopupWindow(this);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);  // 设置popupwindow的以外区域可以点击
        popupWindow.setFocusable(true);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);  // 获取屏幕的大小
        popupWindow.setWidth(point.x / 2);
        popupWindow.setHeight(-2);
        popupWindow.setContentView(listTime);

        // 设置弹出窗体的显示位置
        int[] location = new int[2]; // 分别存放x, y
        spTime.getLocationOnScreen(location); // 坐标
        // 显示气泡窗体
        if (v.getId() == R.id.patrolTime) {
            popupWindow.showAsDropDown(v, 0 - location[0], 0);
        } else if (v.getId() == R.id.patrolAddress) {
            popupWindow.showAsDropDown(v, point.x / 2 - location[0], 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }
}
