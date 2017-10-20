package com.project.enn.dahua.menu;

import android.content.Intent;

import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;
import com.project.enn.dahua.activity.AlarmActivity;

/**
 * Created by Comclay on 2017/4/21.
 * 报警界面
 */

public class AlarmMenu extends BaseNavigationMenu {
    public AlarmMenu(NavigationActivity navigationActivity, NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        showAlarmDialog();
    }

    private void showAlarmDialog(){
        /*AlertDialog.Builder builder = new AlertDialog.Builder(navigationActivity);
        builder.setMessage("确认报警！");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enterAlarmActivity();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();*/
        enterAlarmActivity();
    }

    private void enterAlarmActivity(){
        Intent intent=new Intent(navigationActivity, AlarmActivity.class);
        navigationActivity.startActivity(intent);
    }
}
