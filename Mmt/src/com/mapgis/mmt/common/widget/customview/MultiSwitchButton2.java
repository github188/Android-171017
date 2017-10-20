package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.R;

/**
 * 作者: 周康
 * <p/>
 * 日期: 2016-08-16 15:13
 * <p/>
 * SwitchButton: 自定义TabLayout控件
 */
public class MultiSwitchButton2 extends HorizontalScrollView {
    /**
     * 上下文菜单
     */
    private Context mContext;
    // 标题栏内容集合
    private String [] titles;
    // 当前选中的item的索引
    private int mCurrentIndex = 0;
    // 标题栏中内容最长的索引
    private int maxLengthIndex = 0;
    // LinearLayout布局
    private MyLinearLayout myLinearLayout;
    // 当标题的个数小于3时，就采用平均值的方式计算每个Item的宽度
    // ，否则取其中字符串长度最大的item的宽度作为所有item的宽度
    private final static int FLAG_TAB_COUNT = 3;
    // 绘制内边界
    private Paint mPaintBound;
    private Rect mRect;
    // 当前事件状态
    private int mEventType;
    // 每个item的宽度
    private int maxChildWidth;
    // 监听器对象
    private OnItemChangedListener listener;

    // 选中时的颜色
    private static final int COLOR_SELECTED = Color.WHITE;
    // 没有选中时的颜色
    private static final int COLOR_NOT_SELECTED = Color.BLACK;
    // 矩形颜色块的颜色
    private int TAB_CHECKED_BG_COLOR = getResources().getColor(AppStyle.getSwitchFragmentStyleResource());
    // 字体的大小
    private static float TAB_TEXT_SIZE = 14;


