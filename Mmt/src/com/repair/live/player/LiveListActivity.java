package com.repair.live.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.repair.live.publisher.LivePublishActivity;

/**
 * 正在直播的展示列表
 */
public class LiveListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("title")) {
            this.getBaseTextView().setText(getIntent().getStringExtra("title"));
        } else {
            this.getBaseTextView().setText(R.string.live_list);
        }

        ImageButton mRightButton = getBaseRightImageView();
        mRightButton.setVisibility(View.VISIBLE);
        mRightButton.setImageResource(R.drawable.my_live);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LiveListActivity.this, LivePublishActivity.class);
                LiveListActivity.this.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(LiveListActivity.this);
            }
        });

        LiveListFragment mFragment = LiveListFragment.newInstance();
        addFragment(mFragment);
    }
}
