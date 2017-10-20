package com.mapgis.mmt.module.gis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.constant.Constants;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gps.LocationTimerCallback;
import com.mapgis.mmt.module.gps.Receiver.BluetoothService;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationController;
import com.zondy.mapgis.android.mapview.MapView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

/**
 * 地图主界面
 *
 * @author Zoro
 */
public class MapGISFrame extends BaseActivity {
    public static final String LONG_TAG_FOR_POINT = "longTag4Point";

    private MapGISFrameFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            if ("BT".equals(MyApplication.getInstance().getConfigValue("GpsReceiver"))) {
                // 开启蓝牙设备的监听服务，只有选择了蓝牙设备之后才能开启后台主服务
                Intent intent = new Intent(this, BluetoothService.class);
                startService(intent);
            } else {
                MyApplication.getInstance().Start(this);
            }

            fragment = new MapGISFrameFragment();

            addFragment(fragment);

            initTopBar();

            setSwipeFinish(false);

            handGPSTip();

            EventBus.getDefault().register(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handGPSTip() {
        int tipMode;
        if ((tipMode = isTipGPSOpened()) != 0) {
            final int tipModetemp = tipMode;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (tipModetemp == 1) {
                            Activity activity = AppManager.currentActivity();

                            if (activity == null)
                                return;

                            OkCancelDialogFragment dialogFragment = new OkCancelDialogFragment("GPS未开启，是否打开？");

                            dialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                                @Override
                                public void onRightButtonClick(View view) {
                                    Intent intent = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                                    intent.addCategory(Intent.CATEGORY_DEFAULT);

                                    startActivity(intent);
                                }
                            });

                            dialogFragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                                @Override
                                public void onLeftButtonClick(View view) {
                                    BaseClassUtil.getConfigACache().put("isGPSFirstTip", 0);
                                }
                            });

                            dialogFragment.show(((BaseActivity) activity).getSupportFragmentManager(), "");

