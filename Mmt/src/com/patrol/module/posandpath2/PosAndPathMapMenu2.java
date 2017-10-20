package com.patrol.module.posandpath2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.entity.TracePoint;
import com.patrol.module.posandpath2.beans.DeptBean;
import com.patrol.module.posandpath2.beans.PersonInfo;
import com.patrol.module.posandpath2.beans.UserBean;
import com.patrol.module.posandpath2.beans.UserInfo;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicText;
import com.zondy.mapgis.geometry.Dot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Comclay on 2016/10/20.
 * 位置与轨迹
 */

class PosAndPathMapMenu2 extends BaseMapMenu {
    private TextView mTvTitle;
    private TextView mTvState;
    private ProgressBar mProgressBar;
    private ImageView mIvReusltDetail;
    private View mBottomBarView;

    // 部门信息
    private ArrayList<DeptBean> mDeptList;
    // 用户信息
    private ArrayList<UserInfo> mUserInfoList;
    // 用户所在站点信息
    private String[] mUserStations;

    // 要展示在地图上的数据
    private ArrayList<UserInfo> mUserOnMaps;

    private Bitmap onLineBitmap;
    private Bitmap offLineBitmap;

    public final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    private int mCase = 0;

    private UserInfo mPosUserInfo;
    private PathAsycnTask mPathAsycnTask;

    /**
     * 设置标题
     *
     * @param title 标题
     */
    public void setTvTitle(String title) {
        mTvTitle.setText(title);
    }

    public void setTvState(String state) {
        mTvState.setText(state);
        setStateVisibility(true);
    }

    void setmPosUserInfo(UserInfo mPosUserInfo) {
        this.mPosUserInfo = mPosUserInfo;
    }

    private void setStateVisibility(boolean visibility) {
        mTvState.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    PosAndPathMapMenu2(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        mBottomBarView = initBottomBarView(com.mapgis.mmt.R.layout.today_trace_bottom_bar);
        mBottomBarView.setVisibility(View.INVISIBLE);

        initData();
        return true;
    }

    /**
     * 初始化标题栏布局
     */
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.title_posandpath_view, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 退出功能按钮
        view.findViewById(R.id.backImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 标题栏
        mTvTitle = ((TextView) view.findViewById(R.id.tvTitle));

        mTvState = ((TextView) view.findViewById(R.id.tvState));
        mTvState.setTextAppearance(mapGISFrame, R.style.default_text_small_purity);
        mTvState.setTextColor(Color.WHITE);
        mTvState.setAlpha(0.87f);

        mProgressBar = (ProgressBar) view.findViewById(R.id.loadProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mIvReusltDetail = (ImageView) view.findViewById(R.id.ivResultDetail);

        mapGISFrame.findViewById(com.mapgis.mmt.R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

        return view;
    }

    class Case {
        // 全员的位置
        public final static int CASE_ALL_PATROLLER_POSITION = 1;
        // 巡检轨迹
        public final static int CASE_PATROLLER_PATH = 1 << 1;//2
        // 用户列表
        public final static int CASE_USER_INFO_LIST_POS = 1 << 2;//4
        // 用户列表的巡检轨迹
        public final static int CASE_USER_INFO_LIST_TRACE = 1 << 3;//8
        // 用户列表
        public final static int CASE_USER_INFO_LIST = CASE_USER_INFO_LIST_POS | CASE_USER_INFO_LIST_TRACE;
    }

    public void initMapLayoutView(int mCase) {
        switch (mCase) {
            case Case.CASE_ALL_PATROLLER_POSITION:
                this.mCase = this.mCase | Case.CASE_ALL_PATROLLER_POSITION;  // 位置与轨迹
                initPosAndPathView();
                break;
            case Case.CASE_PATROLLER_PATH:
                this.mCase = this.mCase | Case.CASE_PATROLLER_PATH;   // 今日轨迹
                initPatrolPathView();
                break;
            case Case.CASE_USER_INFO_LIST_POS:
                this.mCase = this.mCase | Case.CASE_USER_INFO_LIST_POS;   // 位置
                initPosView();
                break;
            case Case.CASE_USER_INFO_LIST_TRACE:   // 轨迹
                this.mCase = this.mCase | Case.CASE_USER_INFO_LIST_TRACE;
                initPatrolPathView();
                break;
        }
    }

    /**
     * 用户列表中有定位和今日轨迹两个按钮,且这两个按钮用的是位置与轨迹界面
     * 但仍要重新初始化布局
     */
    private void initPosView() {
        initViewNormal(R.string.pos_and_path_title, View.INVISIBLE, View.INVISIBLE, View.GONE, View.INVISIBLE);
    }

    /**
     * @param titleResId            标题资源ID
     * @param progressBarVisibility 进度条的显示状态
     * @param detailVisibility      详情按钮的显示状态
     * @param stateVisibility       状态栏的显示状态
     */
    private void initViewNormal(int titleResId, int progressBarVisibility
            , int detailVisibility, int stateVisibility, int bottomVisibility) {
        mTvTitle.setText(mapGISFrame.getResources().getString(titleResId));
        mProgressBar.setVisibility(progressBarVisibility);
        mIvReusltDetail.setVisibility(detailVisibility);
        mTvState.setVisibility(stateVisibility);
        mBottomBarView.setVisibility(bottomVisibility);
    }

    /**
     * 初始化位置与轨迹布局
     */
    private void initPosAndPathView() {
        initViewNormal(R.string.pos_and_path_title, View.VISIBLE, View.INVISIBLE, View.VISIBLE, View.INVISIBLE);
//        mTvState.setText("在线:0    离线:0");
        setStateText(0, 0);
        mIvReusltDetail.setImageResource(R.drawable.detail);
        /*
         * 1,当在位置与轨迹界面时,进入到人员信息列表界面
         */
        mIvReusltDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterUserInfoListActivity();
            }
        });
    }

