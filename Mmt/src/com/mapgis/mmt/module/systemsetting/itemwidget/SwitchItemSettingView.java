package com.mapgis.mmt.module.systemsetting.itemwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.SwitchImageButton;

/**
 * Created by Comclay on 2017/3/22.
 * 可以选择的设置条目
 */

public class SwitchItemSettingView extends RelativeLayout {
    private ImageView mIcon = null;
    private TextView mTvTitle = null;
    private TextView mTvMsg = null;
    private SwitchImageButton mSwitchButton = null;

    public SwitchItemSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.item_switch_setting_view, this);
        mIcon = (ImageView) this.findViewById(R.id.icon);
        mTvTitle = (TextView) this.findViewById(R.id.tv_title);
        mTvMsg = (TextView) this.findViewById(R.id.tv_message);
        mSwitchButton = (SwitchImageButton) this.findViewById(R.id.switchButton);

        // 默认属性
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.dimen_48dp));
        this.setBackgroundResource(R.drawable.setting_item_bg);
        this.setPadding(0, getResources().getDimensionPixelSize(R.dimen.dimen_8dp)
                , 0, getResources().getDimensionPixelSize(R.dimen.dimen_5dp));

        // 自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchItemSettingView);
        Drawable drawable = typedArray.getDrawable(R.styleable.SwitchItemSettingView_icon);
        String title = typedArray.getString(R.styleable.SwitchItemSettingView_text_title);
        String message = typedArray.getString(R.styleable.SwitchItemSettingView_text_message);
        boolean checked = typedArray.getBoolean(R.styleable.SwitchItemSettingView_checked, false);
        boolean iconVisiable = typedArray.getBoolean(R.styleable.SwitchItemSettingView_iconVisiable, false);
        boolean messageVisiable = typedArray.getBoolean(R.styleable.SwitchItemSettingView_messageVisiable, false);

        this.mIcon.setImageDrawable(drawable);
        this.mTvTitle.setText(title == null ? "" : title);
        this.mTvMsg.setText(message == null ? "" : message);
        this.mSwitchButton.setChecked(checked);
        this.mIcon.setVisibility(iconVisiable ? VISIBLE : GONE);
        this.mTvMsg.setVisibility(messageVisiable ? VISIBLE : GONE);
        typedArray.recycle();
    }

    /**
     * SwitchButton的可见性
     */

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.mSwitchButton.setOnCheckedChangeListener(listener);
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

    /**
     * 设置switchbutton的选中状态
     *
     * @param checked true开，false关
     */
    public void setSwitchChecked(boolean checked) {
        this.mSwitchButton.setChecked(checked);
    }

    public SwitchImageButton getSwitchButton(){
        return this.mSwitchButton;
    }
}
