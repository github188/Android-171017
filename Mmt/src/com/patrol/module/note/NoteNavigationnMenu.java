package com.patrol.module.note;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

public class NoteNavigationnMenu extends BaseNavigationMenu {

    public NoteNavigationnMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {

        Intent intent = new Intent(navigationActivity, NoteActivity.class);
        intent.putExtra("Title", "巡线日志");
        navigationActivity.startActivity(intent);
    }
}
