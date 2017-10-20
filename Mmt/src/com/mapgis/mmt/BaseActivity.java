package com.mapgis.mmt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.common.widget.fragment.BackHandledInterface;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.systemsetting.HomeEventReceiver;
import com.mapgis.mmt.module.systemsetting.locksetting.util.LoginManager;
import com.mapgis.mmt.module.systemsetting.locksetting.util.PasswordManager;
import com.swipebacklayout.lib.SwipeBackLayout;
import com.swipebacklayout.lib.SwipeBackLayout.SwipeListener;
import com.swipebacklayout.lib.app.SwipeBackActivity;

public class BaseActivity extends SwipeBackActivity implements BackHandledInterface {
    protected SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onCreate");
            AppManager.addActivity(this);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            setDefaultContentView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean canChangeStatus = true;

    private boolean hasChangeStatus = false;

    @TargetApi(19)
    private void setStatusBar() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            canChangeStatus = false;
            return;
        }

        Window window = getWindow();
        ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int statusBarHeight = getStatusBarHeight(getResources(), "status_bar_height");

        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
            //如果已经为 ChildView 设置过了 marginTop, 再次调用时直接跳过
            if (lp != null && lp.topMargin < statusBarHeight && lp.height != statusBarHeight) {
                //不预留系统空间
                ViewCompat.setFitsSystemWindows(mChildView, false);
                lp.topMargin += statusBarHeight;
                mChildView.setLayoutParams(lp);
            }
        }

        View statusBarView = mContentView.getChildAt(0);
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == statusBarHeight) {
            //避免重复调用时多次添加 View
            statusBarView.setBackgroundResource(AppStyle.getActionBarStyleResource());
            return;
        }

        statusBarView = new View(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
        statusBarView.setBackgroundResource(AppStyle.getStatusBarStyleResource());

        mContentView.addView(statusBarView, 0, lp);

        hasChangeStatus = true;

    }

    private int getStatusBarHeight(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    protected View defaultBackBtn;

    public View getDefaultBackBtn() {
        return defaultBackBtn;
    }

    public void setDefaultBackBtn(View defaultBackBtn) {
        this.defaultBackBtn = defaultBackBtn;
    }

    protected void setDefaultContentView() {
        setContentView(R.layout.base_fragment);

        defaultBackBtn = findViewById(R.id.baseActionBarImageView);

        addBackBtnListener(defaultBackBtn);

        findViewById(R.id.linearLayout1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseRightImageView().performClick();
            }
        });

        findViewById(R.id.baseErrorLayout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissErrorMsg();
            }
        });

        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        mSwipeBackLayout.setSwipeListener(new SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
            }

            @Override
            public void onScrollOverThreshold() {
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
            }

            @Override
            public void onActivityFinish() {// 自己添加的方法...
                try {
                    View view = getBaseLeftImageView();

                    if (view != null)
                        view.performClick();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    protected void addBackBtnListener(View view) {

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onCustomBack();
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AppManager.finishToNavigation(BaseActivity.this);
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onSaveInstanceState");
    }

    /**
     * 添加Fragment
     *
     * @param fragment 对象
     */
    public void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.baseFragment, fragment);
        ft.show(fragment);

        if (!fragment.isRemoving()) {
            ft.commitAllowingStateLoss();
        }
    }

    public void refreshOtherFragment() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment otherFragment = fm.findFragmentById(R.id.otherFragment);
            transaction.detach(otherFragment);
            transaction.attach(otherFragment);
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 替换Fragment
     *
     * @param fragment 对象
     */
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.baseFragment, fragment);
        ft.commitAllowingStateLoss();
    }

    /**
     * 替换第二个Fragment
     *
     * @param fragment 对象
     */
    public void replaceOtherFragment(Fragment fragment) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            // ft.setCustomAnimations(R.anim.slide_in_from_bottom,
            // R.anim.slide_out_to_bottom);
            ft.replace(R.id.otherFragment, fragment);
            ft.commitAllowingStateLoss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 显示主Fragment还是备用Fragment
     *
     * @param isShowMain 如果true则显示主Frgament
     */
    public void showMainFragment(final boolean isShowMain) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment otherFragment = fm.findFragmentById(R.id.otherFragment);

        if (otherFragment == null) {
            return;
        }

        // 如果想要显示主页面，并且当前显示的就是主页面，则不向下执行
        if (isShowMain && otherFragment.isHidden()) {
            return;
        }

        // 如果想要显示备用页面，并且当前显示的就是备用页面，则不向下执行
        if (!isShowMain && otherFragment.isVisible()) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);

        if (isShowMain) {
            ft.hide(otherFragment);
        } else {
            ft.show(otherFragment);
        }

        ft.commitAllowingStateLoss();
    }

    /**
     * 备用Fragment是否可见
     */
    public boolean isOtherFragmentVisible() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment otherFragment = fm.findFragmentById(R.id.otherFragment);
        return otherFragment.isVisible();
    }

    /**
     * 删除备用Fragment
     */
    public void removeOtherFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment otherFragment = fm.findFragmentById(R.id.otherFragment);

        if (otherFragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(otherFragment);
            ft.commitAllowingStateLoss();
        }
    }

    private boolean hasFinish = false;
    protected boolean hasSetActionBarBG = false;

    @Override
    protected void onStart() {
        super.onStart();

        //状态栏，actionBar保持一致
        if (canChangeStatus && !hasChangeStatus) {
            setStatusBar();
        }

        if (!hasSetActionBarBG) {
            onActionBarBG();
            hasSetActionBarBG = true;
        }
    }

    protected void onActionBarBG() {
        try {

            View actionBar = findViewById(R.id.layoutActionBar);

            if (actionBar == null) {
                actionBar = findViewById(R.id.mainActionBar);
            }

            if (actionBar == null) {
                if (hasChangeStatus) {
                    actionBar = ((ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(1)).getChildAt(0);

                } else {
                    actionBar = ((ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0)).getChildAt(0);
                }
            }

            if (actionBar != null && actionBar.getVisibility() == View.GONE && customView != null) {
                actionBar = ((ViewGroup) customView).getChildAt(0);
            }

            if (actionBar != null && actionBar.getVisibility() == View.VISIBLE) {
                actionBar.setBackgroundResource(AppStyle.getActionBarStyleResource());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 判断是否应该进入锁屏界面
     */
    public static boolean isUnlockScreenActive = false;

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onResume");

        if (!PasswordManager.isNeedLockScreen()) {
            return;
        }
        // 开始计时
        startTimer();

        // 注册广播监听
        registerHomeKeyReceiver(this);

        if (isUnlockScreenActive) {
            isUnlockScreenActive = false;
            enterLockScreen();
        }
    }

    /**
     * 进入锁屏界面
     */
    protected void enterLockScreen() {
        LoginManager.getInstance(this).enterLocalUnlockActivity(PasswordManager.getPasswordType(), false
                , null, BaseActivity.class.getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onPause");

        stopLock();
    }

    public void stopLock() {
        if (!PasswordManager.isNeedLockScreen()) {
            return;
        }
        // 取消监听
        unregisterHomeKeyReceiver(this);

        // 结束屏幕无操作计时器
        stopTimer();
    }

    /**
     * 监听home事件的广播接收者
     */
    protected static HomeEventReceiver mHomeKeyReceiver = null;

    protected static void registerHomeKeyReceiver(Context context) {
        mHomeKeyReceiver = new HomeEventReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    protected static void unregisterHomeKeyReceiver(Context context) {
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onStop");
    }

    /**
     * onRestart()方法调用时说明应用程序是从后台重新进入forground状态的
     * <p/>
     * 这时需要用户输入手势密码
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onDestroy");

        if (!hasFinish) {
            onDefaultBack(BaseActivity.this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(this.getClass().getSimpleName(), this.getClass().getSimpleName() + "===>onConfigurationChanged");
    }

    protected static Thread threadTimer;
    protected static TimerRunnable timerRunnable;

    /**
     * 开启计时器
     */
    protected void startTimer() {
        if (timerRunnable != null && timerRunnable.isLocked) {
            timerRunnable.unLockTimer();
            return;
        }
        timerRunnable = new TimerRunnable();
        threadTimer = new Thread(timerRunnable);
        threadTimer.start();
    }

    protected void stopTimer() {
        if (timerRunnable == null) {
            return;
        }
        timerRunnable.setLocked(true);
        timerRunnable.lockTimer();
    }

    // 当前屏幕无操作的时间
    protected static int mNoOperateScreenTimes = 0;

    /**
     * 计时器
     */
    private class TimerRunnable implements Runnable {
        private boolean isFinishing = false;
        private boolean isLocked = false;
        // 用户三分钟没有操作屏幕就要重新输入密码才能进入
        private final static int NO_OPERATE_SCREEN = 1000 * 60 * 3;

        public boolean isFinishing() {
            return isFinishing;
        }

        public void setFinishing(boolean finishing) {
            isFinishing = finishing;
        }

        public boolean isLocked() {
            return isLocked;
        }

        public void setLocked(boolean locked) {
            this.isLocked = locked;
        }

        @Override
        public void run() {
            synchronized (lockObj) {
                while (!isFinishing) {
                    try {
                        if (mNoOperateScreenTimes < NO_OPERATE_SCREEN) {
                            lockObj.wait(1000);
                            mNoOperateScreenTimes += 1000;
                        } else {
                            isLocked = true;
                            handler.sendEmptyMessage(0);
                        }
                        if (isLocked) {
                            mNoOperateScreenTimes = 0;
                            lockObj.wait();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private final Object lockObj = new Object();

        /**
         * 锁住计时器
         */
        public void lockTimer() {
            synchronized (lockObj) {
                try {
                    mNoOperateScreenTimes = 0;
                    isLocked = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 唤醒计时器
         */
        public void unLockTimer() {
            synchronized (lockObj) {
                try {
                    mNoOperateScreenTimes = 0;
                    isLocked = false;
                    lockObj.notifyAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 结束线程
         */
        public void destroyTimer() {
            mNoOperateScreenTimes = 0;
            isFinishing = true;
            if (isLocked) {
                unLockTimer();
            }
        }
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                enterLockScreen();
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        mNoOperateScreenTimes = 0;
        stopTimer();
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
            // 当用户按home键时返回桌面,此处无法监听到home事件，只有通过广播来监听
            isUnlockScreenActive = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (PasswordManager.isNeedLockScreen()) {
            startTimer();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mNoOperateScreenTimes = 0;
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 监听用户对屏幕的操作，如果三分钟没有操作屏幕就要输入密码重新进入应用
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mNoOperateScreenTimes = 0;
        return super.onTouchEvent(event);
    }

    public ImageButton getBaseLeftImageView() {
        return (ImageButton) findViewById(R.id.baseActionBarImageView);
    }

    public TextView getBaseTextView() {
        return (TextView) findViewById(R.id.baseActionBarTextView);
    }

    public ImageButton getBaseRightImageView() {
        return (ImageButton) findViewById(R.id.baseActionBarRightImageView);
    }

    public void setBaseProgressBarVisibility(boolean isVisible) {
        findViewById(R.id.baseActionBarProgressBar).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.linearLayout1).setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    public int getBaseProgressBarVisibility() {
        return findViewById(R.id.baseActionBarProgressBar).getVisibility();
    }

    /**
     * 显示Toast信息
     */
    public void showToast(String toastStr) {
        try {
            if (!this.isFinishing()) {
                MyApplication.getInstance().showMessageWithHandle(toastStr);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 设置是否允许滑动结束界面
     */
    public void setSwipeFinish(boolean enable) {
        mSwipeBackLayout.setEnableGesture(enable);
    }

    /**
     * 显示错误信息
     */
    public void showErrorMsg(final String msg) {
        try {
            ((TextView) findViewById(R.id.baseErrorMsg)).setText(msg);
            findViewById(R.id.baseErrorLayout).setVisibility(View.VISIBLE);
            Animation showErrorAnim = AnimationUtils.loadAnimation(this, R.anim.error_show_anim);
            showErrorAnim.setFillAfter(true);
            findViewById(R.id.baseErrorLayout).startAnimation(showErrorAnim);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * 隐藏错误信息
     */
    public void dismissErrorMsg() {
        findViewById(R.id.baseErrorLayout).setVisibility(View.GONE);
    }

    public void setTopViewVisibility(int visibility) {
        findViewById(R.id.baseTopView).setVisibility(visibility);
    }

    /**
     * 重置顶部菜单,设置标题
     *
     * @param title 标题
     */
    public void setTitleAndClear(String title) {
        LinearLayout topView = (LinearLayout) findViewById(R.id.baseTopView);

        topView.getChildAt(0).setVisibility(View.VISIBLE);
        topView.removeViews(1, topView.getChildCount() - 1);

        getBaseTextView().setText(title);
        setDefaultBackBtn(findViewById(R.id.baseActionBarImageView));
    }

    private View customView;

    /**
     * 设置自定义顶部菜单
     *
     * @param view 自定义视图
     */
    public void setCustomView(View view) {
        LinearLayout topView = (LinearLayout) findViewById(R.id.baseTopView);

        customView = view;

        View actionBar = topView.getChildAt(0);
        actionBar.setVisibility(View.GONE);

        for (int i = 1; i < topView.getChildCount(); i++) {
            topView.removeViewAt(i);
        }

        topView.addView(customView, actionBar.getLayoutParams());
    }

    public LinearLayout getBottomView() {
        return (LinearLayout) findViewById(R.id.baseBottomView);
    }

    /**
     * 设置底部菜单 显示/隐藏
     */
    public void setBottomViewVisible(int visibility) {
        LinearLayout bottomView = (LinearLayout) findViewById(R.id.baseBottomView);
        bottomView.setVisibility(visibility);
    }

    public void clearAllBottomUnitView() {
        ((LinearLayout) findViewById(R.id.baseBottomView)).removeAllViews();
    }

    /**
     * 添加底部菜单按钮
     */
    public void addBottomUnitView(BottomUnitView bottomUnitView, OnClickListener onClickListener) {
        bottomUnitView.setOnClickListener(onClickListener);
        addBottomUnitView(bottomUnitView, false);
    }

    public View addBottomUnitView(String btnText, boolean needIcon, OnClickListener listener) {
        BottomUnitView bottomUnitView = new BottomUnitView(this);
        bottomUnitView.setContent(btnText);
        if (needIcon) {
            bottomUnitView.setImageResource(R.drawable.handoverform_report);
        }
        bottomUnitView.setOnClickListener(listener);

        addBottomUnitView(bottomUnitView, false);

        return bottomUnitView;
    }

    /**
     * 添加底部菜单按钮
     *
     * @param bottomUnitView 加入底部的菜单按钮(按钮已经设置了 OnClickListener)
     * @param needClear      添加菜单按钮时是否清空已经存在的按钮
     */
    public void addBottomUnitView(BottomUnitView bottomUnitView, boolean needClear) {

        LinearLayout bottomView = (LinearLayout) findViewById(R.id.baseBottomView);

        if (bottomView.getVisibility() != View.VISIBLE) {
            bottomView.setVisibility(View.VISIBLE);
        }

        if (needClear) {
            bottomView.removeAllViews();
        }

        bottomView.addView(bottomUnitView);
    }

    /**
     * 获取自定义顶部菜单View
     *
     * @return 顶部菜单View
     */
    public View getCustomView() {
        return customView;
    }

    protected BackHandledFragment mBackHandedFragment;

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    public void onBackPressed() {
        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (defaultBackBtn != null) {
                defaultBackBtn.performClick();
            } else {
                onCustomBack();
            }
        }
    }

    protected void onDefaultBack(Activity activity) {
        hasFinish = true;
        AppManager.finishActivity(activity == null ? BaseActivity.this : activity);
        MyApplication.getInstance().finishActivityAnimation(activity == null ? BaseActivity.this : activity);
    }

    public void onCustomBack() {
        if (AppManager.activityList.size() >= 3) {
            backByReorder();
            return;
        }
        onDefaultBack(BaseActivity.this);
    }

    /**
     * @param refresh 回退时是否刷新上一页
     */
    public void backByReorder(boolean refresh) {
        try {
            Activity currentActivity = AppManager.currentActivity();

            if (currentActivity == null) {
                throw new Exception();
            }

            //地图到其他页后点击返回 或 其他页到其他页
            //打开倒数第二个后关闭当前
            if (currentActivity == BaseActivity.this) {
                startSecondActivity(currentActivity, refresh);
                onDefaultBack(BaseActivity.this);
            } else {

                //其他页面到地图后点击的返回
                //直接打开其他页即可
                if (BaseActivity.this instanceof MapGISFrame && !(currentActivity instanceof MapGISFrame)) {

                    Intent intent = currentActivity.getIntent();
                    if (refresh) {
                        AppManager.finishActivity(currentActivity);
                    } else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    }
                    startActivity(intent);
                } else {

                    //  ! BaseActivity.this instanceof MapGISFrame || (currentActivity instanceof MapGISFrame)
                    if (currentActivity instanceof MapGISFrame) {
                        //异常
                        AppManager.finishToNavigation(BaseActivity.this);
                        return;
                    }

                    if (!(BaseActivity.this instanceof MapGISFrame)) {
                        //处理多级返回
                        startSecondActivity(currentActivity, refresh);
                        onDefaultBack(currentActivity);
                    }
                }
            }
        } catch (Exception ex) {
            BaseActivity.this.showErrorMsg("返回异常");
        }
    }

    public void backByReorder() {
        backByReorder(false);
    }

    private void startSecondActivity(Activity currentActivity, boolean refresh) {
        Activity lastActivity = null;
        //从倒数第二个开始 找到和当前activity不同的activity为止
        for (int i = AppManager.activityList.size() - 2; i >= 0; i--) {

            lastActivity = AppManager.activityList.get(i);

            if ((lastActivity != currentActivity) && (!lastActivity.getLocalClassName().equals(currentActivity.getLocalClassName()))) {
                break;
            }

            onDefaultBack(lastActivity);
        }

        if (lastActivity == null) {
            return;
        }

        if (AppManager.activityList.size() <= 1) {
            return;
        }

        Intent intent = lastActivity.getIntent();

        if (refresh) {
            AppManager.finishActivity(lastActivity);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        startActivity(intent);
    }


}
