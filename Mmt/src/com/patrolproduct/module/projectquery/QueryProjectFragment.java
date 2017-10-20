package com.patrolproduct.module.projectquery;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.repair.zhoushan.common.Constants;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QueryProjectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QueryProjectFragment extends Fragment {


    // 查询的字段名称
    public static final String QUERYNAME = "工程名称";

    public static final String[] TAB_TITLE = {"调压箱", "调压柜", "流量计"};

    /*
     * tab栏
     */
    private MultiSwitchButton tabView;
    /*
     * ViewPager用来展示调压箱，调压柜，流量计三个列表
     */
    protected ViewPager viewPager;
    /*
     * 顶部的搜索框
     */
    protected TextView txtSearch;

    protected View frameOnLoading;

    protected ListFragmentPagerAdapter fragmentAdapter;

    protected ArrayList<ListPagerFragment> fragments;

    public QueryProjectFragment() {
    }

    /**
     * @return A new instance of fragment QueryProjectFragment.
     */
    public static QueryProjectFragment newInstance() {
        return new QueryProjectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_projectquery, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 初始化布局
     *
     * @param view fragment的布局
     */
    private void initView(View view) {
        tabView = (MultiSwitchButton) view.findViewById(R.id.multiSwitchButton);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        frameOnLoading = view.findViewById(R.id.frameOnLoading);
        frameOnLoading.setVisibility(View.VISIBLE);

        tabView.setOnScrollListener(new MultiSwitchButton.OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                viewPager.setCurrentItem(index, false);
            }
        });

//        tabView.setOnItemChangedListener(new MultiSwitchButton2.OnItemChangedListener() {
//            @Override
//            public void onChanged(int currentIndex) {
//                // 选择时，禁止滑动
//                viewPager.setCurrentItem(currentIndex, false);
//            }
//        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                tabView.setSelected(position);
                tabView.setCurrentItem(position);
            }

            /**
             * 滑动状态
             * @param state 0,1,2
             *              0：表示什么也没干
             *              1：表示正在滑动中
             *              2：表示滑动结束
             */
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.txtSearch = (EditText) view.findViewById(R.id.txtSearch);
        txtSearch.setHint(QUERYNAME);
        txtSearch.setVisibility(View.VISIBLE);
        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CaseSearchActivity.class);
                // key 为搜索框的当前值
                intent.putExtra("key", txtSearch.getText().toString());
                intent.putExtra("searchHint", QUERYNAME);
                intent.putExtra("searchHistoryKey", "queryProjectKey");
                QueryProjectFragment.this.startActivity(intent);
                QueryProjectFragment.this.startActivityForResult(intent, Constants.SEARCH_REQUEST_CODE);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        tabView.setContent(TAB_TITLE);
        setFragmentPager();

        frameOnLoading.setVisibility(View.GONE);
    }

    private void setFragmentPager() {
        fragments = new ArrayList<>();

        for (String layerName : TAB_TITLE) {
            fragments.add(ListPagerFragment.newInstance(layerName));
        }

        /*
         * 嵌套使用fragment时
         *      子Fragmentg要用etChildFragmentManager()
         */
        fragmentAdapter = new ListFragmentPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(fragmentAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1 && requestCode == Constants.SEARCH_REQUEST_CODE) {
            // 搜索界面返回的搜索关键字
            String key = data.getStringExtra("key"); //返回空代表查全部
            txtSearch.setText(key);

            // 通知fragment刷新列表中的数据
//            EventBus.getDefault().post(true);

            for (ListPagerFragment fragmentPager : fragments) {
                if (fragmentPager != null && fragmentPager.getListViewResult() != null) {
                    fragmentPager.getListViewResult().setRefreshing(true);
                }
            }
//            ListPagerFragment.keyValue = key;

//            ((QueryProjectActivity) getActivity()).showToast("查询关键字：" + key);
        }
    }

    public String getKeyValue(){
        return txtSearch.getText().toString().trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
