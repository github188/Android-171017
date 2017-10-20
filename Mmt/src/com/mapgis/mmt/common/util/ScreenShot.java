package com.mapgis.mmt.common.util;

import java.io.FileOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

public class ScreenShot {
    private boolean needTop;
    private String path;

    public ScreenShot(boolean needTop) {
        this(needTop, "sdcard/xx.png");
    }

    public ScreenShot(boolean needTop, String path) {
        this.needTop = needTop;
        this.path = path;
    }

    // 获取指定Activity的截屏，保存到png文件
    public Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();

        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap b1 = view.getDrawingCache();

        int top = 0;

        if (!needTop) {
            // 获取状态栏高度
            Rect frame = new Rect();

            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

            top = frame.top;
        }

        Point p = new Point();

        // 获取屏幕长和高
        activity.getWindowManager().getDefaultDisplay().getSize(p);

        // 去掉标题栏
        Bitmap b = Bitmap.createBitmap(b1, 0, top, p.x, p.y - top);

        view.destroyDrawingCache();

        return b;
    }

    // 保存到sdcard
    public void savePic(Bitmap b, String strFileName) {
        try {
            FileOutputStream fos = new FileOutputStream(strFileName);

            b.compress(Bitmap.CompressFormat.PNG, 90, fos);

            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 程序入口
    public void shoot(Activity a) {
        savePic(takeScreenShot(a), this.path);
    }
}

/**
 * 需要注意的是，shoot方法只能在view已经被加载后方可调用。
 * 或者在
 *
 * @Override public void onWindowFocusChanged(boolean hasFocus){
 * // TODO Auto-generated method stub
 * super.onWindowFocusChanged(hasFocus);
 * ScreenShot.shoot(this);
 * }
 * 中调用
 **/

