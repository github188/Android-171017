package com.mapgis.mmt.module.gis;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.common.widget.customview.SelfView;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.entity.MenuItem;
import com.mapgis.mmt.module.gis.map.pager.MmtMapBottomPager;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.clearmap.ClearMapMenu;
import com.mapgis.mmt.module.gis.toolbar.gps.MyLocationMapMenu;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.net.BaseStringTask;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewLongTapListener;
import com.zondy.mapgis.geometry.Dot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapGISFrameFragment extends BackHandledFragment {
    private MapGISFrame mapGISFrame;
    private MmtMapView mapView;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;

    private ViewGroup moduleToolbar;// 其他功能进入地图后,可以进行操作的菜单栏,只提供位置,具体实现由功能自己完成

    private ViewPager viewPager;

    public LinkedList<SelfView> selfViewsList = new LinkedList<>();

    /**
     * 传感器管理器
     */
    private SensorManager sensorManager;
    /**
     * 传感器监听器
     */
    private SensorEventListener sensorEventListener;

    public MapView getMapView() {
        return mapView;
    }

    private static MapLoaderTask task = new MapLoaderTask();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mapGISFrame = (MapGISFrame) getActivity();

        View view = inflater.inflate(R.layout.mapframe_drawer, container, false);

        initMapView(view);

        initToolView(view);

        initBottomView(view);

        initDrawerView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sensorManager = (SensorManager) mapGISFrame.getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new OrientationSensorListener(mapView);

        Intent outIntent = getActivity().getIntent();
        if (outIntent.getBooleanExtra("outIntentFlag", false)) {
            OutIntentListener outIntentListener = new OutIntentListener(outIntent, mapView);
            outIntentListener.showExtent();
        } else { // 如果 不是 从 外部应用 跳转 ， 则 直接 打开 主菜单界面
            new MmtBaseTask<String,Integer,String>(mapGISFrame)
            {
                @Override
                protected String doInBackground(String... params) {
                    return null;
                }

                @Override
                protected void onSuccess(String s) {
                    try {
                        View v = ((ViewGroup) ((ViewGroup) mapGISFrame.findViewById(R.id.layoutMapToolbar)).getChildAt(0)).getChildAt(0);

                        if (((ViewGroup) v).getChildAt(1).getTag().equals("返回主页")) {
                            v.performClick();
                        } else {
                            menu = MapMenuRegistry.getInstance().getMenuInstance("返回主页", mapGISFrame);
                            menu.onOptionsItemSelected();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        menu = MapMenuRegistry.getInstance().getMenuInstance("返回主页", mapGISFrame);
                        menu.onOptionsItemSelected();
                    }

                    mapGISFrame.getBaseLeftImageView().setVisibility(View.GONE);
                }
            }.mmtExecute("");
        }

        MyApplication.getInstance().putConfigValue("MapView", mapView);

        MapLoaderTask customTask = MyApplication.getInstance().getConfigValue("mapLoaderTask", MapLoaderTask.class);

        if (customTask != null) {
            task = customTask;
        }

        if (task.getStatus() == Status.PENDING) {
            task.executeOnExecutor(MyApplication.executorService, mapView);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        // 获取方向传感器 通过SensorManager对象获取相应的Sensor类型的对象,应用在前台时候注册监听器
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    public void onPause() {
        // 应用不在前台时候销毁掉监听器
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    private void initMapView(View view) {
        try {
            mapView = new MmtMapView(mapGISFrame);
            mapView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            ((ViewGroup) view.findViewById(R.id.layoutMapRoot)).addView(mapView, 0);

            mapView.setZoomControlsEnabled(false);
            mapView.setShowNorthArrow(true);
            mapView.setMyLocationButtonEnabled(false);
            mapView.setShowLogo(false);
            mapView.setMapRotateGesturesEnabled(false);
            mapView.setShowScaleBar(false);
            mapView.setShowUserLocation(false);

            mapView.setRenderContextListener(new MmtMapViewRenderContextListener(getActivity()));
            mapView.setMapLoadListener(new MmtMapViewLoadListener(mapGISFrame));

            mapView.setAnnotationListener(new DefaultMapViewAnnotationListener());
            mapView.setLongTapListener(new MapViewLongTapListener() {

                @Override
                public boolean mapViewLongTap(PointF arg0) {
                    Dot mapPoint = mapView.viewPointToMapPoint(arg0);

                    Toast.makeText(getActivity(), mapPoint.toString(), Toast.LENGTH_LONG).show();

                    if (getActivity().getIntent().getBooleanExtra(MapGISFrame.LONG_TAG_FOR_POINT, false)) {

                        getActivity().getIntent().removeExtra(MapGISFrame.LONG_TAG_FOR_POINT);

                        Serializable action = getActivity().getIntent().getSerializableExtra("action");
                        Intent intent;

                        if (action != null && action instanceof Class<?>) {
                            intent = new Intent(getActivity(), (Class<?>) action);
                        } else {
                            intent = new Intent(getActivity().getIntent().getStringExtra("action"));
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                        intent.putExtra("location", Convert.FormatDouble(mapPoint.x) + "," + Convert.FormatDouble(mapPoint.y));

                        getActivity().startActivity(intent);
                    }
                    return false;
                }
            });

            view.findViewById(R.id.imageview_main_zoomin).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapView.zoomIn(true);
                }
            });

            view.findViewById(R.id.imageview_main_zoomout).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapView.zoomOut(true);
                }
            });

            /** *－－－地图复位按钮－－－ * **/
            view.findViewById(R.id.imageview_main_zoomfull).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapView.zoomFull();
                }
            });

            /** 显示地图模型 */