                            return;
                        }
                        if (tipModetemp == 2) {
                            MyApplication.getInstance().showMessageWithHandle("GPS未开启");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, 1000);
        }
    }

    private boolean isInWorkTime() {
        //默认一开始就提示gps是否开启，部分项目只有执行某个操作后才提示（港华）
        //换一种理解：默认一开始就属于工作时段（后续也可以自己设置工作时段才提示），港华项目点击上半后才属于工作时段
        //这里可通过判断当前时间是否在工作时段内
        return MyApplication.getInstance().getConfigValue("GPSTipRunStart", 1) == 1;
    }


    /**
     * 提示模式，1：弹框提示，2：tip提示，0：不提示
     *
     * @return 1：弹框提示，2：tip提示，0：不提示
     */
    private int isTipGPSOpened() {
        if (isInWorkTime() && !isGPSOpened()) {
            String isGPSFirstTip = BaseClassUtil.getConfigACache().getAsString("isGPSFirstTip");
            if (TextUtils.isEmpty(isGPSFirstTip) || "1".equals(isGPSFirstTip)) {
                return 1;
            }
            return 2;
        }
        return 0;
    }

    private boolean isGPSOpened() {
        LocationManager locationManager = (LocationManager) MapGISFrame.this
                .getSystemService(Context.LOCATION_SERVICE);
        return locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private void initTopBar() {
        getBaseTextView().setText(Product.getInstance().Title);

        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.menu = MapMenuRegistry.getInstance().getMenuInstance("返回主页", MapGISFrame.this);
                fragment.menu.onOptionsItemSelected();
                MyApplication.getInstance().finishActivityAnimation(MapGISFrame.this);
            }
        });

        getBaseLeftImageView().setVisibility(View.GONE);

        getBaseRightImageView().setImageResource(R.drawable.actionbar_layer);
        getBaseRightImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MapMenuRegistry.getInstance().getMenuInstance("图层控制", MapGISFrame.this).onOptionsItemSelected();
            }
        });

        getBaseRightImageView().setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        if (getIntent().getBooleanExtra("isShowBaseLeftImage", false)) {
            getBaseLeftImageView().setVisibility(View.VISIBLE);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        fragment.onActivityResult(arg0, arg1, arg2);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("outIntentFlag", false)) {
            AppManager.finishProgram();

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * 获取地图视图
     */
    public MapView getMapView() {
        if (fragment == null) {
            return null;
        }
        return fragment.getMapView();
    }

    /**
     * 获取主页面块
     */
    public MapGISFrameFragment getFragment() {
        return fragment;
    }

    /**
     * 清除地图上的绘制内容
     */
    public void clearMapview() {
        fragment.clearMapview();
    }

    /**
     * 将底部栏还原为初始状态
     */
    public void setBottomBarClear() {
        ViewGroup bottomBar = (ViewGroup) findViewById(R.id.layoutMapToolbar);

        if (bottomBar.getVisibility() == View.GONE) {
            return;
        }

        bottomBar.getChildAt(0).setVisibility(View.VISIBLE);
        bottomBar.removeViews(1, bottomBar.getChildCount() - 1);
    }

    /**
     * 重置地图功能
     */
    public void resetMenuFunction() {
        try {
            setTitleAndClear(Product.getInstance().Title);

            ViewGroup bottomBar = (ViewGroup) findViewById(R.id.layoutMapToolbar);

            bottomBar.getChildAt(0).setVisibility(View.VISIBLE);
            bottomBar.removeViews(1, bottomBar.getChildCount() - 1);
            bottomBar.setVisibility(View.VISIBLE);

            removeOtherFragment();

            fragment.showViewPagerVisibility(View.GONE);

            initTopBar();

            if (getMapView() != null) {
                getMapView().setZoomChangedListener(null);
                getMapView().setTapListener(null);
                getMapView().getAnnotationLayer().removeAllAnnotations();
                getMapView().getGraphicLayer().removeAllGraphics();

                getMapView().refresh();
            }

            if (fragment.menu != null) {
                fragment.menu.removeBottomViewBar();
                fragment.menu = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 地图未加载成功，调用该方法停止功能
     */
    public void stopMenuFunction() {
        showToast(getResources().getString(R.string.mapmenu_error));
        setTitleAndClear(Product.getInstance().Title);
    }

    @Override
    protected void onDestroy() {
        //部分手机滑动关闭app时不调用onDestroy方法
        CacheUtils.getInstance().flush();
        UserBean ub = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);
        if (ub != null && !ub.isOffline) {
            NavigationController.exitAppSilent(MapGISFrame.this);
        }
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public void onBackEvent(Constants.HandleToListWithMapEvent event) {
        resetMenuFunction();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(receiver, new IntentFilter(MmtMainService.class.getName()));
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Real-time locate
            if (intent.hasExtra("xy")) {
                GpsXYZ xy = intent.getParcelableExtra("xy");
                checkRealtimeLocate(xy);
            }
            // Menu's specified implement for some purpose
            if (fragment.menu != null) {
                fragment.menu.onReceive(context, intent);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(receiver);
    }

    /**
     * 用于定时定位功能
     */
    long preRealtimeLocateTime = -1;

    /**
     * 已经开启定时定位，并且时间间隔达到了定时定位间隔要求，发送定时定位指令，否则发送检查到位状态指令
     *
     * @param xyz 当前坐标
     */
    private void checkRealtimeLocate(GpsXYZ xyz) {
        try {
            if (MyApplication.getInstance().getConfigValue("isRealtimeLocate", 0) <= 0)
                return;

            long now = new Date().getTime();

            long span = MyApplication.getInstance().getConfigValue("realtimeLocateInterval", 5) * 1000;

            if (preRealtimeLocateTime > 0 && (now - preRealtimeLocateTime) < span)
                return;

            boolean isPanTo = MyApplication.getInstance().getConfigValue("MapPanToCenter", 1) == 1;

            MyApplication.getInstance().sendToBaseMapHandle(new LocationTimerCallback(xyz, isPanTo));

            preRealtimeLocateTime = now;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}