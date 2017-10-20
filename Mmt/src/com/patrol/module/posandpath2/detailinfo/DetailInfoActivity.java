package com.patrol.module.posandpath2.detailinfo;

import android.os.Bundle;

import com.mapgis.mmt.BaseActivity;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Comclay on 2016/10/25.
 * 详细信息界面
 */

public class  DetailInfoActivity extends BaseActivity {
    public final static String ARG_TITLE = "title";
    public final static String ARG_GROUP_DATA = "groupData";
    public final static String ARG_MAP_OBJECT = "mapObject";
    private DetailInfoFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(ARG_TITLE)) {
            getBaseTextView().setText(getIntent().getStringExtra(ARG_TITLE));
        }else{
            getBaseTextView().setText("详细信息");
        }
        if (getIntent().hasExtra(ARG_GROUP_DATA)) {
            LinkedHashMap<String, LinkedHashMap<String, String>> mGroupData = new LinkedHashMap<>();

            Serializable serializableExtra = getIntent().getSerializableExtra(ARG_GROUP_DATA);
            mGroupData.putAll((Map<String, LinkedHashMap<String, String>>) serializableExtra);
            mFragment = DetailInfoFragment.newInstance(mGroupData);
        }

        if (getIntent().hasExtra(ARG_MAP_OBJECT)){
            mFragment = DetailInfoFragment.newInstance((DetailInfoMapData) getIntent().getParcelableExtra(ARG_MAP_OBJECT));
        }

        addFragment(mFragment);
    }

    @Override
    public void onCustomBack() {
        onDefaultBack(this);
    }
}
