package com.mapgis.mmt.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者 : zhoukang
 * 日期 : 2017-06-19  16:46
 * 说明 : 网络检查控件
 */

public class NetCheckView extends View {
    // 手机到网络之间检查
    public final static int CHECK_NET = 1;
    // 到服务器的检查
    public final static int CHECK_SERVER = 2;

    private static float mPointRadius = 3f;
    // 共三条线，SHORT_RATE两短线的长度，LONG_RATE长线的长度
    private final static float SHORT_RATE = 1.5f * mPointRadius;
    private final static float LONG_RATE = 2.0f * mPointRadius;

    // 百分比
    private final static float fraction = 0;

    // 底部角度
    private double mBtmRadians;
    // 原点的个数
    private int mPointCount;
    // 标记长线在所有point中对应的索引位置，<=-2或>=max point count + 2即可以隐藏
    private int index = -2;
    // 网络自检类型CHECK_NET，CHECK_SERVER
    private int checkType = 0;

    private float mIcon2PointSpace;

    private IconPaint mMobilePaint;
    private IconPaint mNetPaint;
    private IconPaint mServerPaint;

    private LinePaint mM2NPaint;
    private LinePaint mN2SPaint;

    private TextPaint mTextPaint;

    private AnimPaint mAnimPaint;

    public NetCheckView(Context context) {
        super(context);
        init();
    }

