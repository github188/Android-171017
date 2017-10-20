package com.mapgis.mmt.module.systemsetting.setting;

import android.content.Context;
import android.view.View;

import com.mapgis.mmt.module.systemsetting.itemwidget.MoreItemSettingView;

/**
 * Created by Comclay on 2017/6/14.
 *
 * 可以点击更多的设置项，默认实现了OnClickListener点击事件的监听
 */

public abstract class BaseMoreSettingItem implements ISettingItem, View.OnClickListener {
    // 该项设置是否可见
    protected Context mContext;
    protected boolean isVisible = true;
    protected MoreItemSettingView mItemView;

    public BaseMoreSettingItem(Context context, View itemView) {
        this(context, itemView, true);
    }

    public BaseMoreSettingItem(Context context, View itemView, boolean isVisible) {
        this.mContext = context;
        if (itemView instanceof MoreItemSettingView) {
            this.mItemView = (MoreItemSettingView) itemView;
        } else {
            throw new IllegalArgumentException("itemView must be instanceof MoreItemSettingView...");
        }
        this.isVisible = isVisible;
        if (isVisible) {
            init();
            this.mItemView.setOnClickListener(this);
        }
    }

    /**
     * 点击事件
     */
    public void performClicked() {
    }

    @Override
    public void onClick(View v) {
        performClicked();
    }

    public int getViewId(){
        return this.mItemView.getId();
    }
}
