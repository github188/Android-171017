package com.repair.zhoushan.module.devicecare.consumables;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.R;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.module.devicecare.ScheduleTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.List;

public class MaterialListActivity extends BaseActivity {

    private MaterialListFragment mMaterialListFragment;

    private ScheduleTask mScheduleTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mScheduleTask = getIntent().getParcelableExtra("ListItemEntity");

        getBaseTextView().setText("物料清单");

        createView();
        createBottomView();
    }

    private void createView() {

        this.mMaterialListFragment = new MaterialListFragment();
        Bundle args = new Bundle();
        args.putParcelable("ListItemEntity" , mScheduleTask);
        mMaterialListFragment.setArguments(args);

        addFragment(mMaterialListFragment);
    }

    private void createBottomView() {

        // 若从养护历史列表进入不展示底部功能按钮
        if (getIntent().hasCategory(Constants.CATEGORY_DEF_FOR_TAG)) {
            return;
        }

        BottomUnitView saveUnitView = new BottomUnitView(MaterialListActivity.this);
        saveUnitView.setContent("退料");
        saveUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(saveUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<WuLiaoBean> materialList = mMaterialListFragment.getMaterialList();
                if (materialList.size() == 0) {
                    Toast.makeText(MaterialListActivity.this, "暂无可退的物料", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 补料、退料成功回来需要刷新列表
                Intent intent = new Intent(MaterialListActivity.this, UpdateMaterialActivity.class);
                intent.putExtra("MaterialListStr", new Gson().toJson(materialList));
                intent.putExtra("ListItemEntity", mScheduleTask);
                intent.putExtra("IsExistCostCenter", mMaterialListFragment.isExistCostCenter());
                mMaterialListFragment.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });

        BottomUnitView addUnitView = new BottomUnitView(MaterialListActivity.this);
        addUnitView.setContent("领料");
        addUnitView.setImageResource(R.drawable.handoverform_report);
        addBottomUnitView(addUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 领料成功回来需要刷新列表
                Intent intent = new Intent(MaterialListActivity.this, AddMaterialActivity.class);
                intent.putExtra("ListItemEntity", mScheduleTask);
                intent.putExtra("IsExistCostCenter", mMaterialListFragment.isExistCostCenter());
                mMaterialListFragment.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            }
        });
    }

}
