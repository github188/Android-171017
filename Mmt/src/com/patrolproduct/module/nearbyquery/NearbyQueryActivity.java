package com.patrolproduct.module.nearbyquery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.R;

import java.util.List;

/**
 * Created by Comclay on 2016/11/3.
 * 附近查询功能的界面
 */

public class NearbyQueryActivity extends BaseActivity {
    private final static String FLAG_SELECT_LAYERS = "selectLayers";
    private NearbyQueryFragment mFragment;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置标题
        getBaseTextView().setText("附近查询");

        // 在地图上显示当前选中的选项卡中的所有设备
        ImageButton baseRightImageView = getBaseRightImageView();
        baseRightImageView.setImageResource(R.drawable.all_result_show_map);
        baseRightImageView.setVisibility(View.VISIBLE);

        if (MyApplication.getInstance().mapGISFrame.getMapView().getMap() == null){
            showErrorMsg("没有加载地图，该功能无法使用！");
            return;
        }

        String layerNames = "";
        if (getIntent().hasExtra("layerNames")){
            layerNames = getIntent().getStringExtra("layerNames");
        }
        sp = MyApplication.getInstance().getSystemSharedPreferences();
        mFragment = NearbyQueryFragment.newInstance(sp.getString(FLAG_SELECT_LAYERS,layerNames));
        addFragment(mFragment);
    }

    /**
     * 获取Fragment
     * @return NearbyQueryFragment
     */
    public NearbyQueryFragment getQueryFragment(){
        return mFragment;
    }

    /**
     * 保存当前选中的图层选项卡
     */
    @Override
    public void finish() {
        super.finish();
        if(mFragment != null){
            List<String> currentLayers = mFragment.getCurrentLayers();

            SharedPreferences.Editor edit = sp.edit();
            edit.putString(FLAG_SELECT_LAYERS,BaseClassUtil.listToString(currentLayers));
            edit.commit();
        }
    }
}
