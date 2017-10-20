package com.patrolproduct.module.nearbyquery;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.R;
import com.zondy.mapgis.map.MapLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NearbyQueryFragment extends Fragment {
    private static final String ARG_PARAM1 = "layerNames";
    private List<String> mVisibleVectorLayerNames;
    // 图层名称集合,多个图层用逗号隔开
    private String mLayerNames;
    // 查询半径
    private String[] mQueryRadius;
    // 查询半径选择框
    private Spinner mSpinnerQueryRadius;
    // 查询图层选择框
    private TextView mSpinnerQueryLayer;
    // 标题栏
    private TabLayout mTabLayout;
    // 结果页面
    private ViewPager mViewPager;
    private View mRootView;
    private Dialog mLayerDialog;
    // 当前选择的查询半径
    private String mCurrentRadius;
    private boolean[] mCurrentCheckedLayersState;
    private List<String> mCurrentLayerNames;
    // ViewPager页面适配器
    private ResultPagerAdapter mAdapter;
    private ArrayList<NearbyLayerResultFragment> mFragmentList;

    public NearbyQueryFragment() {
    }

    /**
     * 创建Fragment对象
     *
     * @param layerNames 图层名称列表，用","隔开
     * @return NearbyQueryFragment's object
     */
    public static NearbyQueryFragment newInstance(String layerNames) {
        NearbyQueryFragment fragment = new NearbyQueryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, layerNames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLayerNames = getArguments().getString(ARG_PARAM1);
        }

        mQueryRadius = getResources().getStringArray(R.array.nearby_query_radius);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_nearby_query, container, false);
        initView(mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    public List<String> getCurrentLayers() {
        return mCurrentLayerNames;
    }

    /**
     * 初始化布局
     */
    private void initView(View view) {
        mSpinnerQueryRadius = (Spinner) view.findViewById(R.id.spinner_query_radius);
        mSpinnerQueryLayer = (TextView) view.findViewById(R.id.spinner_query_layername);

        mTabLayout = (TabLayout) view.findViewById(R.id.tabLayout_layer_titles);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);

        initTitle();
    }

    private void initTitle() {
        Activity activity = getActivity();
        if (activity instanceof NearbyQueryActivity) {
            ((NearbyQueryActivity) activity).getBaseRightImageView()
                    .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showCurrentLayerOnMap();
                                            }
                                        }
                    );
        }
    }

    /**
     * 将当前选中的选项卡中的结果全部显示到地图上
     */
    private void showCurrentLayerOnMap() {
        if (mFragmentList == null || mFragmentList.size() == 0) {
            return;
        }

        NearbyLayerResultFragment fragment = mFragmentList.get(
                mTabLayout.getSelectedTabPosition());
        fragment.showAllOnMap();
    }

    private void initData() {
        mVisibleVectorLayerNames = new ArrayList<>();
        mCurrentLayerNames = new ArrayList<>();
        mFragmentList = new ArrayList<>();
        setSpinnerDefaultData();
        setContentData();

        refreshView();
    }

    private void setContentData() {
        mAdapter = new ResultPagerAdapter(getFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * 初始化Spinner的中数据及设置默认值
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSpinnerDefaultData() {
        mQueryRadius = getResources().getStringArray(R.array.nearby_query_radius);
        // 这个设置spinner本身的样式
        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(getActivity()
                , R.layout.spinner_item, mQueryRadius);
        // 用来设置下拉菜单的item的布局
        radiusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mSpinnerQueryRadius.setAdapter(radiusAdapter);

        // 得到所有可见的图层列表
        List<MapLayer> mVisibleVectorLayer = GisUtil.getVisibleVectorLayerNames(
                MyApplication.getInstance().mapGISFrame.getMapView());

        if (mVisibleVectorLayer == null) {
            if (getActivity() instanceof NearbyQueryActivity) {
                ((NearbyQueryActivity) getActivity()).showErrorMsg("图层获取失败！");
            }
            mRootView.setVisibility(View.GONE);
            return;
        }

        if (mVisibleVectorLayer.size() == 0) {
            if (getActivity() instanceof NearbyQueryActivity) {
                ((NearbyQueryActivity) getActivity()).showErrorMsg("图层信息为空！");
            }
            mRootView.setVisibility(View.GONE);
            return;
        }

        mRootView.setVisibility(View.VISIBLE);

        for (int i = 0; i < mVisibleVectorLayer.size(); i++) {
            MapLayer layer = mVisibleVectorLayer.get(i);
            mVisibleVectorLayerNames.add(layer.getName());
        }

        mCurrentCheckedLayersState = new boolean[mVisibleVectorLayerNames.size()];

        mSpinnerQueryRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                if (itemAtPosition instanceof String) {
                    mCurrentRadius = ((String) itemAtPosition).replace("米", "");
                    refreshView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerQueryLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultiLayerDialog();
            }
        });

        mSpinnerQueryRadius.setSelection(0);
        mCurrentRadius = mQueryRadius[0].replace("米","");
        // 设置默认的图层
        setSpinnerLayerText(mLayerNames);
    }

    /**
     * 选择多个图层的对话框
     */
    private void showMultiLayerDialog() {
        final boolean[] states = getCheckedState();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择图层");
        builder.setIcon(R.drawable.main_icon_maplayers);
        String[] layers = new String[mVisibleVectorLayerNames.size()];
        mVisibleVectorLayerNames.toArray(layers);

        builder.setMultiChoiceItems(layers, states, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                states[which] = isChecked;
            }
        });
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCurrentCheckedLayersState = states;
                setSpinnerLayerText();
                dialog.dismiss();
                refreshView();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mLayerDialog = builder.create();
        mLayerDialog.setCanceledOnTouchOutside(true);
        mLayerDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLayerDialog != null && mLayerDialog.isShowing()) {
            mLayerDialog.dismiss();
        }
    }

    /**
     * 设置spinner中显示的图层名称
     */
    private void setSpinnerLayerText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mCurrentCheckedLayersState.length; i++) {
            if (mCurrentCheckedLayersState[i]) {
                sb.append(",").append(mVisibleVectorLayerNames.get(i));
            }
        }
        if (sb.length() != 0) sb.deleteCharAt(0);
        mSpinnerQueryLayer.setText(sb.toString());
    }

    /**
     * 设置选中的图层
     *
     * @param layerText 图层名，多个图层用逗号隔开
     */
    private void setSpinnerLayerText(String layerText) {
        StringBuilder sb = new StringBuilder();
        List<String> list = BaseClassUtil.StringToList(layerText, ",");
        for (String s : list) {
            if (mVisibleVectorLayerNames.contains(s)) {
                sb.append(",").append(s);
            }
        }

        if (sb.length() == 0) {
            mSpinnerQueryLayer.setText(mVisibleVectorLayerNames.get(0));
            return;
        }

        sb.deleteCharAt(0);
        mSpinnerQueryLayer.setText(sb.toString());
    }

    /**
     * 获取当前选中的图层便于弹出多选对话框
     *
     * @return boolean类型集合
     */
    public boolean[] getCheckedState() {
        boolean[] checkedState = new boolean[mVisibleVectorLayerNames.size()];
        // 当前查询的图层
        String layerNames = mSpinnerQueryLayer.getText().toString().trim();
        for (int i = 0; i < mVisibleVectorLayerNames.size(); i++) {
            String name = mVisibleVectorLayerNames.get(i);
            if (layerNames.contains(name)) {
                checkedState[i] = true;
                continue;
            }
            checkedState[i] = false;
        }
        return checkedState;
    }

    /**
     * 刷新数据
     */
    private void refreshView() {
        try {
            int lastIndex = mTabLayout.getSelectedTabPosition();
            String currentLayerName = "";
            if (lastIndex != -1 && mCurrentLayerNames.size() != 0) {
                currentLayerName = mCurrentLayerNames.get(lastIndex);
            }
            mCurrentLayerNames.clear();
            mFragmentList.clear();

            Collections.addAll(mCurrentLayerNames, mSpinnerQueryLayer.getText().toString().trim().split(","));
            if (mCurrentLayerNames.size() == 0){
                mViewPager.setVisibility(View.INVISIBLE);
                mTabLayout.setVisibility(View.INVISIBLE);
                mSpinnerQueryLayer.setHint("请选择图层");
                return;
            }
            for (String s : mCurrentLayerNames) {
                mFragmentList.add(NearbyLayerResultFragment.newInstance(mCurrentRadius, s));
            }

            if (mFragmentList.size() <= 1) {
                mTabLayout.setVisibility(View.GONE);
            } else {
                mTabLayout.setVisibility(View.VISIBLE);
            }

            if (mFragmentList.size() > 4) {
                mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            } else {
                mTabLayout.setTabMode(TabLayout.MODE_FIXED);
            }

            mAdapter.notifyDataSetChanged();
            mViewPager.setOffscreenPageLimit(mFragmentList.size());
            mTabLayout.setupWithViewPager(mViewPager);

            if (!BaseClassUtil.isNullOrEmptyString(currentLayerName)) {
                int index = mCurrentLayerNames.indexOf(currentLayerName);
                mViewPager.setCurrentItem(index == -1 ? 0 : index, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ResultPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<NearbyLayerResultFragment> fragmentLsit;

        ResultPagerAdapter(FragmentManager fm, ArrayList<NearbyLayerResultFragment> fragmentList) {
            super(fm);
            this.fragmentLsit = fragmentList;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCurrentLayerNames.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentLsit.get(position);
        }

        @Override
        public int getCount() {
            return mCurrentLayerNames.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }
}
