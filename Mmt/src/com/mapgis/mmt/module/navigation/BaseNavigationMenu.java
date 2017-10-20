package com.mapgis.mmt.module.navigation;

import android.content.Intent;

import com.mapgis.mmt.R;

public abstract class BaseNavigationMenu {
    protected NavigationActivity navigationActivity;
    protected NavigationItem item;

    public BaseNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        this.navigationActivity = navigationActivity;
        this.item = item;
    }

    public abstract void onItemSelected();

    public boolean onActivityResult(int resultCode, Intent intent) {
        return false;
    }

    /**
     * 不同主页风格下使用的图标，默认顺序是 WindowsPhone、Android
     *
     * @return 不同风格的图标列表
     */
    public int[] getIcons() {
        return getIcons("上报");
    }

    protected int[] getIcons(String target) {
        switch (target) {
            case "上报":
                return new int[]{R.drawable.main_menu_case_report, R.drawable.home_case_report, R.drawable.home_circle_case_report};
            case "在办":
                return new int[]{R.drawable.main_menu_maintenance_handover, R.drawable.ic_menu_doing_32dp, R.drawable.home_circle_case_doing};
            case "已办":
                return new int[]{R.drawable.main_menu_case_list, R.drawable.home_case_done, R.drawable.home_circle_case_done};
            case "分派":
                return new int[]{R.drawable.main_menu_case_list, R.drawable.ic_menu_dispatch_32dp, R.drawable.home_circle_case_done};
            case "总览":
                return new int[]{R.drawable.main_menu_my_plan, R.drawable.ic_menu_overview_32dp, R.drawable.home_circle_case_done};
            case "养护":
                return new int[]{R.drawable.main_menu_my_plan, R.drawable.ic_menu_care_32dp, R.drawable.home_circle_case_done};
            case "台账":
                return new int[]{R.drawable.main_menu_my_plan, R.drawable.ic_menu_taizhang_32dp, R.drawable.home_circle_case_done};
            case "检测":
                return new int[]{R.drawable.main_menu_my_plan, R.drawable.ic_menu_detect_32dp, R.drawable.home_circle_case_done};
            default:
                return new int[]{R.drawable.main_menu_case_report, R.drawable.home_case_report, R.drawable.home_circle_case_report};
        }
    }
}
