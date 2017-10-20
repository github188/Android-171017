package com.mapgis.mmt.module.systemsetting.download;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * Created by Comclay on 2017/4/17.
 * 下载管理
 */

public class DownloadActivity extends BaseActivity {
    public final static String EXTRA_REFRESH = "isRefresh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText(R.string.text_download_manager);
        boolean isRefresh = false;
        if (getIntent().hasExtra(EXTRA_REFRESH)) {
            isRefresh = getIntent().getBooleanExtra(EXTRA_REFRESH, false);
        }
        addFragment(DownloadFragment.newInstance(isRefresh));
    }
}
