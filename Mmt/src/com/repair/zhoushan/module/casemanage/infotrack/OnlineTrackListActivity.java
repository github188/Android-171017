package com.repair.zhoushan.module.casemanage.infotrack;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.SimpleBaseAdapter;
import com.repair.zhoushan.module.SimplePagerListActivity;
import com.repair.zhoushan.module.SimplePagerListDelegate;

import java.util.ArrayList;

public class OnlineTrackListActivity extends SimplePagerListActivity {

    private String trackState = "跟踪中";

    @Override
    public void init() {

        final ArrayList<HotlineModel> eventItemList = new ArrayList<HotlineModel>();

        mSimplePagerListDelegate = new SimplePagerListDelegate<HotlineModel>(OnlineTrackListActivity.this, eventItemList, HotlineModel.class) {

            @Override
            protected SimpleBaseAdapter generateAdapter() {
                return new OnlineTrackAdapter(OnlineTrackListActivity.this, eventItemList);
            }

            @Override
            protected String generateUrl() {

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/MapgisCity_ProjectManage_ZS/REST/ProjectManageREST.svc/HotlineManage/GetOnlineTrackWithPaging")
                        .append("?pageSize=").append(getPageSize())
                        .append("&pageIndex=").append(getLoadPageIndex())
                        .append("&sortFields=更新时间&direction=desc")
                        .append("&followState=").append(trackState);

                return sb.toString();
            }
        };
    }

    @Override
    protected void afterViewCreated() {

        super.afterViewCreated();

        ImageButton filterBtn = (ImageButton) findViewById(R.id.baseActionBarRightImageView);
        filterBtn.setVisibility(View.VISIBLE);
        filterBtn.setImageResource(R.drawable.common_title_filter_selected);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment listDialogFragment = new ListDialogFragment("跟踪状态", new String[]{"全部", "跟踪中", "完成"});
                listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {

                        if ("全部".equals(value)) {
                            trackState = "";
                        } else {
                            trackState = value;
                        }

                        mSimplePagerListDelegate.updateData();
                    }
                });
                listDialogFragment.show(getSupportFragmentManager(), "");
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasCategory(Constants.CATEGORY_BACK_TO_LIST)) {
            intent.removeCategory(Constants.CATEGORY_BACK_TO_LIST);
            mSimplePagerListDelegate.updateData();
        }
        setIntent(intent);
    }
}
