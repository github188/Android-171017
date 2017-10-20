package com.mapgis.mmt.module.navigation.circular;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.R;

public class SemiCircleLayout extends ViewGroup {

    private final int defShowCount = 7;
    private int maxShowCount;

    // Child sizes
    private int maxChildWidth = 0;
    private int maxChildHeight = 0;

    // Sizes of the ViewGroup
    private int circleWidth, circleHeight;

    private float radius = 0;
    private float centerPointOffsetX;
    private float centerPointOffsetY;

    private float angle = 90;

    public float getCenterPointOffsetY() {
        return centerPointOffsetY;
    }

    public float getCenterPointOffsetX() {
        return centerPointOffsetX;
    }

    public float getRadius() {
        return radius;
    }

    public int getMaxShowCount() {
        return maxShowCount;
    }

    public SemiCircleLayout(Context context) {
        this(context, null);
    }

    public SemiCircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SemiCircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = null;
        try {
            typedArray  = context.obtainStyledAttributes(attrs, R.styleable.SemiCircleLayout);

            if (typedArray.hasValue(R.styleable.SemiCircleLayout_zh_maxShowCount)) {
                maxShowCount = typedArray.getInt(R.styleable.SemiCircleLayout_zh_maxShowCount, defShowCount);
            }
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }

        // Needed for the ViewGroup to be drawn
        setWillNotDraw(false);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);

//        child.setOnClickListener(itemClickListener);
//        child.setOnTouchListener(menuTouchListener);
    }

//    private final View.OnClickListener itemClickListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (onItemClickListener != null) {
//                onItemClickListener.onItemClick(v);
//            }
//        }
//    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Measure child views first
        maxChildWidth = 0;
        maxChildHeight = 0;

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
            maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
        }

        // Then decide what size we want to be
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(widthSize, heightSize);
        } else {
            //Be whatever you want
            width = maxChildWidth * 3;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(heightSize, widthSize);
        } else {
            //Be whatever you want
            height = maxChildHeight * 3;
        }

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int layoutWidth = r - l;
        int layoutHeight = b - t;

        radius = layoutHeight * 3 / 10;

        circleHeight = getHeight();
        circleWidth = getWidth();

        centerPointOffsetX = circleWidth * 0.10236f;
        centerPointOffsetY = circleHeight * 0.44949f;

        setChildAngles();
    }



    private void setChildAngles() {

        int left, top, childWidth, childHeight;
        int childCount = getChildCount();

        float angleDelay = 180.0f / (childCount - 1);
        float localAngle = angle;

        for (int i = 0; i < childCount; i++) {

            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            if (localAngle > 360) {
                localAngle -= 360;
            } else if (localAngle < 0) {
                localAngle += 360;
            }

            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            left = Math.round((float) (centerPointOffsetX + radius * Math.cos(Math.toRadians(localAngle))));
            top = Math.round((float) (centerPointOffsetY - radius * Math.sin(Math.toRadians(localAngle))));

            child.setTag(localAngle);

            child.layout(left, top, left + childWidth, top + childHeight);

            localAngle -= angleDelay;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
