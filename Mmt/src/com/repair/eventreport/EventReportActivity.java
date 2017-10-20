package com.repair.eventreport;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.widget.fragment.LevelItemFragment;
import com.mapgis.mmt.entity.LevelItem;

import java.util.ArrayList;

public class EventReportActivity extends BaseActivity implements LevelItemFragment.OnItemCheckedListener {
    private EventReportFragmentView fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText(this.getTitle());
        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backByReorder();
            }
        });
        getBaseRightImageView().setVisibility(View.GONE);

        if (savedInstanceState == null) {
            boolean allowMultiple = getIntent().getBooleanExtra("AllowMultiple", false);
            fragment = EventReportFragmentView.newInstance(allowMultiple);
            addFragment(fragment);
        }
    }

    @Override
    public void onItemChecked(ArrayList<LevelItem> checkedItemList) {
        fragment.onItemChecked(checkedItemList);
    }
}