//            view.findViewById(R.id.mapViewMode).setVisibility(View.GONE);
            view.findViewById(R.id.mapViewMode).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    MapModePopupWindow popupWindow = new MapModePopupWindow(mapGISFrame, v);
//
//                    if (!popupWindow.isVisible()) {
//                        popupWindow.show();
//                    }
                    MapMenuRegistry.getInstance().getMenuInstance("图层控制", mapGISFrame).onOptionsItemSelected();
                }
            });

            mapView.initExtentChangeListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化工具栏
     */
    private void initToolView(View view) {
        moduleToolbar = (ViewGroup) view.findViewById(R.id.layoutMapModuleToolbar);

        RelativeLayout toolFrame = (RelativeLayout) view.findViewById(R.id.toolFrame);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolFrame.getLayoutParams();
        params.bottomMargin = DimenTool.dip2px(getActivity(), 10);
        toolFrame.setLayoutParams(params);
    }

    /**
     * 底部菜单栏
     */
    public BaseMapMenu menu;

    /**
     * 初始化底部菜单栏
     */
    private void initBottomView(View view) {
        viewPager = (ViewPager) view.findViewById(R.id.baseBottomPagerView);
        viewPager.setOffscreenPageLimit(5);

        /** *－－－清理屏幕－－－ * **/
        view.findViewById(R.id.mapviewClear).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClearMapMenu(mapGISFrame).onOptionsItemSelected();

                for (SelfView item : selfViewsList) {
                    mapView.removeView(item.view);
                }

                selfViewsList.clear();
            }
        });

        /** *－－－定位－－－ * **/
        view.findViewById(R.id.mapviewLocate).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyLocationMapMenu(mapGISFrame).onOptionsItemSelected();
            }
        });

        ArrayList<MenuItem> toolbarList = Product.getInstance().MapToolBars;

        if (toolbarList == null || toolbarList.size() == 0) {
            return;
        }

        ViewGroup parentGroup = (ViewGroup) view.findViewById(R.id.layoutMapToolbar);

        LinearLayout subLayout = new LinearLayout(getActivity());
        subLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        subLayout.setMinimumHeight(DimenTool.dip2px(mapGISFrame, 50));
        subLayout.setOrientation(LinearLayout.HORIZONTAL);

        parentGroup.addView(subLayout);

        for (int i = 0; i < toolbarList.size(); i++) {
            LinearLayout group = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.mapframe_bottombar_item, null);

            group.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1));

