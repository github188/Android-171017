package com.mapgis.mmt.common.widget;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;

/**
 * 图片预览界面
 * <p>
 * <p>
 * 传进来的参数可以是图片存放的本地路径，或是URL地址。<br>
 * fileList：文件路径<br>
 * pos：当前查看的第几张图片<br>
 * canDelete：是否允许删除<br>
 */
public class PictureViewActivity extends BaseActivity {
    private PictureViewFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        getBaseTextView().setText("图片预览");

        getBaseLeftImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.doFinish();
            }
        });

        boolean isCanDel=getIntent().getBooleanExtra("canDelete", true);
        if (isCanDel) {
            addDeleteButton();
        }

        fragment = new PictureViewFragment();
        Bundle bundle=new Bundle();
        bundle.putBoolean("canDelete",isCanDel);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    /**
     * 是否显示删除按钮
     */
    private void addDeleteButton() {
        getBaseRightImageView().setImageResource(R.drawable.delete_white);
        getBaseRightImageView().setVisibility(View.VISIBLE);
        getBaseRightImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OkCancelDialogFragment deleteFragment = new OkCancelDialogFragment("确定删除?");
                deleteFragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                    @Override
                    public void onRightButtonClick(View view) {
                        fragment.doDelete();
                    }
                });
                deleteFragment.show(getSupportFragmentManager(), "");
            }
        });
    }
}

