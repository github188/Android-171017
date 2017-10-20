package com.repair.zhoushan.module.casemanage.casedetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;

/**
 * 左端一个ListView展示节点，右端展示节点详情
 */
public class CaseDetailListFragment extends Fragment{

    private ArrayList<String> groupTitles;
    private ArrayList<GDFormBean> gdFormBeans;

    private ListView mListView;
    private FrameLayout mGroupContent;

    private FlowBeanFragment selectedFragment;

    public CaseDetailListFragment(ArrayList<String> groupTitles, ArrayList<GDFormBean> gdFormBeans) {
        this.groupTitles = groupTitles;
        this.gdFormBeans = gdFormBeans;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.case_detail_view, container, false);

        mListView = (ListView) view.findViewById(R.id.groupTitle);
        mGroupContent = (FrameLayout) view.findViewById(R.id.groupContent);

        mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.case_detail_title_item, groupTitles));
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectItem(gdFormBeans.size() - 1);
    }

    private void selectItem(int position) {

        selectedFragment = new FlowBeanFragment();

        Bundle args = new Bundle();
        args.putParcelable("GDFormBean", gdFormBeans.get(position));
        selectedFragment.setArguments(args);

        selectedFragment.setCls(CaseDetailActivity.class);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.groupContent, selectedFragment).commit();

    }
}