    private void enterUserInfoListActivity() {
        mPosUserInfo = null;
        Intent intent = new Intent(mapGISFrame, UserInfoListActivity.class);
        if (!AppManager.existActivity(UserInfoListActivity.class)) {     // 第一次启动
            intent.putParcelableArrayListExtra("userList", mUserInfoList);
            intent.putParcelableArrayListExtra("deptList", mDeptList);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }

        mapGISFrame.startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(mapGISFrame);
    }

    /**
     * 初始化巡检轨迹界面
     */
    private void initPatrolPathView() {
        initViewNormal(R.string.path_title, View.VISIBLE, View.INVISIBLE, View.VISIBLE, View.VISIBLE);
        mIvReusltDetail.setImageResource(R.drawable.date);
        /*
         * 2,当在今日轨迹界面时,让用户选择日期
         */
        mIvReusltDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mDeptList = new ArrayList<>();
        mUserOnMaps = new ArrayList<>();

        mUserInfoList = new ArrayList<>();

        onLineBitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.patrol_on);
        offLineBitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.patrol_off);
//        onLineBitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.user_online);
//        offLineBitmap = onLineBitmap;

        mCase = Case.CASE_ALL_PATROLLER_POSITION;
        initMapLayoutView(mCase);

        refreshData();
    }

    /**
     * 刷新数据
     */
    private void refreshData() {
        new MmtBaseTask<Void, Void, Boolean>(mapGISFrame) {
            @Override
            protected void onPreExecute() {
                showProgressBar(true);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return /*getStationData() &&*/ getDeptData() && getUserData();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                showProgressBar(false);

                if (!aBoolean) {
                    mapGISFrame.showToast("数据请求失败");
                    refreshPosAndPathError();
                    return;
                }

                showDefaultUsers();

//                showUsersOnMap();
            }
        }.execute();
    }

    /**
     * 获取站点信息
     */
    private boolean getStationData() {
        try {
            mDeptList.clear();

            Date time = new Date();
            String UserID = MyApplication.getInstance().getUserId() + "";
//            https://pipenet.enn.cn:8000/changsha/cityinterface/services/zondy_mapgiscitysvr_casemanage/rest/casemanagerest.svc/EventManage/GetStation?userID=1&time=Wed%20Dec%2028%2012%3A57%3A55%20GMT%2B0800%202016

            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/services/zondy_mapgiscitysvr_casemanage/rest/casemanagerest.svc/EventManage/GetStation";
            String result = NetUtil.executeHttpGet(url, "time", time.toString(), "userID", UserID);
            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return false;
            }

            JSONArray jsonArray = new JSONObject(result).getJSONArray("getMe");
            mUserStations = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                mUserStations[i] = jsonArray.getString(i);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 默认显示用户所在部门所有在线人员信息
     */
    private void showDefaultUsers() {
        mUserOnMaps.clear();
        PersonInfo perinfo;
        String department = MyApplication.getInstance().getUserBean().Department;
        int onlineCount = 0;
        for (UserInfo info : mUserInfoList) {
            perinfo = info.Perinfo;
            if (department != null && department.equals(perinfo.partment) && "1".equals(perinfo.IsOline)) {
                mUserOnMaps.add(info);
                showUserOnMap(info);
                onlineCount++;
            }
        }
        setStateText(onlineCount, 0);
    }

    /**
     * 数据刷新失败时的操作
     */
    private void refreshPosAndPathError() {

    }

    public void showUserOnMap(UserInfo userInfo) {
        String position = userInfo.point.Position;
        if (BaseClassUtil.isNullOrEmptyString(position)) {
            return;
        }
        PersonInfo personInfo = userInfo.Perinfo;

        String p[] = position.split(",");
        Dot dot = new Dot(Double.valueOf(p[0]), Double.valueOf(p[1]));

        DataBindAnnotation<UserInfo> annotation = new DataBindAnnotation<>(
                userInfo, "今日轨迹", "", dot, personInfo.IsOline.equals("1") ? onLineBitmap : offLineBitmap);
        mapView.getAnnotationLayer().addAnnotation(annotation);
        annotation.setCanShowAnnotationView(true);

        GraphicText graphicText = new GraphicText(dot, personInfo.name);

        graphicText.setColor(Color.RED);
        graphicText.setFontSize(30);
        long imageHeight = annotation.getImageHeight();
        long textHeight = graphicText.getTextHeight();
        graphicText.setAnchorPoint(new PointF(0.5f, -(float) imageHeight / textHeight - 0.3f));

        mapView.getGraphicLayer().addGraphic(graphicText);
    }

    public void showUsersOnMap(List<UserInfo> userInfoList) {
        try {
            if (!userInfoList.equals(mUserOnMaps)) {
                mUserOnMaps.clear();
                mUserOnMaps.addAll(userInfoList);
            }
            for (GraphicLayer graphicLayer : mapView.getGraphicLayers().getAllGraphicLayers()) {
                graphicLayer.removeAllGraphics();
            }
            mapView.getAnnotationLayer().removeAllAnnotations();

            int onlineCount = 0;
            for (UserInfo userInfo : userInfoList) {
                if ("1".equals(userInfo.Perinfo.IsOline)) {
                    onlineCount++;
                }
                showUserOnMap(userInfo);
            }

            setStateText(onlineCount, userInfoList.size() - onlineCount);

            mapView.setAnnotationListener(new PosMapViewAnnotationListener());

            mapView.refresh();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setStateText(int onlineCount, int offlineCount) {
        mTvState.setText(String.format("在线：%d   离线：%d", onlineCount, offlineCount));
    }

    /**
     * 将用户定位到地图上
     */
    public void showUsersOnMap() {
        showUsersOnMap(mUserOnMaps);
    }

    private String mID;
    private String mUserName;
    private String mTime;

    /**
     * 在地图上显示轨迹
     */
    public void showPathOnMap(String id, String name, String time) {
        if (BaseClassUtil.isNullOrEmptyString(time)) {
            // 如果时间为空,就默认显示今日时间
            time = mSimpleDateFormat.format(new Date());
        }
        mID = id;
        mUserName = name;
        mTime = time;
        mTvState.setText(mUserName + " " + mTime);
        ((TextView) mBottomBarView.findViewById(R.id.tvTraceSpan)).setText("-- 到 --");
        ((TextView) mBottomBarView.findViewById(R.id.tvTraceStatistics)).setText("总时长：0分钟  总里程：0公里");
        if (mPathAsycnTask != null) {
            /*if (mPathAsycnTask.isSuccess() && mTime.equals(mPathAsycnTask.getmTime())
                    && mID.equals(mPathAsycnTask.getId())) {
                mPathAsycnTask.showReset();
                showProgressBar(false);
                return;
            }*/
            mPathAsycnTask.clearMapView();
            mPathAsycnTask.cancel(true);
            mPathAsycnTask = null;
        }

        mPathAsycnTask = new PathAsycnTask(mapGISFrame, id, new PathAsycnTask.PatrolPathListener() {
            @Override
            public void onPreTask() {
                showProgressBar(true);
            }

            @Override
            public void onBackTask() {

            }

            @Override
            public void onPostTask() {
                ((TextView) mBottomBarView.findViewById(com.mapgis.mmt.R.id.tvTraceSpan)).setText(mPathAsycnTask.getSpan());
//                ((TextView) mBottomBarView.findViewById(com.mapgis.mmt.R.id.tvTraceStatistics)).setText(mPathAsycnTask.getPatrolDistance());
                showProgressBar(false);
            }
        });
        mPathAsycnTask.execute(time);

        initStatistics();
    }

    /**
     * 初始化今日轨迹中时长路程信息
     */
    private void initStatistics() {
        new MmtBaseTask<Void, Void, String>(mapGISFrame) {
            @Override
            protected void onPreExecute() {
//                mBottomBarView.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... params) {
                return getStatistics();
            }

            @Override
            protected void onPostExecute(String s) {
                if (!BaseClassUtil.isNullOrEmptyString(s)) {
                    ((TextView) mBottomBarView.findViewById(com.mapgis.mmt.R.id.tvTraceStatistics)).setText(s);
                }
            }
        }.execute();
    }

    private String getStatistics() {
        String statistics = "未获取到时长里程统计信息";
        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/FetchTraceStatistics";
        String json = NetUtil.executeHttpGet(url, "userID", mID, "start", mTime + " 00:00:00", "end", mTime + " 23:59:59");
        if (!TextUtils.isEmpty(json)) {
            ResultData<TracePoint> data = new Gson().fromJson(json, new TypeToken<ResultData<TracePoint>>() {
            }.getType());

            if (data != null && data.ResultCode > 0) {
                statistics = data.ResultMessage;
            }
        }
        return statistics;
    }

    /**
     * 选择日期的对话框
     */
    private void showDateDialog() {
        String[] arr = mTime.split("-");
        int year = Integer.valueOf(arr[0]);
        int month = Integer.valueOf(arr[1]);
        int day = Integer.valueOf(arr[2]);
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(
                mapGISFrame
                , new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(i, i1, i2);
                mTime = mSimpleDateFormat.format(calendar.getTime());
                showPathOnMap(mID, mUserName, mTime);
            }
        }
                , year
                , month - 1
                , day);
        mDatePickerDialog.setCanceledOnTouchOutside(true);
        mDatePickerDialog.show();
    }

    /**
     * 获取部门信息
     */
    private boolean getDeptData() {
        try {
            mDeptList.clear();

            Date time = new Date();
            Boolean IsAll = true;
            String UserID = MyApplication.getInstance().getUserId() + "";
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetDepartment";
            String result = NetUtil.executeHttpGet(url, "time", time.toString(), "IsAll", IsAll.toString(), "UserID", UserID);
            if (BaseClassUtil.isNullOrEmptyString(result)) {
                return false;
            }

            DeptBean[] deptBeans = new Gson().fromJson(result, new TypeToken<DeptBean[]>() {
            }.getType());

            Collections.addAll(mDeptList, deptBeans);
            // 按照部门ID排序
            Collections.sort(mDeptList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户信息
     */
    private boolean getUserData() {
        try {
//            http://192.168.191.1:9999/CityInterface/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/GetPatrolerPosition?userId=227
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc/GetPatrolerPosition";

            String result = NetUtil.executeHttpGet(url, "userId", MyApplication.getInstance().getUserId() + "");

            if (BaseClassUtil.isNullOrEmptyString(result)) return false;

            UserBean bean = new Gson().fromJson(result, UserBean.class);

            mUserInfoList.clear();
            mUserInfoList.addAll(bean.Ppoint);
            // 根据站点来过滤用户
           /* if (mUserStations != null && mUserStations.length != 0) {
                String patrolManRole = MyApplication.getInstance().getConfigValue("PatrolManRole");
                String[] manRoles = null;
                if (!BaseClassUtil.isNullOrEmptyString(patrolManRole)) {
                    manRoles = patrolManRole.split(",");
                }

                for (UserInfo userInfo : bean.Ppoint) {
                    if (exceptStation(userInfo)) {
                        // 角色中包含站点信息
                        // 在判断角色配置信息
                        if (manRoles == null || exceptRole(userInfo, manRoles)) {
                            mUserInfoList.add(userInfo);
                        }
                    }
                }
            }*/
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean exceptRole(UserInfo userInfo, String[] roles) {
        String role = userInfo.Perinfo.Role;
        for (String str : roles) {
            if (role != null && role.contains(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean exceptStation(UserInfo userInfo) {
        for (String str : mUserStations) {
            if (userInfo.Perinfo.Role != null && userInfo.Perinfo.Role.contains(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 显示滚动进度条，或者显示详情按钮
     *
     * @param isVisiable 是否显示进度条，true则显示进度条，否则显示详情按钮
     */
    public void showProgressBar(boolean isVisiable) {
        mIvReusltDetail.setVisibility(isVisiable ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(isVisiable ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        return super.onActivityResult(resultCode, intent);
    }

    @Override
    public boolean onBackPressed() {
        if ((this.mCase & Case.CASE_USER_INFO_LIST_TRACE) != 0 &&
                (this.mCase & Case.CASE_USER_INFO_LIST_POS) != 0) {   // 回退到定位
            ArrayList<UserInfo> list = new ArrayList<>();
            list.add(mPosUserInfo);
            initMapLayoutView(Case.CASE_USER_INFO_LIST_POS);
            showUsersOnMap(list);
            this.mCase -= Case.CASE_USER_INFO_LIST_TRACE;
        } else if ((this.mCase & Case.CASE_USER_INFO_LIST_TRACE) > 0 ||
                (this.mCase & Case.CASE_USER_INFO_LIST_POS) > 0) {  // 回退到用户列表
            enterUserInfoListActivity();
            initMapLayoutView(Case.CASE_ALL_PATROLLER_POSITION);
            showUsersOnMap();
            showProgressBar(false);
            this.mCase = this.mCase & ~Case.CASE_USER_INFO_LIST;
        } else if ((this.mCase & Case.CASE_PATROLLER_PATH) != 0) { // 回退到全员位置与轨迹
            initMapLayoutView(Case.CASE_ALL_PATROLLER_POSITION);
            showUsersOnMap(mUserOnMaps);
            showProgressBar(false);
            this.mCase -= Case.CASE_PATROLLER_PATH;
        } else {  // 退出该功能
            // 结束调用用户列表界面
            AppManager.finishActivity(UserInfoListActivity.class);

            mapGISFrame.resetMenuFunction();
            mapGISFrame.onBackPressed();
            this.mCase = 0;
        }
        return true;
    }
}
