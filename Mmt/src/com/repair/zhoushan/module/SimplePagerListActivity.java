package com.repair.zhoushan.module;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;

/**
 * SimplePagerListDelegate 类功能要通用，非通用的功能一律移到 SimplePagerListActivity的子类中予以实现.
 * SimplePagerListActivity、SimplePagerListActivity的子类以及 SimplePagerListDelegate要职责分明.
 */
public abstract class SimplePagerListActivity extends BaseActivity {

    protected SimplePagerListDelegate mSimplePagerListDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        init();
        super.onCreate(savedInstanceState);
    }

    /**
     * 应当在这个方法中实现对 mSimplePagerListDelegate的初始化.
     */
    public abstract void init();

    @Override
    protected void setDefaultContentView() {
        mSimplePagerListDelegate.setDefaultContentView(new SimplePagerListDelegate.OnViewCreatedListener() {
            @Override
            public void onViewCreated() {
                afterViewCreated();
            }
        });
    }

    protected void afterViewCreated(){}

    public void updateView() {
        mSimplePagerListDelegate.updateView();
    }

    public void updateData() {
        mSimplePagerListDelegate.updateData();
    }

}