//            ((ImageView) (group.getChildAt(0))).setImageResource(ResourceUtil.getDrawableResourceId(toolbarList.get(i).Icon,
//                    R.drawable.mapview_rect));

            ((ImageView) (group.getChildAt(0))).setImageResource(ResourceUtil.getDrawableResourceId(mapGISFrame, toolbarList.get(i).Icon));

            ((TextView) (group.getChildAt(1))).setText(toolbarList.get(i).Alias);
            group.getChildAt(1).setTag(toolbarList.get(i).Name);

            group.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    menu = MapMenuRegistry.getInstance().getMenuInstance(((ViewGroup) v).getChildAt(1).getTag().toString(),
                            mapGISFrame);

                    menu.onOptionsItemSelected();
                }
            });

            subLayout.addView(group);
        }
    }

    // 侧滑栏
    private void initDrawerView(View view) {
        if (Product.getInstance().MapMoreMenus == null || Product.getInstance().MapMoreMenus.size() <= 0) {
            return;
        }
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawerListView = (ListView) view.findViewById(R.id.rightDrawer);
        drawerListView.setCacheColorHint(0);

        drawerListView.setAdapter(new BaseAdapter() {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.mapframe_drawer_item, parent, false);

                Object object = getItem(position);
                if (object == null) {
                    return convertView;
                }
                MenuItem item = (MenuItem) object;

                //int id = ResourceUtil.getDrawableResourceId(item.Icon);
                int id = ResourceUtil.getDrawableResourceId(mapGISFrame, item.Icon);
                ImageView iv = (ImageView) convertView.findViewById(R.id.rightDrawerItemImage);

                if (id > 0) {
                    iv.setImageResource(id);
                    iv.setVisibility(View.VISIBLE);
                } else
                    iv.setVisibility(View.GONE);

                ((TextView) convertView.findViewById(R.id.rightDrawerItemText)).setText(item.Alias);

                return convertView;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public Object getItem(int position) {
                return (Product.getInstance().MapMoreMenus == null || position >= Product.getInstance().MapMoreMenus.size()) ? null : Product.getInstance().MapMoreMenus.get(position);
            }

            @Override
            public int getCount() {
                try {
                    if (Product.getInstance() != null && Product.getInstance().MapMoreMenus != null)
                        return Product.getInstance().MapMoreMenus.size();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return 0;
            }
        });

        drawerListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                menu = MapMenuRegistry.getInstance().getMenuInstance(
                        Product.getInstance().MapMoreMenus.get(arg2).Name, mapGISFrame);

                if (menu.onOptionsItemSelected()) {
                    showDrawer();
                    // showBottomBar(View.GONE);
                }
            }
        });
    }

    /**
     * 显示侧滑栏
     */
    public void showDrawer() {
        if (drawerLayout == null) {
            MyApplication.getInstance().showMessageWithHandle("请为用户关联更多菜单");
            return;
        }
        if (drawerLayout.isDrawerOpen(drawerListView)) {
            drawerLayout.closeDrawer(drawerListView);
        } else {
            drawerLayout.openDrawer(drawerListView);
        }
    }

    /**
     * 将创建好的底部滑动功能加入到地图
     *
     * @param bottomPager 底部视图
     */
    public void setBottomPager(final MmtMapBottomPager bottomPager) {
        bottomPager.addOnMap(mapGISFrame);
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    /**
     * 设置底部滑动页可见性
     */
    public void showViewPagerVisibility(int visibility) {
        if (viewPager != null) {
            viewPager.setVisibility(visibility);
            if (visibility == View.GONE) {
                viewPager.removeAllViews();
            }
        }
    }

    /**
     * 清理屏幕
     */
    public void clearMapview() {
        if (getView() != null && getView().findViewById(R.id.mapviewClear) != null) {
            getView().findViewById(R.id.mapviewClear).performClick();
        }
    }

    /**
     * 其他功能进入地图后,可以进行操作的菜单栏,只提供位置,具体实现由功能自己完成
     */
    public ViewGroup getModuleToolbar() {
        return moduleToolbar;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (menu != null && menu.onActivityResult(resultCode, intent)) {
            return;
        }

        switch (resultCode) {
            case ResultCode.APP_EXIT_OK:
                if (mapView.getDispRange() != null) {
                    MyApplication.getInstance().getSystemSharedPreferences().edit()
                            .putString("preDispRange", new Gson().toJson(mapView.getDispRange())).commit();
                }

                if (!MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).isOffline) {
                    Map<String, String> paramters = new HashMap<>();

                    paramters.put("userID", String.valueOf(MyApplication.getInstance().getUserId()));

                    String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc";

                    BaseStringTask task = new BaseStringTask(new BaseTaskParameters(url + "/UserLogOut", paramters),
                            new BaseTaskListener<String>() {
                                @Override
                                public void onCompletion(short completFlg, String localObject1) {
                                    exitProcess();
                                }

                                @Override
                                public void onError(Throwable paramThrowable) {
                                    exitProcess();
                                }
                            });
                    MyApplication.getInstance().submitExecutorService(task);
                } else {
                    exitProcess();
                }

                break;
            default:

                List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();

                for (int i = fragments.size() - 1; i >= 0; i--) {

                    Fragment fragment = fragments.get(i);

                    if (fragment == null || fragment == this)
                        continue;

                    fragment.onActivityResult(requestCode, resultCode, intent);

                    break;
                }
                break;
        }
    }

    private void exitProcess() {
        MyApplication.getInstance().getSystemSharedPreferences().edit()
                .putBoolean("isWorkTime", MyApplication.getInstance().getConfigValue("defaultWorkTime", 1) > 0).commit();

        AppManager.finishProgram();
    }

    @Override
    public boolean onBackPressed() {
        try {
            if (drawerLayout != null && drawerListView != null
                    && drawerLayout.isDrawerOpen(drawerListView)) {
                drawerLayout.closeDrawer(drawerListView);

                return true;
            }
            return menu != null && menu.onBackPressed();
        } catch (Exception ex) {
            return true;
        }
    }
}
