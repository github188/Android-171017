package com.patrol.module.posandpath;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.Product;

/**
 * Created by meiko on 2016/3/15 0015.
 */
public class PosAndPathActivity extends BaseActivity {
    private PosAndPathFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new PosAndPathFragment(this);
        addFragment(fragment);

        initTopView();

        initData();
    }

    /**
     * 初始化顶部的布局
     */
    private void initTopView(){
        // 设置标题
        getBaseTextView().setText(Product.getInstance().Title);
    }

    /**
     * 初始化数据
     */
    private void initData(){

    }

    @Override
    public void onBackPressed() {
//        finish();  // 退出
        onCustomBack();
        // 执行退出动画 只能在活动关闭之后执行此动画
        MyApplication.getInstance().finishActivityAnimation(this);
    }

    public PosAndPathFragment getFragment() {
        return fragment;
    }
}
