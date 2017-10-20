package com.mapgis.mmt.common.widget.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiSwitchButton extends View implements OnTouchListener {
    private final Context context;
    /**
     * 可供选择的数目
     */
    private int selectRectCount = 4;

    private final Paint whitePaint;
    private final Paint blackPaint;
    private final Paint bluePaint;
    private final Paint blackPaintTxt;

    /**
     * 外边框矩形
     */
    private Rect parentRect;

    /**
     * 移动的矩形框
     */
    private Rect moveRect;

    /**
     * 指尖移动前所处的X起始点
     */
    private float tagStartX;

    /**
     * 是否在滑动
     */
    private boolean isMove;

    /**
     * 所要显示的文本信息
     */
    private List<String> content;

    /**
     * 当前所处的区域
     */
    private int index = 0;

    private OnScrollListener onScrollListener;

    public MultiSwitchButton(Context context) {
        this(context, null);
}

    public MultiSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        whitePaint = new Paint();
        whitePaint.setTextSize(DimenTool.dip2px(context, 15));
        whitePaint.setFakeBoldText(true);
        whitePaint.setAntiAlias(true);
        whitePaint.setFilterBitmap(true);
        whitePaint.setColor(getResources().getColor(R.color.white));

        blackPaint = new Paint();
        blackPaint.setTextSize(DimenTool.dip2px(context, 15));
        blackPaint.setFakeBoldText(true);
        blackPaint.setAntiAlias(true);
        blackPaint.setFilterBitmap(true);
        blackPaint.setStyle(Style.STROKE);
        blackPaint.setColor(getResources().getColor(R.color.black));

        bluePaint = new Paint();
        bluePaint.setTextSize(DimenTool.dip2px(context, 15));
        bluePaint.setFakeBoldText(true);
        //    bluePaint.setColor(getResources().getColor(R.color.progressbar_blue));
        bluePaint.setColor(getResources().getColor(AppStyle.getSwitchFragmentStyleResource()));

        blackPaintTxt = new Paint();
        blackPaintTxt.setTextSize(DimenTool.dip2px(context, 15));
        blackPaintTxt.setFakeBoldText(true);
        blackPaintTxt.setAntiAlias(true);
        blackPaintTxt.setFilterBitmap(true);
        blackPaintTxt.setStyle(Style.FILL);
        blackPaintTxt.setColor(getResources().getColor(R.color.black));
        setOnTouchListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);

            if (parentRect == null) {
                parentRect = new Rect();

                parentRect.left = getDipSize(18);
                parentRect.top = getDipSize(13);
                parentRect.right = getWidth() - getDipSize(18);
                parentRect.bottom = getHeight() - getDipSize(13);
            }

            if (moveRect == null) {
                moveRect = new Rect();

                moveRect.left = parentRect.left;
                moveRect.top = parentRect.top;
                moveRect.right = parentRect.left + parentRect.width() / selectRectCount;
                moveRect.bottom = parentRect.bottom;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(parentRect, whitePaint);

        canvas.drawRect(parentRect, blackPaint);

        canvas.drawRect(moveRect, bluePaint);

        if (content == null || content.size() == 0) {
            return;
        }

        // 绘制文字
        for (int i = 0; i < selectRectCount; i++) {

            int parentPartStartX = parentRect.left + parentRect.width() * i / selectRectCount;

            float leftTextWidth = blackPaint.measureText(content.get(i));

            canvas.drawText(content.get(i), parentPartStartX + parentRect.width() / selectRectCount / 2 - leftTextWidth / 2,
                    parentRect.top + parentRect.height() / 2 + DimenTool.dip2px(context, 15) / 3, index == i ? whitePaint
                            : blackPaintTxt);
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                tagStartX = event.getX();

                if (moveRect.contains((int) event.getX(), (int) event.getY())) {
                    isMove = true;
                }

                break;
            case MotionEvent.ACTION_UP:

                if (!isMove) {
                    // 点击移动矩形块
                    for (int m = selectRectCount - 1; m >= 0; m--) {
                        int parentPartStartX = parentRect.left + parentRect.width() * m / selectRectCount;

                        if (event.getX() >= parentPartStartX) {
                            moveRect.left = parentPartStartX;

                            index = m;

                            break;
                        }
                    }

                } else {
                    // 滑动移动矩形块
                    for (int i = selectRectCount - 1; i >= 0; i--) {
                        int parentPartStartX = parentRect.left + parentRect.width() * i / selectRectCount;

                        if (moveRect.left >= parentPartStartX) {

                            int parentPartCenterX = parentPartStartX + parentRect.width() / (selectRectCount * 2);

                            // 超过分段的中间线，则自动后移，否则保持原来位置
                            if (moveRect.left < parentPartCenterX) {

                                moveRect.left = parentPartStartX;

                                index = i;

                            } else {

                                moveRect.left = parentPartStartX + parentRect.width() / selectRectCount;

                                index = i + 1;
                            }

                            break;
                        }
                    }
                }

                isMove = false;

                moveRect.top = parentRect.top;
                moveRect.right = moveRect.left + parentRect.width() / selectRectCount;
                moveRect.bottom = parentRect.bottom;

                if (onScrollListener != null) {
                    onScrollListener.OnScrollComplete(index);
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (isMove) {

                    float downX = event.getX();

                    moveRect.left = (int) (moveRect.left + downX - tagStartX);

                    tagStartX = downX;

                    if (moveRect.left < parentRect.left) {
                        moveRect.left = parentRect.left;
                    } else if (moveRect.left > parentRect.right - parentRect.width() / selectRectCount) {
                        moveRect.left = parentRect.right - parentRect.width() / selectRectCount;
                    }

                    moveRect.top = parentRect.top;
                    moveRect.right = moveRect.left + parentRect.width() / selectRectCount;
                    moveRect.bottom = parentRect.bottom;

                    break;
                }
        }

        postInvalidate();

        return true;

    }

    private int getDipSize(int size) {
        return DimenTool.dip2px(context, size);
    }

    /**
     * 获取滑动标题名称列表
     */
    public List<String> getContent() {
        return content;
    }

    /**
     * 偏移滑动矩形框
     *
     * @param arg0 当前第几页
     * @param arg1 当前的偏移量百分比
     */
    public void moveRectLeft(int arg0, float arg1) {
        if (moveRect != null) {

            int unitWidth = parentRect.width() / selectRectCount;

            float vWidth = unitWidth * arg1;

            moveRect.left = (int) (parentRect.left + arg0 * unitWidth + vWidth);
            moveRect.top = parentRect.top;
            moveRect.right = moveRect.left + parentRect.width() / selectRectCount;
            moveRect.bottom = parentRect.bottom;

            postInvalidate();
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void setCurrentItem(int index) {
        this.index = index;
    }

    public int getCurrentItem() {
        return index;
    }

    public void setContent(String[] content) {
        this.selectRectCount = content.length;
        this.content = new ArrayList<String>();
        this.content.addAll(Arrays.asList(content));
        postInvalidate();
    }

    public void setContent(List<String> content) {
        this.selectRectCount = content.size();
        this.content = content;
        postInvalidate();
    }

    public interface OnScrollListener {
        void OnScrollComplete(int index);
    }
}
