package com.patrol.module.posandpath2.detailinfo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * 详情界面
 */
public class DetailInfoFragment extends Fragment {
    private static final String ARG_PARAM_DATA = "data";
    private static final String ARG_PARAM_OBJECT = "object";

    private LinkedHashMap<String, LinkedHashMap<String, String>> mGroupData;

    private LinearLayout mDataLinearLayout;
    private FrameLayout mLoadFrame;
    private FrameLayout mEmptyFrame;

    public DetailInfoFragment() {
    }

    public static DetailInfoFragment newInstance(LinkedHashMap<String, LinkedHashMap<String, String>> mGroupData) {
        DetailInfoFragment fragment = new DetailInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_DATA, mGroupData);
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailInfoFragment newInstance(DetailInfoMapData obj) {
        DetailInfoFragment fragment = new DetailInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_OBJECT, obj);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        if (getArguments().containsKey(ARG_PARAM_DATA)) {
            mGroupData = (LinkedHashMap<String, LinkedHashMap<String, String>>) getArguments().getSerializable(ARG_PARAM_DATA);
        } else if (getArguments().containsKey(ARG_PARAM_OBJECT)) {
            mGroupData = ((DetailInfoMapData) getArguments().getParcelable(ARG_PARAM_OBJECT)).toMapData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_info, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        mDataLinearLayout = (LinearLayout) view.findViewById(R.id.dataList);
        mLoadFrame = (FrameLayout) view.findViewById(R.id.loadingData);
        mEmptyFrame = (FrameLayout) view.findViewById(R.id.emptyData);
    }


    private void initData() {
        mLoadFrame.setVisibility(View.VISIBLE);
        if (mGroupData == null || mGroupData.size() == 0) {
            mLoadFrame.setVisibility(View.GONE);
            mEmptyFrame.setVisibility(View.VISIBLE);
        }
        showListData();
        mLoadFrame.setVisibility(View.GONE);
        mEmptyFrame.setVisibility(View.GONE);
    }

    private void showListData() {
        // 组名
        Set<String> keyGroupNames = mGroupData.keySet();
        for (String groupName : keyGroupNames) {

            ViewGroup groupView = createGroupView(groupName);
            mDataLinearLayout.addView(groupView);

            HashMap<String, String> tempMap = mGroupData.get(groupName);
            Set<String> keyItemNames = tempMap.keySet();

            for (String itemName : keyItemNames) {

                ViewGroup itemView = createItemView(itemName, tempMap.get(itemName));
                mDataLinearLayout.addView(itemView);
            }
        }
    }

    private ViewGroup createItemView(String itemName, String itemValue) {
        ViewGroup view = (ViewGroup) View.inflate(getActivity(), R.layout.layout_item_view, null);
        TextView tvName = (TextView) view.findViewById(R.id.textName);
        TextView tvValue = (TextView) view.findViewById(R.id.textValue);
        tvName.setText(itemName + "：");
        tvValue.setText(itemValue);
        return view;
    }

    private ViewGroup createGroupView(String groupName) {
        ViewGroup view = (ViewGroup) View.inflate(getActivity(), R.layout.layout_item_group, null);
        TextView textView = (TextView) view.findViewById(R.id.textData);
        textView.setText(groupName);
        return view;
    }

}
