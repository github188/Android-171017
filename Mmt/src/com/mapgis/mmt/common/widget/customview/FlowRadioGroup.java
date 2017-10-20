package com.mapgis.mmt.common.widget.customview;

/**
 * Created by liuyunfan on 2016/1/13.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式布局的RadioGroup
 */
public class FlowRadioGroup extends RadioGroup {
    private int colsCount = 2;
    private List<RadioButton> radioButtons;
    private int checkedIndex = 0;
    private List<Integer> row1Lefts = new ArrayList<>();

    public FlowRadioGroup(Context context) {
        super(context);
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowRadioGroup(Context context, List<RadioButton> radioButtons, int colsCount) {
        this(context);
        this.radioButtons = radioButtons;
        this.colsCount = colsCount;
        addToContainer();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        int x = 0;
        int y = 0;
        int row = 0;

        for (int index = 0; index < childCount; index++) {
            final View child = getChildAt(index);
            if (child.getVisibility() != View.GONE) {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                // 此处增加onlayout中的换行判断，用于计算所需的高度
                int width = child.getMeasuredWidth();
                if (colsCount > 1) {
                    width = Math.max(width, maxWidth / colsCount);
                }
                int height = child.getMeasuredHeight();
                x += width;
                y = row * height + height;
                if (colsCount > 1) {
                    if (x > maxWidth || (index != 0 && index % colsCount == 0)) {
                        x = width;
                        row++;
                        y = row * height + height;
                    }
                } else {
                    if (x > maxWidth) {
                        x = width;
                        row++;
                        y = row * height + height;
                    }
                }
            }
        }
        // 设置容器所需的宽度和高度
        setMeasuredDimension(maxWidth, y);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        int maxWidth = r - l;
        int splidMaxWidth = maxWidth;
        if (colsCount > 0) {
            splidMaxWidth = maxWidth / colsCount;
        }
        int x = 0;
        int y = 0;
        int row = 0;
        for (int i = 0; i < childCount; i++) {

            final View child = this.getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int width = child.getMeasuredWidth();
                int clonWidth = width;
                width = Math.max(width, splidMaxWidth);
                int height = child.getMeasuredHeight();
                x += width;
                y = row * height + height;
                if (colsCount > 1) {
                    if (x > maxWidth || (i != 0 && i % colsCount == 0)) {
                        x = width;
                        row++;
                        y = row * height + height;
                    }
                } else {
                    if (x > maxWidth) {
                        x = width;
                        row++;
                        y = row * height + height;
                    }
                }
                if (i < colsCount) {
                    row1Lefts.add(x - width + (splidMaxWidth - clonWidth) / colsCount);
                }

                if (colsCount > 1) {
                    child.layout(row1Lefts.get(i % colsCount), y - height, x, y);
                } else {
                    child.layout(x - width, y - height, x, y);
                }

                //左边和第一行的对应的一个对齐
                //后续开发
            }
        }
    }

    public void addToContainer() {
        if (radioButtons != null) {
            for (RadioButton rb : radioButtons) {
                addView(rb);
            }
            View view = getChildAt(checkedIndex);
            if (view instanceof RadioButton) {
                ((RadioButton) view).setChecked(true);
            }
        }
    }
}