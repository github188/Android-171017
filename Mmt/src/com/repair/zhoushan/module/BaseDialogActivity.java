package com.repair.zhoushan.module;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.R;

import java.util.List;

public abstract class BaseDialogActivity extends FragmentActivity implements View.OnClickListener {

    protected GDFormBean mGDFormBean;
    protected String mTitle;

    // 标志该界面
    protected String mTag;

    protected String fileRelativePath;

    protected FlowBeanFragment mFlowBeanFragment;

    public FlowBeanFragment getFlowBeanFragment() {
        return mFlowBeanFragment;
    }

    public String getmTag() {
        return mTag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent argsIntent = getIntent();
        if (this.mGDFormBean == null) {
            this.mGDFormBean = argsIntent.getParcelableExtra("GDFormBean");
        }
        if (TextUtils.isEmpty(this.mTitle)) {
            this.mTitle = argsIntent.getStringExtra("Title");
        }
        if (TextUtils.isEmpty(this.mTag)) {
            this.mTag = argsIntent.getStringExtra("Tag");
        }
        if (TextUtils.isEmpty(this.fileRelativePath)) {
            this.fileRelativePath = argsIntent.getStringExtra("FileRelativePath");
        }
        if (TextUtils.isEmpty(mTag)) {
            mTag = mTitle;
        }

        AppManager.addActivity(this);

        initView();
    }

    protected void initView() {
        setContentView(R.layout.ok_cancle_base_dialog);

        ((TextView) findViewById(R.id.tv_ok_cancel_dialog_Tips)).setText(mTitle);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);

        this.mFlowBeanFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", mGDFormBean);
        if (!TextUtils.isEmpty(fileRelativePath)) {
            args.putString("FileRelativePath", fileRelativePath);
        }
        mFlowBeanFragment.setArguments(args);
        mFlowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                onViewCreated();
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.layout_ok_cancel_dialog_content, mFlowBeanFragment);
        ft.commitAllowingStateLoss();
    }

    protected void onViewCreated() {
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btn_cancel) {
                hideKeyBorad();
                AppManager.finishActivity();
            } else if (v.getId() == R.id.btn_ok) {

                List<FeedItem> feedItemList = mFlowBeanFragment.getFeedbackItems(ReportInBackEntity.REPORTING);
                if (feedItemList != null) {
                    handleOkEvent(mTag, feedItemList);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.finishActivity(this);
    }

    /**
     * 一般仅仅需要表单中填入的简单值；
     * 对于复杂的有图片录音的可以通过获取 mFlowBeanFragment对象在方法中进行特殊处理。
     *
     * @param tag          用于标志以确定响应事件
     * @param feedItemList 表单中填入的数据
     */
    protected abstract void handleOkEvent(String tag, List<FeedItem> feedItemList);

    /**
     * 上报成功后
     */
    protected void onSuccess() {
        hideKeyBorad();
        setResult(Activity.RESULT_OK);

        AppManager.finishActivity();
    }
    /**
     * 上报成功后
     */
    protected void onSuccessV2() {
        hideKeyBorad();
        AppManager.finishActivity();
    }

    protected void hideKeyBorad() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
