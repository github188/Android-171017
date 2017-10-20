package com.repair.reporthistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.module.gis.MapGISFrame;

public class PatrolReportHistoryActivity extends BaseActivity {
    private String listTitle;
    private PatrolEventListViewFragment fragment;

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public String getDetailTitle() {
        return detailTitle;
    }

    public void setDetailTitle(String detailTitle) {
        this.detailTitle = detailTitle;
    }

    private String detailTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new PatrolEventListViewFragment();
        replaceFragment(fragment);
        listTitle = getIntent().getStringExtra("title");
        if (listTitle == null) {
            listTitle = "巡线历史";
        }
        detailTitle = listTitle + "详情";
        findViewById(R.id.baseActionBarImageView).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int size = AppManager.activityList.size();
                        // 点返回，返回列表，返回主页，返回地图

                        // 1.返回列表（列表-详情-列表）
                        if (getSupportFragmentManager().getBackStackEntryCount() > 0
                                && getIntent().getIntExtra("pos", -2) < 0
                                && AppManager.activityList.get(size - 1).getClass().equals(PatrolReportHistoryActivity.class)) {
                            if (getIntent().getIntExtra("pos", -2) == -1) {
                                getIntent().removeExtra("pos");
                            }
                            getBaseRightImageView().setVisibility(View.VISIBLE);
                            getSupportFragmentManager().popBackStack();
                        }
                        // 2.回到地图页 （地图-列表-详情-地图）
                        else if (AppManager.activityList.get(size - 1).getClass()
                                .equals(PatrolReportHistoryActivity.class)
                                && getSupportFragmentManager().getBackStackEntryCount() > 0
                                && getIntent().getIntExtra("pos", -2) >= 0) {

                            Intent intent = new Intent(PatrolReportHistoryActivity.this, MapGISFrame.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            PatrolReportHistoryActivity.this.startActivity(intent);

                            getIntent().removeExtra("pos");
                            getSupportFragmentManager().popBackStack();
                        }
                        // 3.列表页返回主页
                        else if (AppManager.activityList.get(size - 1)
                                .getClass().equals(PatrolReportHistoryActivity.class)
                                && getSupportFragmentManager().getBackStackEntryCount() == 0
                                && (getIntent().getIntExtra("pos", -2) == -2)) {

                            getSupportFragmentManager().beginTransaction().remove(fragment);
                            AppManager.finishActivity(PatrolReportHistoryActivity.this);
                            MyApplication.getInstance().finishActivityAnimation(PatrolReportHistoryActivity.this);

                            Class<?> navigationActivityClass = ActivityClassRegistry.getInstance().getActivityClass("主界面");
                            Intent intent = new Intent(PatrolReportHistoryActivity.this, navigationActivityClass);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            // 1.地图到详情页或列表页
            if (getIntent().hasExtra("pos")) {
                int pos = getIntent().getIntExtra("pos", -2);
                // 2中操作，1点详情，2点返回
                // 1.点详情，(一定是从列表页打开)
                if (pos >= 0) {
                    fragment.OnItemClick(pos, "map");
                }
                // -1,点返回
                else {
                    // 之前是列表页
                    if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                        // 直接返回
                    } else {
                        // 之前是详情页（列表-详情-地图）
                        if (pos == -1) {
                            // 直接返回
                        }
                        // 之前是详情页（地图-详情-地图）
                        if (pos == -2) {
                            // 打开列表页
                            getBaseRightImageView().setVisibility(View.VISIBLE);
                            getSupportFragmentManager().popBackStack();
                        }
                    }
                }
            }
        } catch (Exception ex) {

        }

    }

}
