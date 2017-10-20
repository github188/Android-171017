package com.repair.gisdatagather.product.editdata;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.R;

/**
 * Created by liuyunfan on 2015/12/19.
 */
public class EditDataActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle.getBoolean("isPad", true)) {
            if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        getBaseLeftImageView().setVisibility(View.GONE);
       // getBaseRightImageView().setBackgroundResource(R.drawable.ic_scan_qrcode_24dp);
        getBaseRightImageView().setVisibility(View.VISIBLE);
        setTitleAndClear("编辑属性");
        Fragment fragment = new EditDataFragment();
        fragment.setArguments(bundle);
        replaceFragment(fragment);

    }

    @Override
    public void onCustomBack() {
        AppManager.finishActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                View view;
                if ((view = findViewById(R.id.gisGathertoolFrame)) != null && view.getVisibility() != View.VISIBLE && findViewById(R.id.toolFrame).getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }
}
