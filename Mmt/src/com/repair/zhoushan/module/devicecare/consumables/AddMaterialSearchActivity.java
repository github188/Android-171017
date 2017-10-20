package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;

import java.util.ArrayList;

public class AddMaterialSearchActivity extends BaseActivity {

    // 物料数据
    private ArrayList<WuLiaoBean> materialList;
    private AddMaterialListAdpater adapter;

    private ListFragment listFragment;

    // 记录过滤后的数据，所有物料数据的子集
    private ArrayList<WuLiaoBean> tempMaterialList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.materialList = getIntent().getParcelableArrayListExtra("MaterialList");
        tempMaterialList.addAll(materialList);

        initActionbar();
        initMainContent();
        initBottomView();
    }

    private void initMainContent() {

        this.adapter = new AddMaterialListAdpater(this, tempMaterialList);

        this.listFragment = new ListFragment();
        listFragment.setListAdapter(adapter);
        // listFragment.setEmptyText("没有匹配的物料");

        addFragment(listFragment);
    }

    private void initBottomView() {

        addBottomUnitView("确定", true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResult();
            }
        });
    }

    private void submitResult() {

        final StringBuilder sb = new StringBuilder();
        final ArrayList<WuLiaoBean> selectedBeans = new ArrayList<WuLiaoBean>();

        for (WuLiaoBean bean : materialList) {
            if (bean.isCheck() && bean.getNum() > 0) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append(bean.getNum()).append(" * {\'").append(bean.getCode())
                        .append("\', \'").append(bean.getName()).append("\'}");
                selectedBeans.add(new WuLiaoBean(bean.getCode(), bean.getName(), bean.getNum(), bean.getUnit()));
            }
        }
        if (sb.length() == 0)
            sb.append("无");

        new AlertDialog.Builder(AddMaterialSearchActivity.this)
                .setTitle("已选中的物料：")
                .setMessage(sb.toString())
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedBeans.size() > 0) {
                            Intent intent = new Intent();
                            intent.putParcelableArrayListExtra("SelectedMaterialList", selectedBeans);
                            setResult(Activity.RESULT_OK, intent);

                            AddMaterialSearchActivity.this.finish();
                        }
                    }
                }).show();
    }

    private void initActionbar() {

        View searchActionbar = LayoutInflater.from(this).inflate(R.layout.search_actionbar, null);
        searchActionbar.findViewById(R.id.btnOtherAction).setVisibility(View.GONE);
        setCustomView(searchActionbar);

        // Back button.
        addBackBtnListener(searchActionbar.findViewById(R.id.btnBack));

        // Input text.
        EditText etSearch = (EditText) searchActionbar.findViewById(R.id.txtSearch);
        etSearch.setFocusable(true);
        etSearch.setFocusableInTouchMode(true);
        etSearch.setHint("物料编号");
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterData(s.toString());
            }
        });
    }

    private void filterData(String keyWord) {

        tempMaterialList.clear();

        if (TextUtils.isEmpty(keyWord)) {
            tempMaterialList.addAll(materialList);
        } else {
            for (WuLiaoBean wuLiaoBean : materialList) {
                if (wuLiaoBean.getCode().contains(keyWord)) {
                    tempMaterialList.add(wuLiaoBean);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
