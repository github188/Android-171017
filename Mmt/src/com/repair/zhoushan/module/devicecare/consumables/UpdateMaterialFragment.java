package com.repair.zhoushan.module.devicecare.consumables;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.R;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;
import com.repair.zhoushan.module.devicecare.consumables.view.ExpandTabView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeSingleListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateMaterialFragment extends Fragment {

    private ExpandTabView filterTabView;
    private ListView mListView;
    private UpdateMaterialAdapter mListAdapter;

    private ArrayList<View> mViewArray = new ArrayList<View>();
    // 工厂
    private ListTreeSingleListView viewLeft;
    // 成本中心
    private ListTreeSingleListView viewRight;

    // 工厂列表
    private List<SAPBean> factoryFilterList = new LinkedList<SAPBean>();
    // Key: factoryCode;  Value: costCenterList
    private Map<String, LinkedList<SAPBean>> allCostCenterFilterList = new HashMap<String, LinkedList<SAPBean>>();
    // 工厂选定后对应的成本中心列表
    private List<SAPBean> costCenterFilterList = new LinkedList<SAPBean>();

    private List<WuLiaoBean> materialList = new ArrayList<WuLiaoBean>();
    // 过滤后的列表数据
    private List<WuLiaoBean> curMaterialList = new LinkedList<WuLiaoBean>();
    private SAPBean curFactory;
    private SAPBean curCostCenter;

    // 是否存在成本中心, default false.
    private boolean isExistCostCenter;

    public List<WuLiaoBean> getMaterialList() {
        return curMaterialList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {

            isExistCostCenter = args.getBoolean("IsExistCostCenter");

            String dataStr = args.getString("MaterialListStr");
            if (!TextUtils.isEmpty(dataStr)) {
                List<WuLiaoBean> dataList = new Gson().fromJson(dataStr, new TypeToken<List<WuLiaoBean>>() {
                }.getType());

                materialList.addAll(dataList);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_material_view, container, false);

        this.filterTabView = (ExpandTabView) view.findViewById(R.id.expandtab_view);
        this.mListView = (ListView) view.findViewById(R.id.listView);
        this.mListAdapter = new UpdateMaterialAdapter(getActivity(), curMaterialList);
        mListView.setAdapter(mListAdapter);

        initView();
        initData();

        initListener();

        return view;
    }

    private void initView() {

        mListView.setBackgroundResource(R.color.default_light_dark);
        mListView.setDivider(new ColorDrawable(0x00000000));
        mListView.setDividerHeight(DimenTool.dip2px(getActivity(), 8));

        ArrayList<String> mTextArray = new ArrayList<String>();

        mTextArray.add("工厂");
        this.viewLeft = new ListTreeSingleListView(getActivity(), factoryFilterList);
        mViewArray.add(viewLeft);

        if (isExistCostCenter) {
            mTextArray.add("成本中心");
            this.viewRight = new ListTreeSingleListView(getActivity(), costCenterFilterList);
            mViewArray.add(viewRight);
        }

        filterTabView.setValue(mTextArray, mViewArray);
    }

    private void initData() {

        if (materialList.size() == 0)
            return;

        List<String> factoryCodeList = new LinkedList<String>();
        LinkedList<SAPBean> subList;

        // 数据分类（一级：工厂；二级：成本中心）
        for (WuLiaoBean wuLiaoBean : materialList) {

            // 空间换时间, 找出所有工厂
            if (!factoryCodeList.contains(wuLiaoBean.GongChang.getCode())) {
                factoryCodeList.add(wuLiaoBean.GongChang.getCode());
                factoryFilterList.add(wuLiaoBean.GongChang);
            }

            if (isExistCostCenter) {

                // 时间换空间，按成本中心分组
                if (allCostCenterFilterList.containsKey(wuLiaoBean.GongChang.getCode())) {
                    subList = allCostCenterFilterList.get(wuLiaoBean.GongChang.getCode());

                    boolean isContained = false;
                    for (SAPBean sapBean : subList) {
                        if (sapBean.getCode().equals(wuLiaoBean.ChenBenCenter.getCode())) {
                            isContained = true;
                            break;
                        }
                    }
                    if (!isContained) {
                        subList.add(wuLiaoBean.ChenBenCenter);
                    }

                } else {
                    subList = new LinkedList<SAPBean>();
                    subList.add(wuLiaoBean.ChenBenCenter);
                    allCostCenterFilterList.put(wuLiaoBean.GongChang.getCode(), subList);
                }
            }
        }

        curFactory = factoryFilterList.get(0);
        filterTabView.setTitle(curFactory.getName(), 0);
        viewLeft.setSelectedPosition(0);

        if (isExistCostCenter) {
            costCenterFilterList.clear();
            costCenterFilterList.addAll(allCostCenterFilterList.get(curFactory.getCode()));
            curCostCenter = costCenterFilterList.get(0);
            filterTabView.setTitle(curCostCenter.getName(), 1);
            viewRight.setSelectedPosition(0);
        }

        filterData();
    }

    private void initListener() {

        viewLeft.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
            @Override
            public void getValue(int selectIndex, String showText) {
                onRefresh(viewLeft, showText);

                if (!curFactory.getCode().equals(factoryFilterList.get(selectIndex).getCode())) {

                    curFactory = factoryFilterList.get(selectIndex);

                    if (isExistCostCenter) {
                        // 工厂切换后成本中心也要跟着切换
                        costCenterFilterList.clear();
                        costCenterFilterList.addAll(allCostCenterFilterList.get(curFactory.getCode()));
                        curCostCenter = costCenterFilterList.get(0);
                        viewRight.setSelectedPosition(0);
                    }

                    filterData();
                }
            }
        });

        if (isExistCostCenter) {

            viewRight.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
                @Override
                public void getValue(int selectIndex, String showText) {
                    onRefresh(viewRight, showText);

                    if (!curCostCenter.getCode().equals(costCenterFilterList.get(selectIndex).getCode())) {
                        curCostCenter = costCenterFilterList.get(selectIndex);
                        filterData();
                    }
                }
            });
        }
    }

    private void filterData() {

        curMaterialList.clear();

        for (WuLiaoBean wuLiaoBean : materialList) {
            if (wuLiaoBean.GongChang.getCode().equals(curFactory.getCode())) {

                if (isExistCostCenter) {
                    if (wuLiaoBean.ChenBenCenter.getCode().equals(curCostCenter.getCode())) {
                        curMaterialList.add(wuLiaoBean);
                    }
                } else {
                    curMaterialList.add(wuLiaoBean);
                }
            }
        }

        mListAdapter.notifyDataSetChanged();
    }

    private void onRefresh(View view, String showText) {

        filterTabView.onPressBack();
        int position = mViewArray.indexOf(view);
        if (position >= 0 && !filterTabView.getTitle(position).equals(showText)) {
            filterTabView.setTitle(showText, position);
        }
    }

}
