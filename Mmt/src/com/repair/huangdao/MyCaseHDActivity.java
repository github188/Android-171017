package com.repair.huangdao;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;
import com.repair.huangdao.detail.CaseDetailFragment;
import com.repair.huangdao.list.CaseListFragment;

public class MyCaseHDActivity extends BaseActivity {
    CaseListFragment listFragment;
    public volatile String shouldRefresh = "";
    public CaseItem selectedItem;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.baseTopView).setVisibility(View.GONE);
        setSwipeBackEnable(false);

        listFragment = new CaseListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.base_root_relative_layout, listFragment, CaseListFragment.class.getName());

        transaction.show(listFragment);

        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra("pos")) {
            int pos = getIntent().getIntExtra("pos", -2);

            getIntent().removeExtra("pos");

            if (pos >= 0) {//从列表界面定位到地图后返回的显示详情请求
                if (listFragment.isVisible())//处于列表界面时，打开详情，否则保持显示即可
                    listFragment.OnItemClick(pos, "map");
            } else if (pos != -1 && !listFragment.isVisible()) {//从列表界面定位到地图后返回的恢复列表请求
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.remove(manager.findFragmentByTag(CaseDetailFragment.class.getName()));
                transaction.show(listFragment);

                transaction.commitAllowingStateLoss();
            }
        }
    }
}
