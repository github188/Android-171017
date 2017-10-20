package com.mapgis.mmt.module.systemsetting.setting;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import com.mapgis.mmt.module.systemsetting.itemwidget.SwitchItemSettingView;

/**
 * Created by Comclay on 2017/6/14.
 *
 * 包含SwitchButton的设置项的抽象基类，默认实现OnCheckedChangeListener监听
 */

public abstract class BaseSwitchSettingItem implements ISettingItem, CompoundButton.OnCheckedChangeListener {

    // 该项设置是否可见
    protected Context mContext;
    protected boolean isVisible = true;
    protected SwitchItemSettingView mItemView;

    public BaseSwitchSettingItem(Context context, View itemView) {
        this(context, itemView, true);
    }

    public BaseSwitchSettingItem(Context context, View itemView, boolean isVisible) {
        this.mContext = context;
        if (itemView instanceof SwitchItemSettingView) {
            this.mItemView = (SwitchItemSettingView) itemView;
        } else {
            throw new IllegalArgumentException("itemView must be instanceof MoreItemSettingView...");
        }
        this.isVisible = isVisible;
        if (isVisible) {
            init();
            ((SwitchItemSettingView) itemView).setOnCheckedChangeListener(this);
        }
    }

    /**
     * 状态发生改变了
     *
     * @param isChecked true：选中，false：未选中
     */
    public void performCheckedChanged(boolean isChecked) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        performCheckedChanged(isChecked);
    }

    public int getViewId(){
        return this.mItemView.getId();
    }
}
