package com.mapgis.mmt.module.navigation.circular;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;

public class CircleLayoutItemView extends LinearLayout {

    private ImageView mImageView;
    private TextView mTextView;

    private String menuName;

    public CircleLayoutItemView(Context context) {
        this(context, null);
    }

    public CircleLayoutItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayoutItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }



    private void init() {

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        LayoutInflater.from(getContext()).inflate(R.layout.circle_menu_item, this, true);

        this.mImageView = (ImageView) findViewById(R.id.menu_image);
        this.mTextView = (TextView) findViewById(R.id.menu_text);
    }

    public void setMenuIconAndText(int resId, String text) {
        mImageView.setImageResource(resId);
        menuName = text;
        mTextView.setText(menuName);
    }

    public String getMenuName() {
        return menuName;
    }

    public void startAnimSet(final AnimationCallBack callBack){
        AnimationSet set = new AnimationSet(false);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        set.addAnimation(alphaAnimation);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 20f, 1.0f, 20f,0.5f,0.5f);
        set.addAnimation(scaleAnimation);

        set.setDuration(500);
        set.setFillAfter(true);
        set.setRepeatCount(0);
        this.startAnimation(set);

        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 当动画执行完毕之后执行界面的跳转操作
                callBack.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public interface AnimationCallBack {
        // 动画执行后
        void onAnimationEnd(Animation animation);
    }

    public void setTextVisibility(int visibility){
        mTextView.setVisibility(visibility);
    }

    /**
     * 重置菜单的位置,并将菜单名称设置为可见
     * @param orignalX
     * @param orignalY
     */
    public void reset(int orignalX,int orignalY){
        this.layout(orignalX,orignalY,orignalX+this.getWidth(),orignalY+this.getHeight());
//        this.scrollTo(orignalX,orignalY);
        this.setTextVisibility(View.VISIBLE);
        this.setVisibility(View.VISIBLE);
        this.setAlpha(1.0f);
    }

}
