package com.mapgis.mmt;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by liuyunfan on 2016/6/20.
 */
public class AppStyle {

    /**
     * 主界面导航样式枚举值
     */
    public enum NavigationStyle {

        TRADITIONAL(R.layout.navigation_fragment), // 传统 WindowsPhone磁贴风格
        NORMAL(R.layout.navigation_normal_fragment), // 九宫格风格
        CIRCULAR(R.layout.navigation_circular_fragment); // 半圆形风格

        private final int resourceId; // 样式对应的布局文件

        NavigationStyle(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }

    /**
     * 状态栏
     */
    public enum StatusBarStyle {

        TRADITIONAL(R.color.default_actionbar),
        NORMAL(R.color.xa_actionbar),
        CIRCULAR(R.drawable.statusbar_gradient_bg); // 半圆形风格

        private final int resourceId;

        StatusBarStyle(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }

    /**
     * 头部样式
     */
    public enum ActionBarStyle {

        TRADITIONAL(R.drawable.main_menu_top),
        NORMAL(R.color.xa_actionbar),
        CIRCULAR(R.drawable.actionbar_gradient_bg); // 半圆形风格

        private final int resourceId;

        ActionBarStyle(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }


    /**
     * 按钮样式
     */
    public enum BtnBackgroundStyle {

        TRADITIONAL(R.drawable.layout_focus_bg),
        NORMAL(R.color.xa_actionbar),
        CIRCULAR(R.color.tj_actionbar); // 半圆形风格

        private final int resourceId;

        BtnBackgroundStyle(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }

    public enum SwitchFragmentStyle {
        TRADITIONAL(R.color.default_actionbar),
        NORMAL(R.color.xa_actionbar),
        CIRCULAR(R.color.tj_actionbar); // 半圆形风格
        private final int resourceId;

        SwitchFragmentStyle(int resourceId) {
            this.resourceId = resourceId;
        }

        public int getResourceId() {
            return resourceId;
        }
    }

    public static int getNavigationStyleResource() {

        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int navigationStyleID = NavigationStyle.TRADITIONAL.getResourceId();
        switch (homePageStyle) {
            case "circular":
            case "normal":
                navigationStyleID = NavigationStyle.NORMAL.getResourceId();
                break;
//            case "circular":
//                navigationStyleID = NavigationStyle.CIRCULAR.getResourceId();
//                break;
        }
        return navigationStyleID;
    }

    /**
     * 按钮 样式
     *
     * @return
     */
    public static int getBtnBackgroundStyleResource() {

        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int btnBackgroundStyleID = BtnBackgroundStyle.TRADITIONAL.getResourceId();
        switch (homePageStyle) {
            case "circular":
                btnBackgroundStyleID = BtnBackgroundStyle.CIRCULAR.getResourceId();
                break;
//            case "normal":
//                btnBackgroundStyleID = BtnBackgroundStyle.NORMAL.getResourceId();
//                break;

        }
        return btnBackgroundStyleID;
    }

    public static int getStatusBarStyleResource() {

        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int statusResourceID = StatusBarStyle.TRADITIONAL.getResourceId();
        switch (homePageStyle) {
            case "circular":
                statusResourceID = StatusBarStyle.CIRCULAR.getResourceId();
                break;
            case "normal":
                statusResourceID = StatusBarStyle.NORMAL.getResourceId();
                break;
        }
        return statusResourceID;
    }

    /**
     * ActionBar 样式
     *
     * @return
     */
    public static int getActionBarStyleResource() {

        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int actionBGResourceID = ActionBarStyle.TRADITIONAL.getResourceId();
        switch (homePageStyle) {
            case "circular":
                actionBGResourceID = ActionBarStyle.CIRCULAR.getResourceId();
                break;
            case "normal":
                actionBGResourceID = ActionBarStyle.NORMAL.getResourceId();
                break;
        }
        return actionBGResourceID;
    }

    public static int getSwitchFragmentStyleResource() {

        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int switchFragmentResourceID = SwitchFragmentStyle.TRADITIONAL.getResourceId();
        switch (homePageStyle) {
            case "circular":
                switchFragmentResourceID = SwitchFragmentStyle.CIRCULAR.getResourceId();
                break;
            case "normal":
                switchFragmentResourceID = SwitchFragmentStyle.NORMAL.getResourceId();
                break;
        }
        return switchFragmentResourceID;
    }

    /**
     * 一些写死的按钮，只有天津的改为绿色，其余保持原样不变
     * 遇到一个改一个
     *
     * @return
     */
    public static int getCustromBtnStyleResource() {
        final String homePageStyle = MyApplication.getInstance().getConfigValue("HomePage").toLowerCase();

        int custromBtnStyleResourceID = -1;
        switch (homePageStyle) {
            case "circular":
                custromBtnStyleResourceID = BtnBackgroundStyle.CIRCULAR.getResourceId();
                break;
            case "normal":
                custromBtnStyleResourceID = BtnBackgroundStyle.NORMAL.getResourceId();
                break;
        }
        return custromBtnStyleResourceID;
    }


    public static void initscreen(Activity context) {
        context.requestWindowFeature(Window.FEATURE_NO_TITLE);
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
