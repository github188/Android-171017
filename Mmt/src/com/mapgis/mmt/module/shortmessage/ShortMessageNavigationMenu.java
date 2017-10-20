package com.mapgis.mmt.module.shortmessage;

import android.content.Intent;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.mapgis.mmt.module.navigation.NewTipThread;

/**
 * 即时信息
 */
public class ShortMessageNavigationMenu extends BaseNavigationMenu {

    public ShortMessageNavigationMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, ShortMessageTabs.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivityForResult(intent, 0);
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        NewTipThread.notifyThread();
        return super.onActivityResult(resultCode, intent);
    }

    @Override
    public int[] getIcons() {
        return new int[]{R.drawable.main_menu_message, R.drawable.home_sms, R.drawable.home_circle_task};
    }
}
