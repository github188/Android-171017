package com.patrol.module;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aidl.ICityMobile;
import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.patrol.common.MyPlanUtil;
import com.patrol.common.ShowGISDetailTask;
import com.patrol.common.TaskActionListener;
import com.patrol.entity.KeyPoint;
import com.patrol.entity.TaskInfo;
import com.patrol.module.KeyPoint.PointListFragment;
import com.patrol.module.changeman.NotifyRefreshPaln;
import com.patrol.module.feedback.PlanFeedbackActivity;
import com.patrol.module.map.DeviceStatusChangedCallback;
import com.patrol.module.map.OnTaskMapChangeListener;
import com.patrol.module.myplan.PlanFragment;
import com.patrol.module.patroltrace.PatrolTraceToday;
import com.patrol.module.patroltrace.PatrolTraceTodayUtils;
import com.patrol.module.posandpath.beans.PathBean;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Rect;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检计划地图显示功能类
 */
public class MyPlanMapMenu extends BaseMapMenu implements AdapterView.OnItemClickListener, TaskActionListener {
    private NavigationItem item;
    private TaskInfo task;
    private List<TaskInfo> taskList;

    MyPlanMapMenu(MapGISFrame mapGISFrame, NavigationItem item) {
        super(mapGISFrame);

        this.item = item;

        View titleView = initTitleView();

        if (titleView != null) {
            setActionBarBg(titleView);

            this.mapGISFrame.setCustomView(titleView);
        }

        EventBus.getDefault().register(this);
    }

    private TextView tvTitle;

    //轨迹相关
    private View rightCBox;
    private PatrolTraceToday patrolTraceToday;
    private boolean isChecked = false;

