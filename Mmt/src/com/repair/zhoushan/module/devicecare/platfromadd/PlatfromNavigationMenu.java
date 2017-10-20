package com.repair.zhoushan.module.devicecare.platfromadd;

import android.content.Intent;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 台账挂接
 */
public class PlatfromNavigationMenu extends BaseNavigationMenu {

    public PlatfromNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        String bizName=item.Function.ModuleParam;
        if(TextUtils.isEmpty(bizName)){
            MyApplication.getInstance().showMessageWithHandle("请配置业务类型");
            return;
        }

        Intent intent = new Intent(navigationActivity, PlatfromListActivity.class);
        intent.putExtra("Title", item.Function.Alias);
        intent.putExtra("bizName", bizName);
        navigationActivity.startActivity(intent);
    }

    @Override
    public int[] getIcons() {
        return getIcons("台账");
    }
}