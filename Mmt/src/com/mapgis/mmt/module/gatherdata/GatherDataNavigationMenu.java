package com.mapgis.mmt.module.gatherdata;

import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.navigation.BaseNavigationMenu;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

/**
 * 管网普查
 */
public class GatherDataNavigationMenu extends BaseNavigationMenu {

    public GatherDataNavigationMenu(NavigationActivity navigationActivity,
                                    NavigationItem item) {
        super(navigationActivity, item);
    }

    @Override
    public void onItemSelected() {
        Intent intent = new Intent(navigationActivity, MapGISFrame.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        navigationActivity.startActivity(intent);

        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                mapGISFrame.findViewById(R.id.mapviewClear).performClick();

                View view = mapGISFrame.getLayoutInflater().inflate(
                        R.layout.main_actionbar, null);

                view.findViewById(R.id.baseActionBarImageView)
                        .setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // 这里地图（采集数据）作为列表的下一级
                                // otherFragment为列表页面
                                if (mapGISFrame.isOtherFragmentVisible()) {
                                    mapGISFrame.removeOtherFragment();
                                    mapGISFrame.findViewById(R.id.mapviewClear)
                                            .performClick();
                                    AppManager.finishToNavigation(mapGISFrame);
                                    View centerView = mapGISFrame.getMapView().findViewWithTag("MapViewScreenView");
                                    if (centerView != null) {
                                        mapGISFrame.getMapView().removeView(centerView);
                                    }

                                } else {
                                    mapGISFrame.showMainFragment(false);
                                }
                            }
                        });

                ((TextView) view.findViewById(R.id.baseActionBarTextView))
                        .setText(item.Function.Alias);

                mapGISFrame.setCustomView(view);

                GatherDataListDoneFragment doneFragment = new GatherDataListDoneFragment(
                        mapGISFrame);

                mapGISFrame.replaceOtherFragment(doneFragment);

                mapGISFrame.showMainFragment(false);

                return true;
            }
        });
    }
}
