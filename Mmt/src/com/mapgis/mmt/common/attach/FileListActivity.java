package com.mapgis.mmt.common.attach;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;

/**
 * 文件列表界面
 */
public class FileListActivity extends BaseActivity {

    private FileListFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("title")){
            getBaseTextView().setText(getIntent().getStringExtra("title"));
        }else{
            getBaseTextView().setText("文件列表");
        }

        mFragment = FileListFragment.newInstance(getIntent().getStringExtra("path"));
        addFragment(mFragment);
    }

    @Override
    public void onCustomBack() {
        onCustomBack(false);
    }

    public void onCustomBack(boolean isback) {
        if (!mFragment.onBack(isback)){
            return;
        }
        super.onCustomBack();
    }
}
