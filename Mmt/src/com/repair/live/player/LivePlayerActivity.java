package com.repair.live.player;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;

/**
 * 现场直播列表界面
 */
public class LivePlayerActivity extends BaseActivity {

    private LivePlayerFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("title")){
            this.getBaseTextView().setText(getIntent().getStringExtra("title"));
        }else{
            this.getBaseTextView().setText(R.string.live_player);
        }

        // 直播拉流地址
        mFragment = LivePlayerFragment.newInstance(getIntent().getStringExtra("rtmpUrl"));
        addFragment(mFragment);
    }
}