    /**
     * 初始化顶部的工具栏
     *
     * @return 工具栏
     */
    @Override
    public View initTitleView() {
        try {
            if (this.item == null)
                return null;

            LinearLayout topView = (LinearLayout) mapGISFrame.findViewById(R.id.baseTopView);

            View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.patrol_main_actionbar, topView, false);

            tvTitle = (TextView) view.findViewById(R.id.baseActionBarTextView);
            tvTitle.setText(item.Function.Alias);

            rightCBox = view.findViewById(R.id.cboxTrace);

            PatrolTraceTodayUtils.hidePatrolTraceCbox(isChecked, rightCBox, rect);
            if (rightCBox instanceof CheckBox) {
                final CheckBox cBoxPartol = (CheckBox) rightCBox;
                cBoxPartol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        MyPlanMapMenu.this.isChecked = isChecked;

                        if (isChecked) {

                            new MmtBaseTask<Void, Void, PatrolTraceToday>(mapGISFrame) {
                                @Override
                                protected PatrolTraceToday doInBackground(Void... params) {
                                    if (patrolTraceToday == null || patrolTraceToday.dots == null || patrolTraceToday.dots.size() < 2) {

                                        String time = BaseClassUtil.getSystemTime("yyyy-MM-dd");
                                        String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/services/zondy_mapgiscitysvr_newpatrol/rest/newpatrolrest.svc/GetPerHisPosition" +
                                                "?OnlyWorkTime=true&OptimizeTrace=true&STTime=" + time + " 00:00:00" + "&ENDTime=" + time + " 23:59:59" + "&IDList=" + MyApplication.getInstance().getUserId();

                                        String result = NetUtil.executeHttpGet(url);
                                        if (TextUtils.isEmpty(result)) {
                                            return null;
                                        }
                                        PathBean bean = new Gson().fromJson(result, PathBean.class);
                                        if (!"true".equals(bean.rntinfo.IsSuccess)) {
                                            return null;
                                        }
                                        if (bean.Ppoint == null || bean.Ppoint.size() == 0) {
                                            return null;
                                        }
                                        if (TextUtils.isEmpty(bean.Ppoint.get(0).Ppoint)) {
                                            return null;
                                        }

                                        patrolTraceToday = new PatrolTraceToday(mapGISFrame, bean.Ppoint.get(0).Ppoint);

                                    }

                                    return patrolTraceToday;
                                }

                                @Override
                                protected void onSuccess(PatrolTraceToday patrolTraceToday) {
                                    super.onSuccess(patrolTraceToday);
                                    if (patrolTraceToday == null) {
                                        return;
                                    }
                                    if (patrolTraceToday.dots == null || patrolTraceToday.dots.size() < 2) {
                                        MyApplication.getInstance().showMessageWithHandle("今日无巡检轨迹");
                                        return;
                                    }
                                    patrolTraceToday.showTrace2Map();
                                }
                            }.mmtExecute();
                            return;
                        }
                        if (patrolTraceToday == null) {
                            return;
                        }
                        patrolTraceToday.hideTrace();
                        if (rect != null) {
                            mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100, rect.xMax + 100, rect.yMax + 100), true);
                        }
                        mapView.refresh();

                    }
                });
            }

            view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            return view;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    private PlanFragment planFragment;

    private View viewBar;

    /**
     * 启动巡检计划显示功能
     *
     * @return 是否立即关闭侧滑栏
     */
    @Override
    public boolean onOptionsItemSelected() {
        try {
            mapGISFrame.bindService(new Intent(mapGISFrame, MmtMainService.class), connection, Context.BIND_AUTO_CREATE);

            //设置底部栏显示计划状态：到位数、到位里程等
            {
                mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

                viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.my_plan_page, mapView, false);

                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);

                params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                viewBar.setLayoutParams(params1);

                mapView.addView(viewBar);

                viewBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (task == null)
                                return;

                            PointListFragment fragment = new PointListFragment();

                            Bundle bundle = new Bundle();

                            bundle.putParcelable("task", task);

                            fragment.setArguments(bundle);

                            FragmentTransaction ft = mapGISFrame.getSupportFragmentManager().beginTransaction();

                            ft.add(R.id.otherFragment, fragment, "PointListFragment");

                            ft.show(fragment);

                            if (!fragment.isRemoving()) {
                                ft.commitAllowingStateLoss();
                            }

                            PatrolTraceTodayUtils.hidePatrolTraceCbox(isChecked, rightCBox, rect);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            // 计划Fragment
            planFragment = new PlanFragment();

            // 给计划Fragment的ListView增加点击事件
            planFragment.setOnMyItemClickListener(this);

            mapGISFrame.replaceOtherFragment(planFragment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    private ICityMobile iCityMobile;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                iCityMobile = ICityMobile.Stub.asInterface(service);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iCityMobile = null;
        }
    };

    public List<TaskInfo> refreshMyPlan() {
        try {
            taskList = this.iCityMobile.refreshMyPlan();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return taskList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        try {
            int taskID = ((TaskInfo) parent.getItemAtPosition(position)).ID;
            int index = -1;

            for (int i = 0; i < taskList.size(); i++) {
                TaskInfo info = taskList.get(i);

                if (info.ID == taskID)
                    index = i;

                //清空非当前的计划的缓存点列表
                info.Points = null;
            }

            task = iCityMobile.getTaskInfo(taskID);

            if (index >= 0)
                taskList.set(index, task);

            String title = (task.Index + 1) + "." + task.Name + (task.IsFinish == 1 ? "-完成" : "");
            tvTitle.setText(title);

            onStateChanged();

            showTaskOnMap();

            mapGISFrame.getFragment().getViewPager().setCurrentItem(position - 1);

            PatrolTraceTodayUtils.showPatrolTraceCbox(rightCBox);

            setStateHasRead();

        } catch (RemoteException ex) {
            Toast.makeText(mapGISFrame, "巡线对象过多客户端难以绘制，后台会自动判断到位", Toast.LENGTH_SHORT).show();

            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setStateHasRead() {
        if (task == null) {
            return;
        }
        if (task.ID <= 0) {
            return;
        }
        if ("已查看".equals(task.TaskState)) {
            return;
        }

        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {

                try {
                    String url = MyPlanUtil.getStandardURL() + "/SetPatroloPlanState?taskid=" + task.ID + "&taskState=已查看";
                    String ret = NetUtil.executeHttpGet(url);
                    if (TextUtils.isEmpty(ret)) {
                        return;
                    }
                    ResultWithoutData resultWithoutData = new Gson().fromJson(ret, ResultWithoutData.class);
                    if (resultWithoutData == null) {
                        return;
                    }
                    if (resultWithoutData.ResultCode <= 0) {
                        return;
                    }
                    iCityMobile.setTaskState(task.ID, "已查看");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    private OnTaskMapChangeListener listener = null;
    private Rect rect;

    /**
     * 将数据显示到地图上
     */
    private void showTaskOnMap() {
        try {
            mapView.getGraphicLayer().removeAllGraphics();

            if (listener == null)
                listener = new OnTaskMapChangeListener(mapGISFrame, mapView, this);

            listener.setTask(task);

            ((MmtMapView) mapView).setExtentChangeListener(listener);

            // 显示地图界面
            mapGISFrame.showMainFragment(true);

            // 将数据绘制到地图上
            rect = task.drawOnMap(mapView);

            // 跳转到制定外接矩形,并保留100米的空间间隙
            if (rect != null) {
                mapView.zoomToRange(new Rect(rect.xMin - 100, rect.yMin - 100, rect.xMax + 100, rect.yMax + 100), true);

                listener.ExtentChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mapView.refresh();
        }
    }

    /**
     * 退出巡检计划显示功能
     */
    @Override
    public boolean onBackPressed() {
        try {
            FragmentManager fm = mapGISFrame.getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag("PointListFragment");

            if (fragment != null && fragment.isVisible()) {
                FragmentTransaction transaction = fm.beginTransaction();

                transaction.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);

                transaction.remove(fragment);

                transaction.commitAllowingStateLoss();

                PatrolTraceTodayUtils.showPatrolTraceCbox(rightCBox);
                return true;
            }

            PatrolTraceTodayUtils.hidePatrolTraceCbox(isChecked, rightCBox, rect);

            fragment = fm.findFragmentById(R.id.otherFragment);

            if (!fragment.isVisible()) {//地图界面则返回列表界面；列表界面则退出此功能
                tvTitle.setText(item.Function.Alias);


                ((MmtMapView) mapView).setExtentChangeListener(null);

                mapGISFrame.showMainFragment(false);

            } else {
                mapView.removeView(viewBar);

                if (listener != null)
                    listener.remove();

                task = null;
                taskList = null;
                viewBar = null;

                mapGISFrame.unbindService(connection);

                Class<?> navigationActivityClass = ActivityClassRegistry.getInstance().getActivityClass("主界面");
                Intent intent = new Intent(mapGISFrame, navigationActivityClass);
                intent.putExtra("needClearMap", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mapGISFrame.startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * 巡线完成
     */
    public void onTaskFinish() {
        try {
            FragmentManager fm = mapGISFrame.getSupportFragmentManager();

            Fragment otherFragment = fm.findFragmentById(R.id.otherFragment);

            FragmentTransaction transaction = fm.beginTransaction();

            transaction.show(otherFragment);

            transaction.commitAllowingStateLoss();

            planFragment.onTaskFinish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 到位或者反馈后的状态改变事件
     */
    public void onStateChanged() {
        try {
            if (viewBar == null || task == null)
                return;

            TextView tvPoint = (TextView) viewBar.findViewById(R.id.tvPoint);
            TextView tvLine = (TextView) viewBar.findViewById(R.id.tvLine);

            tvPoint.setText(task.getPointState());
            tvPoint.setVisibility(task.TotalSum > 0 ? View.VISIBLE : View.GONE);

            tvLine.setText(task.getLineState());
            tvLine.setVisibility(task.TotalLength > 0 ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 关键点定位
     *
     * @param kp 关键点
     */
    public void onLocate(KeyPoint kp) {
        try {
            onBackPressed();

            mapView.panToCenter(GisUtil.convertDot(kp.Position), true);

            for (Annotation annotation : mapView.getAnnotationLayer().getAllAnnotations()) {
                if (annotation instanceof MmtAnnotation && ((MmtAnnotation) annotation).info.equals(String.valueOf(kp.ID))) {
                    annotation.showAnnotationView();

                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 关键点反馈，有可能不只是点击annotation出反馈 有个列表也可以的
     *
     * @param kp 关键点
     */
    public void onFeedback(KeyPoint kp) {
        try {
            if (kp.Type == 2) {
                Toast.makeText(mapGISFrame, "管段不需要反馈", Toast.LENGTH_SHORT).show();

                return;
            }

            if (!TextUtils.isEmpty(kp.KClass) && kp.KClass.equals("0")) {
                Toast.makeText(mapGISFrame, "该巡线点仅要求到位，不需要反馈", Toast.LENGTH_SHORT).show();

                return;
            }

            Intent intent = new Intent(mapGISFrame, PlanFeedbackActivity.class);

            intent.putExtra("flowName", "巡检流程");
            intent.putExtra("nodeName", "巡检反馈");

            String defaultParam = "GIS图层:" + kp.GisLayer
                    + ";GIS编号:" + kp.FieldValue + ";GIS坐标:" + kp.Position;
            intent.putExtra("defaultParam", defaultParam);

            intent.putExtra("kp", kp);

            mapGISFrame.startActivityForResult(intent, 200);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 显示关键点详细信息，GIS属性信息
     *
     * @param kp 关键点
     */
    public void onShowGISDetail(KeyPoint kp) {
        new ShowGISDetailTask(mapGISFrame).mmtExecute(kp);
    }

    /**
     * 反馈完成的回调函数
     *
     * @param resultCode 返回码
     * @param intent     返回内容
     * @return 处理结果
     */
    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        try {
            if (resultCode == 200) {
                int id = Integer.valueOf(intent.getStringExtra("ID"));

                for (KeyPoint kp : task.Points) {
                    if (kp.ID != id)
                        continue;

                    if (kp.IsFeedback != 1)
                        task.FeedbackSum++;

                    kp.IsFeedback = 1;
                    kp.FeedbackTime = BaseClassUtil.getSystemTime();

                    MyApplication.getInstance().sendToBaseMapHandle(new DeviceStatusChangedCallback(kp));

                    iCityMobile.onKeyPointFeedback(task.ID, kp.ID);

                    break;
                }

                return true;
            } else
                return super.onActivityResult(resultCode, intent);
        } catch (Exception ex) {
            ex.printStackTrace();

            return super.onActivityResult(resultCode, intent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void notifyRefreshPlan(NotifyRefreshPaln notifyRefreshPaln) {
        try {
            if (notifyRefreshPaln == null) {
                return;
            }

            if (planFragment == null) {
                return;
            }

            if (taskList == null || taskList.size() == 0) {
                return;
            }

            onBackPressed();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            onBackPressed();

                            return false;
                        }
                    });
                }
            }, 1000);

            for (TaskInfo taskInfo : taskList) {
                if (taskInfo.ID != notifyRefreshPaln.taskID) {
                    continue;
                }

                taskList.remove(taskInfo);

                planFragment.onDataSetChanged();

                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (!intent.hasExtra("kp"))
                return;

            KeyPoint sKP = intent.getParcelableExtra("kp");
            KeyPoint kp = null;

            for (TaskInfo info : taskList) {
                if (info.ID != sKP.TaskID)
                    continue;

                info.PipeLenth = intent.getDoubleExtra("len", 0);
                info.ArriveSum = intent.getIntExtra("sum", 0);

                //确认是当前显示的任务
                if (info.ID == this.task.ID) {
                    if (sKP.Type != 2) {
                        kp = this.task.findPointByID(sKP.ID);

                        kp.IsArrive = 1;
                        kp.ArriveTime = sKP.ArriveTime;
                    } else
                        kp = sKP;
                }

                break;
            }

            if (kp == null)
                return;

            MyApplication.getInstance().sendToBaseMapHandle(new DeviceStatusChangedCallback(kp));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<KeyPoint> fetchPipeLines(int id, double xmin, double ymin, double xmax, double ymax) {
        try {
            return iCityMobile.fetchPipeLines(id, xmin, ymin, xmax, ymax);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