    public NetCheckView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NetCheckView);
        mTextPaint.setText((String) typedArray.getText(R.styleable.NetCheckView_text))
                .setTextSize(typedArray.getDimensionPixelSize(R.styleable.NetCheckView_textSize, 32))
                .setTextColor(typedArray.getColor(R.styleable.NetCheckView_textColor, Color.RED));

        mMobilePaint.setSrc(typedArray.getResourceId(R.styleable.NetCheckView_leftDrawable, R.drawable.mobile));
        mNetPaint.setSrc(typedArray.getResourceId(R.styleable.NetCheckView_topDrawable, R.drawable.net));
        mServerPaint.setSrc(typedArray.getResourceId(R.styleable.NetCheckView_rightDrawable, R.drawable.server));

        int color = typedArray.getColor(R.styleable.NetCheckView_leftLineColor, Color.BLUE);
        mM2NPaint.color = color;
        mM2NPaint.setColor(color);

        color = typedArray.getColor(R.styleable.NetCheckView_rightLineColor, Color.BLUE);
        mN2SPaint.color = color;
        mN2SPaint.setColor(color);

        mPointRadius = typedArray.getDimensionPixelSize(R.styleable.NetCheckView_pointRadius, 3);
        mIcon2PointSpace = typedArray.getDimensionPixelSize(R.styleable.NetCheckView_space, (int) (mMobilePaint.bitmap.getWidth() * 1f));

        typedArray.recycle();
    }

    public NetCheckView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMobilePaint = new IconPaint();
        mNetPaint = new IconPaint();
        mServerPaint = new IconPaint();

        mM2NPaint = new LinePaint();
        mN2SPaint = new LinePaint();

        mTextPaint = new TextPaint();

        mAnimPaint = new AnimPaint();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 计算图标的位置
        mMobilePaint.setPoint(getPaddingLeft() + 0f, getMeasuredHeight() - getPaddingBottom() - mMobilePaint.bitmap.getHeight());
        mNetPaint.setPoint((getMeasuredWidth() - mNetPaint.bitmap.getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft()
                , getPaddingTop() + 0f);
        mServerPaint.setPoint(getMeasuredWidth() - mServerPaint.bitmap.getWidth() - getPaddingRight()
                , getMeasuredHeight() - mServerPaint.bitmap.getHeight() - getPaddingTop());

        // 计算虚线的位置
        // 底部角的大小
        PointF mobileCenter = mMobilePaint.getPointCenter();
        PointF netCenter = mNetPaint.getPointCenter();
        PointF serverCenter = mServerPaint.getPointCenter();
        double height = Math.abs(mobileCenter.y - netCenter.y);
        double width = Math.abs(netCenter.x - mobileCenter.x);
        mBtmRadians = Math.atan(height / width);
        height = mIcon2PointSpace * Math.sin(mBtmRadians);
        width = mIcon2PointSpace * Math.cos(mBtmRadians);
        mM2NPaint.setPoint(mobileCenter.x + width, mobileCenter.y - height
                , netCenter.x - width, netCenter.y + height);
        mN2SPaint.setPoint(netCenter.x + width, netCenter.y + height
                , serverCenter.x - width, serverCenter.y - height);

        double length = Math.sqrt(Math.pow(mM2NPaint.startX - mM2NPaint.stopX, 2)
                + Math.pow(mM2NPaint.startY - mM2NPaint.stopY, 2));
        double result = length / (mPointRadius * 4);
        mPointCount = (int) result + 1;

        // 矫正虚线的位置
        double deviation = (result - (int) result) * mPointRadius * 4;
        if (deviation > 1f) {
            width = deviation / 2 * Math.cos(mBtmRadians);
            height = deviation / 2 * Math.sin(mBtmRadians);

            mM2NPaint.setPoint(mM2NPaint.startX + width, mM2NPaint.startY - height
                    , mM2NPaint.stopX - width, mM2NPaint.stopY + height);
            mN2SPaint.setPoint(mN2SPaint.startX + width, mN2SPaint.startY + height
                    , mN2SPaint.stopX - width, mN2SPaint.stopY - height);
        }

        // 计算文字的位置
        mTextPaint.setPoint((mobileCenter.x + serverCenter.x) / 2, (mobileCenter.y + serverCenter.y) / 2);
    }

    public void reset() {
        this.mM2NPaint.setColor(mM2NPaint.color);
        this.mN2SPaint.setColor(mN2SPaint.color);
        removeAnim();
    }

    public void removeAnim() {
        this.index = -2;
        invalidate();
    }

    /**
     * 绘制所有元素的位置
     */
    @Override
    protected void onDraw(Canvas canvas) {
        mTextPaint.draw(canvas);

        mMobilePaint.draw(canvas);
        mNetPaint.draw(canvas);
        mServerPaint.draw(canvas);

        mM2NPaint.draw(canvas);
        mN2SPaint.draw(canvas);

        if (checkType == CHECK_NET) {
            mAnimPaint.draw(canvas, mM2NPaint);
        } else if (checkType == CHECK_SERVER) {
            mAnimPaint.draw(canvas, mN2SPaint);
        }

    }

    public int getMaxCount() {
        return mPointCount;
    }

    public void setIndex(int index) {
        if (this.index == index) return;
        this.index = index;
        invalidate();
    }

    public void setLeftLineColor(int color) {
        this.mM2NPaint.setColor(color);
        invalidate();
    }

    public void setRightLineColor(int color) {
        this.mN2SPaint.setColor(color);
        invalidate();
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public void setText(String text) {
        mTextPaint.setText(text);
        invalidate();
    }

    /**
     * 图标画笔的包装类
     */
    private class IconPaint {
        private Paint paint;
        private Bitmap bitmap;
        private float x, y;

        IconPaint() {
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
        }

        IconPaint(@DrawableRes int resId) {
            this();
            this.bitmap = BitmapFactory.decodeResource(getResources(), resId);
        }

        void setSrc(@DrawableRes int resId) {
            this.bitmap = BitmapFactory.decodeResource(getResources(), resId);
        }

        void setPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        PointF getPointCenter() {
            return new PointF(x + bitmap.getWidth() / 2f, y + bitmap.getHeight() / 2f);
        }

        void draw(Canvas canvas) {
            canvas.drawBitmap(this.bitmap, x, y, paint);
        }
    }

    private class LinePaint {
        private Paint paint;
        private double startX, startY, stopX, stopY;
        private int color;
        private PointF[] pointArr;

        LinePaint() {
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        LinePaint(int color) {
            this();
            this.paint.setColor(color);
//            paint.setStrokeWidth(2);
        }

        void setPoint(double startX, double startY, double stopX, double stopY) {
            this.startX = startX;
            this.startY = startY;
            this.stopX = stopX;
            this.stopY = stopY;
        }

        int getColor() {
            return paint.getColor();
        }


        PointF getCenterPoint() {
            return new PointF((float) ((startX + stopX) / 2), (float) ((startY + stopY) / 2));
        }

        void setColor(int color) {
            this.paint.setColor(color);
        }

        void draw(Canvas canvas) {
            if (pointArr == null) {
                pointArr = new PointF[mPointCount];
                double dx, dy;
                if (startY > stopY) {
                    dx = mPointRadius * Math.cos(mBtmRadians) * 4;
                    dy = -mPointRadius * Math.sin(mBtmRadians) * 4;
                } else {
                    dx = mPointRadius * Math.cos(mBtmRadians) * 4;
                    dy = mPointRadius * Math.sin(mBtmRadians) * 4;
                }
//                double cx = (startX + stopX) / 2;
//                double cy = (startY + stopY) / 2;
                for (int i = 0; i < mPointCount; i++) {
                    pointArr[i] = new PointF((float) (startX + i * dx), (float) (startY + i * dy));
                }
            }

            for (PointF point : pointArr) {
                canvas.drawCircle(point.x, point.y, mPointRadius, paint);
            }
        }
    }

    private class AnimPaint {
        private Paint paint;

        AnimPaint() {
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paint.setStrokeWidth(mPointRadius * 2);
            this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        void draw(Canvas canvas, LinePaint linePaint) {
            this.paint.setColor(linePaint.getColor());
            PointF[] pointArr = linePaint.pointArr;
            if (pointArr == null || index < -1 || index > pointArr.length) return;

            int symbol = 1;
            if (linePaint.startY < linePaint.stopY) {
                symbol = -1;
            }
            float rate = fraction + 1;

            List<Float> list = new ArrayList<>();
            if (index - 1 >= 0 && index - 1 < pointArr.length) {
                PointF point = pointArr[index - 1];
                double dx = rate * SHORT_RATE * Math.sin(mBtmRadians);
                double dy = rate * SHORT_RATE * Math.cos(mBtmRadians) * symbol;
                list.add((float) (point.x - dx));
                list.add((float) (point.y - dy));
                list.add((float) (point.x + dx));
                list.add((float) (point.y + dy));
            }

            if (index >= 0 && index < pointArr.length) {
                PointF point = pointArr[index];
                double dx = rate * LONG_RATE * Math.sin(mBtmRadians);
                double dy = rate * LONG_RATE * Math.cos(mBtmRadians) * symbol;
                list.add((float) (point.x - dx));
                list.add((float) (point.y - dy));
                list.add((float) (point.x + dx));
                list.add((float) (point.y + dy));
            }

            if (index + 1 >= 0 && index + 1 < pointArr.length) {
                PointF point = pointArr[index + 1];
                double dx = rate * SHORT_RATE * Math.sin(mBtmRadians);
                double dy = rate * SHORT_RATE * Math.cos(mBtmRadians) * symbol;
                list.add((float) (point.x - dx));
                list.add((float) (point.y - dy));
                list.add((float) (point.x + dx));
                list.add((float) (point.y + dy));
            }

            float[] array = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            canvas.drawLines(array, paint);
        }
    }

    private class TextPaint {
        private Paint paint;
        private String msg;
        private String text;
        private PointF point = new PointF(0, 0);

        TextPaint() {
            this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.paint.setTextAlign(Paint.Align.CENTER);
        }

        TextPaint(String str) {
            this();
            this.msg = str;
        }

        TextPaint setText(String text) {
            this.msg = text;
            return this;
        }

        TextPaint setTextColor(int color) {
            this.paint.setColor(color);
            return this;
        }

        TextPaint setTextSize(int textSize) {
            this.paint.setTextSize(textSize);
            return this;
        }

        void setPoint(float x, float y) {
            this.point = new PointF(x, y);
        }

        void draw(Canvas canvas) {
            if (BaseClassUtil.isNullOrEmptyString(msg)) {
                return;
            }
            // 计算最终要显示的文本内容
            PointF mobileCenter = mMobilePaint.getPointCenter();
            PointF serverCenter = mServerPaint.getPointCenter();
            double dist = Math.abs(mobileCenter.x - serverCenter.x);
            final double maxSpace = dist - mIcon2PointSpace * 2;

            int index = msg.length();
            Rect bound = new Rect();
            do {
                this.paint.getTextBounds(msg, 0, index, bound);

                // 当文本宽度大于实际可用空间的大小，则将后面的文本替换为".."
                index--;
            } while (maxSpace < bound.width());
            if (index != msg.length() - 1) {
                text = msg.substring(0, index - 1) + "..";
            } else {
                text = msg;
            }
            // 文本居中绘制
            canvas.drawText(this.text, point.x, point.y + bound.height() / 2f, paint);
        }
    }
}
