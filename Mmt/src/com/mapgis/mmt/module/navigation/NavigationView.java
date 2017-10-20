package com.mapgis.mmt.module.navigation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.List;

public class NavigationView extends View {
    private final Paint paint;
    private final int padding = 12;
    private final float textSizeDip;

    private int blue, lightBlue, green, orange, yellow, purple, lightGreen;

    private final List<NavigationItem> navigationItems;

    private final NavigationActivity navigationActivity;

    /** 设置一个缩放的常量 */
    // private final float mMinScale = 5;

    /**
     * 标识 action_down 时 点击 的 NavigationItem
     */
    private NavigationItem itemDown;

    public NavigationView(NavigationActivity navigationActivity, AttributeSet attrs, List<NavigationItem> navigationItems) {
        super(navigationActivity, attrs);

        this.navigationActivity = navigationActivity;
        this.navigationItems = navigationItems;

        textSizeDip = DimenTool.dip2px(navigationActivity, 18);

        paint = new Paint();
        paint.setTextSize(textSizeDip);
        paint.setAntiAlias(true);
        paint.setDither(true);

        initColor();

    }

    public NavigationView(NavigationActivity navigationActivity, List<NavigationItem> navigationItems) {
        this(navigationActivity, null, navigationItems);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initModule(navigationItems);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        for (NavigationItem item : navigationItems) {
            drawMyRect(canvas, item);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (NavigationItem item : navigationItems) {
                    if (item.BackRect != null && item.BackRect.contains(x, y)) {
                        itemDown = item;
                        item.IsClicked = true;
                        postInvalidate();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (itemDown != null && !itemDown.BackRect.contains(x, y)) {
                    itemDown.IsClicked = false;
                    postInvalidate();
                    break;
                }
                for (NavigationItem item : navigationItems) {
                    item.IsClicked = false;

                    if (item.BackRect != null && item.BackRect.contains(x, y)) {
                        item.IsAlert = false;

                        navigationActivity.onNavigationItemClick(item);

                        break;
                    }
                }
                postInvalidate();
                break;
        }
        return true;
    }

    /**
     * 绘制需要的图块模型
     *
     * @param canvas 画布
     */
    private void drawMyRect(final Canvas canvas, final NavigationItem item) {
        Bitmap bitmap = item.getBitmap(this);

        float drawTextSizeDip = textSizeDip;

        paint.setColor(item.BackColor);

        if (item.IsClicked) {
            paint.setAlpha(255);

            // beginScale(canvas, items.getRect(), paint);

        } else {
            paint.setAlpha(200);

            // canvas.drawRect(items.getRect(), paint);
        }

        canvas.drawRect(item.BackRect, paint);

        Matrix matrix = item.getMatrix();

        // 屏幕分辨率过小时，bitmap会超出rect范围，此时将text的大小缩小0.6倍
        if (item.BackRect.centerY() - bitmap.getHeight() < item.BackRect.top) {
            drawTextSizeDip = drawTextSizeDip * 0.6f;
        }

        canvas.drawBitmap(bitmap, matrix, paint);

        paint.setColor(Color.WHITE);

        paint.setTextSize(drawTextSizeDip);

        if (item.Count != null && !item.Count.trim().equals("0")) {
            canvas.drawText(item.Count, item.BackRect.centerX() + bitmap.getWidth() / 2 + padding, item.BackRect.centerY()
                    - bitmap.getHeight() / 4, paint);
        }

        float textWidth = paint.measureText(item.Function.Alias);

        canvas.drawText(item.Function.Alias, item.BackRect.centerX() - textWidth / 2, item.BackRect.centerY() + drawTextSizeDip
                * 4 / 3, paint);
    }

    // private synchronized void beginScale(Canvas canvas, Rect rect, Paint
    // paint) {
    // canvas.drawRect(rect.left + mMinScale, rect.top + mMinScale, rect.right -
    // mMinScale, rect.bottom - mMinScale, paint);
    // }

    /**
     * 颜色初始化
     */
    private void initColor() {
        blue = navigationActivity.getResources().getColor(R.color.nav_blue);
        lightBlue = navigationActivity.getResources().getColor(R.color.nav_lightBlue);
        green = navigationActivity.getResources().getColor(R.color.nav_green);
        lightGreen = navigationActivity.getResources().getColor(R.color.nav_lightGreen);
        orange = navigationActivity.getResources().getColor(R.color.nav_orange);
        yellow = navigationActivity.getResources().getColor(R.color.nav_yellow);
        purple = navigationActivity.getResources().getColor(R.color.nav_purple);
    }

    /**
     * 初始化模板信息
     */
    private void initModule(List<NavigationItem> items) {
        final int margin = 2, w = getWidth(), h = getHeight(), t = items.size();

        if (t == 1) {
            /****************************************** 一个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w - margin, h - 3 * margin);
            items.get(0).BackColor = blue;
        } else if (t == 2) {
            /****************************************** 二个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w - margin, h * 4 / 6);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(margin, 4 * h / 6 + padding, w - margin, h - 3 * margin);
            items.get(1).BackColor = orange;
        } else if (t == 3) {
            /****************************************** 三个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w - margin, h * 4 / 6);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(margin, 4 * h / 6 + padding, w / 3, h - 3 * margin);
            items.get(1).BackColor = orange;

            items.get(2).BackRect = new Rect(w / 3 + padding, 4 * h / 6 + padding, w - margin, h - 3 * margin);
            items.get(2).BackColor = purple;
        } else if (t == 4) {
            /****************************************** 四个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w * 6 / 10 - padding, h * 7 / 10);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(w * 6 / 10, margin, w - margin, h * 7 / 20 - padding / 2);
            items.get(1).BackColor = orange;

            items.get(2).BackRect = new Rect(w * 6 / 10, h * 7 / 20 + padding / 2, w - margin, h * 7 / 10);
            items.get(2).BackColor = green;

            items.get(3).BackRect = new Rect(margin, h * 7 / 10 + padding, w - margin, h - 3 * margin);
            items.get(3).BackColor = lightGreen;
        } else if (t == 5) {
            /****************************************** 五个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w * 2 / 3 - padding, h / 2);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(margin, h / 2 + padding, w * 2 / 3 - padding, 3 * h / 4 - padding);
            items.get(1).BackColor = lightGreen;

            items.get(2).BackRect = new Rect(w * 2 / 3, margin, w - margin, h * 3 / 8 - padding / 2);
            items.get(2).BackColor = orange;

            items.get(3).BackRect = new Rect(w * 2 / 3, h * 3 / 8 + padding / 2, w - margin, 3 * h / 4 - padding);
            items.get(3).BackColor = green;

            items.get(4).BackRect = new Rect(margin, h * 3 / 4, w - margin, h - 3 * margin);
            items.get(4).BackColor = purple;
        } else if (t == 6) {
            /****************************************** 六个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w / 2 - padding, h / 2);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(w / 2, margin, w - margin, h / 4);
            items.get(1).BackColor = lightBlue;

            items.get(2).BackRect = new Rect(w / 2, h / 4 + padding, w - margin, h / 2);
            items.get(2).BackColor = green;

            items.get(3).BackRect = new Rect(margin, h / 2 + padding, w * 3 / 5 - padding, 3 * h / 4 - padding);
            items.get(3).BackColor = lightBlue;

            items.get(4).BackRect = new Rect(w * 3 / 5, h / 2 + padding, w - margin, 3 * h / 4 - padding);
            items.get(4).BackColor = orange;

            items.get(5).BackRect = new Rect(margin, 3 * h / 4, w - margin, h - 3 * margin);
            items.get(5).BackColor = purple;
        } else if (t == 7) {
            /****************************************** 七个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w / 2 - padding, h / 2);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(w / 2, margin, w - margin, h / 4);
            items.get(1).BackColor = lightBlue;

            items.get(2).BackRect = new Rect(w / 2, h / 4 + padding, w - margin, h / 2);
            items.get(2).BackColor = green;

            items.get(3).BackRect = new Rect(margin, h / 2 + padding, w * 3 / 5 - padding, 3 * h / 4 - padding);
            items.get(3).BackColor = lightBlue;

            items.get(4).BackRect = new Rect(w * 3 / 5, h / 2 + padding, w - margin, 3 * h / 4 - padding);
            items.get(4).BackColor = orange;

            items.get(5).BackRect = new Rect(margin, 3 * h / 4, w / 3, h - 3 * margin);
            items.get(5).BackColor = yellow;

            items.get(6).BackRect = new Rect(w / 3 + padding, 3 * h / 4, w - margin, h - 3 * margin);
            items.get(6).BackColor = purple;
        } else {
            /****************************************** 八个模块 *********************************************/
            items.get(0).BackRect = new Rect(margin, margin, w / 2 - padding, h / 2);
            items.get(0).BackColor = blue;

            items.get(1).BackRect = new Rect(w / 2, margin, w - margin, h / 4);
            items.get(1).BackColor = lightBlue;

            items.get(2).BackRect = new Rect(w / 2, h / 4 + padding, w - margin, h / 2);
            items.get(2).BackColor = green;

            items.get(3).BackRect = new Rect(margin, h / 2 + padding, w * 3 / 5 - padding, 3 * h / 4 - padding);
            items.get(3).BackColor = lightBlue;

            items.get(4).BackRect = new Rect(w * 3 / 5, h / 2 + padding, w - margin, 3 * h / 4 - padding);
            items.get(4).BackColor = orange;

            items.get(5).BackRect = new Rect(margin, 3 * h / 4, w / 3, h - 3 * margin);
            items.get(5).BackColor = yellow;

            items.get(6).BackRect = new Rect(w / 3 + padding, 3 * h / 4, 2 * w / 3, h - 3 * margin);
            items.get(6).BackColor = purple;

            items.get(7).BackRect = new Rect(2 * w / 3 + padding, 3 * h / 4, w - margin, h - 3 * margin);
            items.get(7).BackColor = blue;
        }
    }
}
