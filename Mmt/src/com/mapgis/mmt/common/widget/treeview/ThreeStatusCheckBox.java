package com.mapgis.mmt.common.widget.treeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mapgis.mmt.R;

/**
 * Created by Comclay on 2017/3/5.
 * 具有多种选择状态的checkbox
 */

public class ThreeStatusCheckBox extends ImageView {
    private static final String TAG = "ThreeStatusCheckBox";
    /**
     * 选中的状态
     * 0：不选
     * 1：半选
     * 2：全选
     */
    public static final int CHECK_NONE = 0;
    public static final int CHECK_PART = 1;
    public static final int CHECK_ALL = 2;

    private boolean mCheckable = true;

    private int mStatus = CHECK_NONE;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public ThreeStatusCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs
                , R.styleable.ThreeStatusCheckBox);
        int status = typedArray.getInteger(R.styleable.ThreeStatusCheckBox_checkStatus, CHECK_NONE);
        setStatus(status);

        // 用来监听点击事件，并转换为对应的状态图标
        /**
         * 如果当前状态为CHECK_NONE，则点击后变成CHEKC_ALL
         * 如果当前状态为CHECK_PART，则点击后变成CHECK_NONE
         * 如果当前状态为CHEKC_ALL，则点击后变成CHECK_NONE
         */
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mStatus) {
                    case CHECK_NONE:
                    case CHECK_PART:
                        setStatus(CHECK_ALL);
                        break;
                    case CHECK_ALL:
                        setStatus(CHECK_NONE);
                        break;
                }
                Log.i(TAG, "onClick: 当前的状态变为：" + mStatus);
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onChenkedChange(mStatus);
                }
            }
        });
        typedArray.recycle();
    }

    /**
     * 设置状态
     *
     * @param status 0：不选
     *               1：半选
     *               2：全选
     */
    public void setStatus(int status) {
        switch (status) {
            case CHECK_NONE:
                this.setImageResource(R.drawable.checkbox_none);
                break;
            case CHECK_PART:
                this.setImageResource(R.drawable.checkbox_part);
                break;
            case CHECK_ALL:
                this.setImageResource(R.drawable.checkbox_all);
                break;
            default:
                return;
        }
        this.mStatus = status;
    }

    /**
     * 获取状态
     *
     * @return
     */
    public int getStatus() {
        return this.mStatus;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public void removeCheckedChangeListener() {
        this.mOnCheckedChangeListener = null;
    }

    interface OnCheckedChangeListener {
        void onChenkedChange(int status);
    }

    public void setCheckable(boolean chekcable){
        this.setClickable(chekcable);
        this.setFocusable(chekcable);
        this.mCheckable = chekcable;
    }
}
