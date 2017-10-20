package com.mapgis.mmt.module.systemsetting.itemwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;

/**
 * Created by Comclay on 2017/3/22.
 * 可以点击的条目
 */

public class MoreItemSettingView extends RelativeLayout {
    private ImageView mIcon = null;
    private TextView mTvTitle = null;
    private TextView mTvMsg = null;
    private TextView mTvRightMsg = null;
    private ImageView mIconMore = null;

    public MoreItemSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.item_more_setting_view, this);
        mIcon = (ImageView) this.findViewById(R.id.icon);
        mTvTitle = (TextView) this.findViewById(R.id.tv_title);
        mTvMsg = (TextView) this.findViewById(R.id.tv_message);
        mIconMore = (ImageView) this.findViewById(R.id.iv_more);
        mTvRightMsg = (TextView) this.findViewById(R.id.tv_right_msg);

        // 默认属性
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dimen_48dp));
        this.setBackgroundResource(R.drawable.setting_item_bg);
        this.setPadding(0, getResources().getDimensionPixelSize(R.dimen.dimen_8dp)
                , 0, getResources().getDimensionPixelSize(R.dimen.dimen_5dp));

        // 自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MoreItemSettingView);
        Drawable drawable = typedArray.getDrawable(R.styleable.MoreItemSettingView_icon);
        String title = typedArray.getString(R.styleable.MoreItemSettingView_text_title);
        String message = typedArray.getString(R.styleable.MoreItemSettingView_text_message);
        String rightMessage = typedArray.getString(R.styleable.MoreItemSettingView_text_right_message);
        boolean visiable = typedArray.getBoolean(R.styleable.MoreItemSettingView_iconMoreVisiable, true);
        boolean iconVisiable = typedArray.getBoolean(R.styleable.MoreItemSettingView_iconVisiable, false);
        boolean messageVisiable = typedArray.getBoolean(R.styleable.MoreItemSettingView_messageVisiable, false);
        boolean rightMessageVisiable = typedArray.getBoolean(R.styleable.MoreItemSettingView_rightMessageVisiable, true);

        this.mIcon.setImageDrawable(drawable);
        this.mTvTitle.setText(title == null ? "" : title);
        this.mTvMsg.setText(message == null ? "" : message);
        this.mTvRightMsg.setText(rightMessage == null ? "" : rightMessage);
        this.mIconMore.setVisibility(visiable ? VISIBLE : GONE);
        this.mIcon.setVisibility(iconVisiable ? VISIBLE : GONE);
        this.mTvMsg.setVisibility(messageVisiable ? VISIBLE : GONE);
        this.mTvRightMsg.setVisibility(rightMessageVisiable ? VISIBLE : GONE);
        typedArray.recycle();
    }

    /**
     * 设置标题
     *
     * @param charSequence 标题内容
     */
    public void setTitle(@NonNull CharSequence charSequence) {
        this.mTvTitle.setText(charSequence);
    }

    /**
     * 设置描述信息
     *
     * @param charSequence 描述信息内容
     */
    public void setMessage(@NonNull CharSequence charSequence) {
        this.mTvMsg.setText(charSequence);
    }

    /**
     * 设置右边的描述信息
     */
    public void setRightMessage(CharSequence charSequence) {
        this.mTvRightMsg.setText(charSequence);
    }

    /**
     * 返回右边的TextView
     */
    public TextView getRightMessage(){
        return this.mTvRightMsg;
    }

    /**
     * 设置图标
     *
     * @param id 图标资源ID
     */
    public void setIcon(@DrawableRes int id) {
        this.mIcon.setImageResource(id);
    }

    /**
     * 设置描述内容的可见性
     *
     * @param visiable 可见性
     */
    public void setMessageVisiable(int visiable) {
        this.mTvMsg.setVisibility(visiable);
    }

    /**
     * 设置图标的可见性
     *
     * @param visiable 可见性
     */
    public void setIconVisiable(int visiable) {
        this.mIcon.setVisibility(visiable);
    }
}
