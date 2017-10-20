package com.mapgis.mmt.common.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.VideoViewerFragment;

public class VideoViewerActivity extends BaseActivity {

    private VideoViewerFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.base_root_relative_layout).setVisibility(View.GONE);

//        getBaseTextView().setText("视频浏览");

//        findViewById(R.id.mainActionBar).setVisibility(View.GONE);

        fragment = VideoViewerFragment.newInstance(
                getIntent().getStringArrayListExtra(VideoViewerFragment.VIDEO_ABSOLUTE_PATH)
                ,getIntent().getStringArrayListExtra(VideoViewerFragment.IMAGE_ABSOLUTE_PATH)
                ,getIntent().getIntExtra(VideoViewerFragment.CURRENT_SELECTED_INDEX,0)
                ,getIntent().getBooleanExtra(VideoViewerFragment.CAN_DELETE,true)
                );

        ViewGroup contentView = (ViewGroup)findViewById(android.R.id.content);
        View.inflate(this,R.layout.activity_video_viewer,contentView);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_content_video_viewer,fragment);
        ft.commit();
//        addFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(VideoViewerFragment.VIDEO_ABSOLUTE_PATH,fragment.getVideoList());
        intent.putStringArrayListExtra(VideoViewerFragment.IMAGE_ABSOLUTE_PATH,fragment.getImgList());

        setResult(Activity.RESULT_OK,intent);

        finish();

        this.overridePendingTransition(0, R.anim.bottom_out);
//        super.onBackPressed();
    }
}
