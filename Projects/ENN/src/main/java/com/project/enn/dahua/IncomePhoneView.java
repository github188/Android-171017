package com.project.enn.dahua;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.enn.R;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by Comclay on 2017/3/31.
 * 来电提醒界面
 */

public class IncomePhoneView extends FrameLayout implements View.OnClickListener {

    private TextView mTvNumber;
    private TextView mTvMsg;
    private TextView mTvTime;
    private ImageView mBtnAnswer;
    private ImageView mBtnHungup;
    private ImageView mBtnHUngupAfterAnswer;

    public IncomePhoneView(@NonNull Context context) {
        super(context);
        init();
    }

    public IncomePhoneView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IncomePhoneView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化来电布局
     */
    private void init() {
        View.inflate(this.getContext(), R.layout.income_telegram_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTvNumber = (TextView) this.findViewById(R.id.tv_phone_number);
        mTvMsg = (TextView) this.findViewById(R.id.tv_msg);
        mTvTime = (TextView) this.findViewById(R.id.tv_time);
        mBtnAnswer = (ImageView) this.findViewById(R.id.btn_answer);
        mBtnHungup = (ImageView) this.findViewById(R.id.btn_hungup);
        mBtnHUngupAfterAnswer = (ImageView) this.findViewById(R.id.iv_after_answer_hungup);

        initListener();
    }

    private void initListener() {
        mBtnAnswer.setOnClickListener(this);
        mBtnHungup.setOnClickListener(this);
        mBtnHUngupAfterAnswer.setOnClickListener(this);
    }

    public void resetView() {
        mBtnAnswer.setVisibility(VISIBLE);
        mBtnHungup.setVisibility(VISIBLE);
        mBtnHUngupAfterAnswer.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_answer) {
            /*float targetX = (((ViewGroup) mBtnHungup.getParent()).getWidth() - mBtnHungup.getLeft()) / 2f;
            ViewPropertyAnimator.animate(mBtnHungup).translationX(targetX).setDuration(350).start();*/
            mBtnAnswer.setVisibility(GONE);
            mBtnHungup.setVisibility(GONE);
            mBtnHUngupAfterAnswer.setVisibility(VISIBLE);
            if (mCallback != null) mCallback.onAnswerOperation();
        } else if (id == R.id.btn_hungup) {
            if (mCallback != null) mCallback.onHungupOperation();
        } else if (id == R.id.iv_after_answer_hungup) {
            if (mCallback != null) mCallback.onHungupOperation();
        }
    }

    /**
     * 设置电话号码
     */
    public void setNumber(CharSequence phoneNumber) {
        this.mTvNumber.setText(phoneNumber);
    }

    /**
     * 设置来电用户名称或者部门名称信息
     */
    public void setMsg(CharSequence msg) {
        this.mTvMsg.setText(msg);
    }

    public void setTvTime(int second) {
        int hour = second / 3600;
        int min = (second % 3600) / 60;
        int sec = second % 60;

        DecimalFormat format = new DecimalFormat("00");
        String hourString = format.format(hour);
        String minString = format.format(min);
        String secString = format.format(sec);
        this.mTvTime.setText(String.format(Locale.CHINA
                , "通话时长：%s:%s:%s", hourString, minString, secString));
    }

    public void setMsgVisibility(int visibility) {
        this.mTvMsg.setVisibility(visibility);
    }

    private OnSipOperationCallback mCallback;

    /**
     * 添加SoftPhone操作的回调接口
     */
    public void addOnSipOerationCallback(OnSipOperationCallback callback) {
        this.mCallback = callback;
    }

    /**
     * sip电话的操作回调接口
     */
    public interface OnSipOperationCallback {
        void onAnswerOperation();

        void onHungupOperation();
    }
}
