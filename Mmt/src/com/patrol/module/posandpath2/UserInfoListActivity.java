package com.patrol.module.posandpath2;

import android.content.Intent;
import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;

/**
 * Created by Comclay on 2016/10/21.
 * 展示人员列表信息
 */

public class UserInfoListActivity extends BaseActivity {
    private UserInfoFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText(R.string.patroller_list_title);

        mFragment = UserInfoFragment.newInstance(getIntent().getStringArrayListExtra("deptList")
                ,getIntent().getParcelableArrayListExtra("userList"));

        addFragment(mFragment);
    }

    @Override
    public void onCustomBack() {
        mFragment.showSelectedDataOnMap();

        backToMapView();
    }

    public void backToMapView() {
        Intent intent = new Intent(this, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
