package com.project.enn;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.DeviceUtil;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.constant.MapMenuRegistry;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.patrolproduct.PatrolFunctionRegistry;
import com.patrolproduct.PatrolProductAppcation;
import com.project.enn.dahua.ServiceHelper;
import com.project.enn.dahua.menu.AlarmMenu;
import com.project.enn.login.SystemSetter;
import com.project.enn.navigation.ENNBackNavigationMapMenu;
import com.project.enn.navigation.ENNavigationMainFragemnt;
import com.project.enn.navigation.EnnNavigationActivity;
import com.repair.RepairFunctionRegistry;

public class ENNApplication extends PatrolProductAppcation {

    @Override
    public void onCreate() {
        putConfigValue("NeedUnifyLogin", "1");

        super.onCreate();

        PatrolFunctionRegistry.regist();
        RepairFunctionRegistry.regist();

        putConfigValue("HomePage", "normal");

        ActivityClassRegistry.getInstance().regist("登陆设置", SystemSetter.class);
        ActivityClassRegistry.getInstance().regist("主界面", EnnNavigationActivity.class);
        ActivityClassRegistry.getInstance().regist("导航界面",ENNavigationMainFragemnt.class);
        NavigationMenuRegistry.getInstance().regist("报警", AlarmMenu.class);
        MapMenuRegistry.getInstance().regist("返回主页", ENNBackNavigationMapMenu.class.getName());

        if (Build.MODEL.equalsIgnoreCase("北斗移动终端")) {
            boolean isAvilible = DeviceUtil.isAvilible(MyApplication.getInstance(), "com.huace.gnssserver");

            if (!isAvilible) {
                Toast.makeText(this, "未检测到华测位置服务插件，请先下载安装", Toast.LENGTH_LONG).show();

                Uri uri = Uri.parse("http://www.pgyer.com/gnss");

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        }
    }

    @Override
    public void Start(MapGISFrame mapGISFrame) {
        super.Start(mapGISFrame);
        if(hasConnDHPermission()){
            MapGISFrame activity = (MapGISFrame) AppManager.getActivity(MapGISFrame.class);
            ServiceHelper.getInstance().bindDaHuaService(activity);
        }
    }

    public static ENNApplication getInstance() {
        return (ENNApplication) instance;
    }
}