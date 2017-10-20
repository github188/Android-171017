package com.repair.beihai.poj.gdpoj.module.construct;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.common.Constants;

import static com.mapgis.mmt.R.id.txtSearch;

/**
 * Created by liuyunfan on 2016/8/8.
 */
public class GDConstructEventListActivity extends BaseActivity {
    EditText etSearch;
    String keyWord="";
    GDConstructFragment zjFragment;
    GDConstructFragment zbFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String[] titles = new String[]{"自建工程", "招标工程"};

        String params = getIntent().getStringExtra("params");

        zjFragment = new GDConstructFragment();
        Bundle zjbundle = new Bundle();
        zjbundle.putString("flowName", "管道工程自建流程");
        zjbundle.putString("params", params);
        zjFragment.setArguments(zjbundle);

        zbFragment = new GDConstructFragment();
        Bundle zbbundle = new Bundle();
        zbbundle.putString("flowName", "管道工程招标流程");
        zbbundle.putString("params", params);
        zbFragment.setArguments(zbbundle);

        Fragment[] fragments = new Fragment[]{zjFragment, zbFragment};

        MultiSwitchFragment fragment = new MultiSwitchFragment();
        fragment.setDate(titles, fragments);
        fragment.setCurrentIndex(0);
        replaceFragment(fragment);


        overViewHead();
    }

    private void overViewHead() {
        View searchActionbar = LayoutInflater.from(this).inflate(R.layout.search_actionbar, null);
        searchActionbar.findViewById(R.id.btnOtherAction).setVisibility(View.GONE);
        setCustomView(searchActionbar);

        addBackBtnListener(searchActionbar.findViewById(R.id.btnBack));

        etSearch = (EditText) searchActionbar.findViewById(txtSearch);
        etSearch.setFocusable(true);
        etSearch.setFocusableInTouchMode(true);
        etSearch.setHint("摘要信息");

        etSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GDConstructEventListActivity.this, CaseSearchActivity.class);

                // key 为搜索框的当前值
                intent.putExtra("key", etSearch.getText().toString());
                intent.putExtra("searchHint", "摘要信息");
                intent.putExtra("searchHistoryKey", "GDConstructEventListActivity");

                startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            keyWord = data.getStringExtra("key"); //返回空代表查全部
            etSearch.setText(keyWord);

            zjFragment.reFetchData(keyWord);
            zbFragment.reFetchData(keyWord);
        }
    }
}
