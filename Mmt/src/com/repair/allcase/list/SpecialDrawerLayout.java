package com.repair.allcase.list;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;

public class SpecialDrawerLayout extends DrawerLayout {

    public SpecialDrawerLayout(Context context) {
        super(context);
    }

    public SpecialDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpecialDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {
            // 取第二层Drawer, demo里面最后一个子View就是第二层Drawer
            final int childCount = getChildCount();
            final View child = getChildAt(childCount - 1);
            // 源码的默认值是64dp, 为了方便直接写死
            final float density = getResources().getDisplayMetrics().density;
            int minDrawerMargin = (int) (300 * density + 0.5f);
            // 以下代码直接取自DrawerLayout#onMeasure
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                    minDrawerMargin + lp.leftMargin + lp.rightMargin, lp.width);
            final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                    lp.topMargin + lp.bottomMargin, lp.height);
            child.measure(drawerWidthSpec, drawerHeightSpec);
        }

    }
}
