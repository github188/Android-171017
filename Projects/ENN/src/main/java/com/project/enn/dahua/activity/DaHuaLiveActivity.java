package com.project.enn.dahua.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.android.business.client.msp.McuClient;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.project.enn.R;

/**
 * Created by Comclay on 2017/4/20.
 * 大华报警并开启直播功能
 */

public class DaHuaLiveActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setTitleNumber();
        addFragment(DaHuaLiveFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
    }

    public void setTitleNumber() {
        String softPhoneCallnumber = McuClient.getInstance().getSoftPhoneCallnumber();
        if (!BaseClassUtil.isNullOrEmptyString(softPhoneCallnumber)) {
            getBaseTextView().setText(softPhoneCallnumber);
        }else {
            getBaseTextView().setText("现场抢修");
        }
    }

    void setSipHoldIconVisibility(int visibility) {
        if (View.VISIBLE == visibility) {
            getBaseRightImageView().setImageResource(R.drawable.ic_phone_in_talk);
            getBaseRightImageView().setVisibility(View.VISIBLE);
        }else {
            getBaseRightImageView().setVisibility(View.GONE);
        }
    }
}