    public MultiSwitchButton2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        myLinearLayout = new MyLinearLayout(mContext);
//        myLinearLayout.setBackgroundColor(Color.GREEN);
        myLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        myLinearLayout.setBaselineAligned(false);
        myLinearLayout.setLayoutParams(
                new LayoutParams(LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(myLinearLayout);

        this.setOverScrollMode(HorizontalScrollView.OVER_SCROLL_NEVER);
        this.setHorizontalScrollBarEnabled(false);
        int padding = (int) getResources().getDimension(R.dimen.padding_tablayout);
        this.setPadding(padding, padding, padding, padding);
        this.setBackgroundResource(R.drawable.shape_tab_bound_bg);

        mPaintBound = new Paint();
        // stroke 空心矩形框，fill 实心矩形框
        mPaintBound.setStyle(Paint.Style.STROKE);
        mPaintBound.setStrokeWidth(1);
        mPaintBound.setAntiAlias(true);
        mPaintBound.setColor(Color.RED);
    }

    /**
     * 添加内容
     * @param titles 标题
     */
    public void setContent(String [] titles){
        this.titles = titles;
        addItemView(titles);
    }

    private void addItemView(String[] titles) {
        TextView tv;
        for (int i = 0; i < titles.length; i++) {
            if (titles[i].length() > titles[maxLengthIndex].length()){
                maxLengthIndex = i;
            }

            tv = new TextView(mContext);
            tv.setText(titles[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TAB_TEXT_SIZE);
            tv.setClickable(true);
            tv.setFocusable(true);

            if (i == mCurrentIndex){
                tv.setTextColor(COLOR_SELECTED);
            }else{
                tv.setTextColor(COLOR_NOT_SELECTED);
            }

            tv.setTag(i);
            tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEventType == MotionEvent.INVALID_POINTER_ID){
                        return;
                    }
                    int index = (Integer)v.getTag();
                    setSelected(index);
//                    mCurrentIndex = (Integer) v.getTag();
//                    System.out.println("我监听到点击事件了：" + mCurrentIndex);
//                    myLinearLayout.invalidate();
                }
            });
            myLinearLayout.addView(tv);
        }
    }

    /*
     * 当一系列事件中的某个事件被拦截了，那么从这个事件之后的所有事件都不会继续往下分发了
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:

                // 如果触摸到的是在Rect区域，则要将事件分发下去否则按照默认的值来
                View view = myLinearLayout.getChildAt(mCurrentIndex);
                int [] pos = new int[2];
                if (view == null){
                    break;
                }
                view.getLocationOnScreen(pos);
                if (ev.getRawX() > pos[0] && ev.getRawX() < pos[0] + view.getWidth()){
                    firstX = 0f;
                    isMoveRect = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                // 如果触摸到的是在Rect区域，则要将事件分发下去否则按照默认的值来
//                View view = myLinearLayout.getChildAt(mCurrentIndex);
//                int [] pos = new int[2];
//                view.getLocationOnScreen(pos);
//                if (ev.getRawX() > pos[0] && ev.getRawX() < pos[0] + view.getWidth()){
//                    isMoveRect = true;
//                    return true;
//                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private float dx;
    float firstX = 0f;
    private boolean isMoveRect = false;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_MOVE:
                /*
                 * 当触摸到的是Rect颜色块，则用自己的方式处理ACTION_MOVE事件
                 * 否则，采用默认的处理方式
                 */
                if (!isMoveRect){
                    break;
                }

                if (firstX == 0){
                    firstX = ev.getRawX();
                }
                dx = ev.getRawX() - firstX;
                firstX = ev.getRawX();

                /*
                 * 当Rect颜色块滑动到了LinearLayou的头和尾部，且仍在往两头滑动，就不在处理滑动事件
                 */
                if ((mRect.left == 0 && dx < 0) || (mRect.right == myLinearLayout.getWidth() && dx > 0)){
                    return true;
                }

                if (mRect.left + dx < 0){
                    dx = 0-mRect.left;
                }else if (mRect.right + dx > myLinearLayout.getWidth()) {
                    dx = myLinearLayout.getWidth() - mRect.right;
                }

                scrollColorRect();
                return true;
            case MotionEvent.ACTION_UP:
                if (!isMoveRect){
                    break;
                }
                scrollRectOver();

                break;
        }
        return super.onTouchEvent(ev);
    }

    public void scrollRectOver() {
        //                    mCurrentIndex = mRect.left / maxChildWidth +
//                            (mRect.left % maxChildWidth > maxChildWidth / 2 ? 1 : 0);
        mEventType = MotionEvent.ACTION_UP;
        myLinearLayout.invalidate();
        isMoveRect = false;
        if (listener != null){
            listener.onChanged(mCurrentIndex);
        }
    }

    private void scrollColorRect() {
        int tempIndex = mRect.left / maxChildWidth +
                (mRect.left % maxChildWidth > maxChildWidth / 2 ? 1 : 0);
        if (mCurrentIndex != tempIndex){
            ((TextView)myLinearLayout.getChildAt(mCurrentIndex)).setTextColor(COLOR_NOT_SELECTED);
            ((TextView)myLinearLayout.getChildAt(tempIndex)).setTextColor(COLOR_SELECTED);
            mCurrentIndex = tempIndex;
        }

        mEventType = MotionEvent.ACTION_MOVE;
        myLinearLayout.invalidate();
    }

    /**
     * 滑动颜色巨型块
     * @param dx x方向的位移差
     */
    public void scrollColorRect(int dx){
        this.dx = dx;
        scrollColorRect();
    }

    /**
     * 根据百分比来滑动巨型颜色块
     * @param ratio 相对于颜色块的百分比
     */
    public void scrollColorRect(float ratio){
        this.dx = ratio * mRect.width();
        scrollColorRect();
    }

    /**
     * 设置index选项卡为选中
     * @param index 索引
     */
    public void setSelected(int index){
        if (index < 0 || index > titles.length - 1){
            throw new IndexOutOfBoundsException("不存在索引为" + index +"的选项卡");
        }

        if (mCurrentIndex == index/* && mCurrentIndex != 0*/){
            return;
        }

        ((TextView)myLinearLayout.getChildAt(index)).setTextColor(COLOR_SELECTED);
        ((TextView)myLinearLayout.getChildAt(mCurrentIndex)).setTextColor(COLOR_NOT_SELECTED);
        mCurrentIndex = index;
        myLinearLayout.invalidate();

        if (listener != null){
            listener.onChanged(mCurrentIndex);
        }

    }

    /**
     * 设置监听事件
     * @param listener
     */
    public void setOnItemChangedListener(OnItemChangedListener listener){
        this.listener = listener;
    }

    /**
     * 监听器
     */
    public interface OnItemChangedListener{
        void onChanged(int currentIndex);
    }

    /**
     * 自定义的LinearLayout
     */
    class MyLinearLayout extends LinearLayout{
        private Paint mPaint;

        public MyLinearLayout(Context context) {
            super(context);

            setWillNotDraw(false);

            initData();
        }

        private void initData() {
            mPaint = new Paint();
            mPaint.setAlpha(255);
            mPaint.setColor(TAB_CHECKED_BG_COLOR);
            mPaint.setAntiAlias(true);
            // fill表示绘制实心图形，填充
            mPaint.setStyle(Paint.Style.FILL);
            mRect = new Rect();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int remainder = 0;
//            View maxAreaView  = getChildAt(maxLengthIndex);
            if (titles.length <= FLAG_TAB_COUNT && titles.length > 0){
                maxChildWidth = Math.round((MultiSwitchButton2.this.getMeasuredWidth()
                        - MultiSwitchButton2.this.getPaddingLeft()
                        - MultiSwitchButton2.this.getPaddingRight()) / titles.length);

                remainder = (MultiSwitchButton2.this.getMeasuredWidth()
                        - MultiSwitchButton2.this.getPaddingLeft()
                        - MultiSwitchButton2.this.getPaddingRight()) % titles.length;
            }else if(titles.length > FLAG_TAB_COUNT){
//                maxChildWidth = maxAreaView.getMeasuredWidth();
                maxChildWidth = Math.round((MultiSwitchButton2.this.getMeasuredWidth()
                        - MultiSwitchButton2.this.getPaddingLeft()
                        - MultiSwitchButton2.this.getPaddingRight())/FLAG_TAB_COUNT);

                remainder = (MultiSwitchButton2.this.getMeasuredWidth()
                        - MultiSwitchButton2.this.getPaddingLeft()
                        - MultiSwitchButton2.this.getPaddingRight())/FLAG_TAB_COUNT;
            }

            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                params.width = maxChildWidth;
                if (i == getChildCount() - 1){
                    /*
                     * 当i为最后一个item时，加上计算的余数
                     */
                    params.width += remainder;
                }
                view.setLayoutParams(params);
            }
//            int measureWidthSpec = MeasureSpec.makeMeasureSpec(maxChildWidth,MeasureSpec.EXACTLY);
//            int measureHeightSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredHeight(),MeasureSpec.EXACTLY);
//            measureChildren(measureWidthSpec,measureHeightSpec);
//
//            invalidate();

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t,r , b);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mEventType != MotionEvent.ACTION_MOVE){
                View view = getChildAt(mCurrentIndex);
                if (view == null){
                    return;
                }
                mRect.left = view.getLeft();
                mRect.top = view.getTop();
                mRect.right = view.getRight();
                mRect.bottom = view.getBottom();
            }else{
                // 如果当前为滑动事件
                mRect.left += Math.round(dx);
                mRect.right += Math.round(dx);
            }

            canvas.drawRect(mRect,mPaint);
        }
    }
}
