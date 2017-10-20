package com.repair.zhoushan.module.flowcenter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * GridView不换行
 */
public class CusGridView extends GridView {


    public CusGridView(Context context) {
        super(context);
    }

    public CusGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
