package com.mapgis.mmt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by zhouxixiang on 2017\9\13 0013.
 */

public class DialogManager {
    public static void showNormalDialog(Context context,
                                        int icon,
                                        String title,
                                        String content,
                                        final onDialogClickListener listener){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setIcon(icon);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setCancelable(true);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onPositive();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.onNegative();
            }
        });
        builder.create().show();

    }
    public interface onDialogClickListener{
        void onPositive();
        void onNegative();
    }

}
