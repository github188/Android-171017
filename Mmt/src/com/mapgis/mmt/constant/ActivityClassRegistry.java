package com.mapgis.mmt.constant;

import com.mapgis.mmt.module.login.Login;
import com.mapgis.mmt.module.login.SystemSetter;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.systemsetting.AppAboutActivity;

import java.util.Hashtable;

public class ActivityClassRegistry {
    private static ActivityClassRegistry instance;
    private final Hashtable<String, Class<?>> registry;

    {
        registry = new Hashtable<>();
        regist("登陆设置", SystemSetter.class);
        regist("登录界面", Login.class);
        regist("关于界面", AppAboutActivity.class);
        regist("主界面", NavigationActivity.class);
    }

    public static ActivityClassRegistry getInstance() {
        if (instance == null) {
            instance = new ActivityClassRegistry();
        }

        return instance;
    }

    public void regist(String key, Class<?> value) {
        registry.put(key, value);
    }

    public Class<?> getActivityClass(String name) {
        try {
            if (registry.containsKey(name)) {
                return registry.get(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
