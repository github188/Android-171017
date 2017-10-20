package com.repair.zhoushan.module.devicecare.consumables;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.devicecare.consumables.entity.GongChangBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.GongSiBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.QiYeBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.SAPBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoKuBean;
import com.repair.zhoushan.module.devicecare.consumables.entity.WuLiaoZuBean;
import com.repair.zhoushan.module.devicecare.consumables.view.ExpandTabView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeDoubleListView;
import com.repair.zhoushan.module.devicecare.consumables.view.ListTreeSingleListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AddMaterialFragment extends Fragment {

    //region View

    private ExpandTabView expandTabView;

    private ArrayList<View> mViewArray = new ArrayList<View>();

    // 工厂、物料库(二级)
    private ListTreeDoubleListView viewLeft;
    // 成本中心
    private ListTreeSingleListView viewMiddle;
    // 物料组
    private ListTreeSingleListView viewRight;

    private ListView mListView;
    private AddMaterialListAdpater mWuliaoListAdpater;

    //endregion

    //region Data

    // 企业
    private QiYeBean enterprise;

    // 当前成本中心可选值列表
    private List<SAPBean> costCenterList = new ArrayList<SAPBean>();
    // 当前物料组可选值列表
    private List<SAPBean> materialGroupList = new ArrayList<SAPBean>();

    // 所有的工厂及物料库数据，是转换过的数据，用于过滤菜单用
    private List<SAPBean> factoryList = new LinkedList<SAPBean>();
    private List<Integer> companyIndexList = new LinkedList<Integer>(); // 暂用于记录工厂的所属公司，与factoryList
    private List<List<SAPBean>> warehouseList = new LinkedList<List<SAPBean>>();

    // 当前物料列表数据
    private List<WuLiaoBean> materialList = new ArrayList<WuLiaoBean>();

    // 生成物料订单用
    private SAPBean curCompany;
    private SAPBean curFactory;
    private SAPBean curCostCenter;
    private SAPBean curWarehouse;
    private SAPBean curMaterialGroup;
    private List<WuLiaoBean> orderedMaterialList = new LinkedList<WuLiaoBean>();

    // 是否存在成本中心, default false.
    private boolean isExistCostCenter;

    //endregion

    public ArrayList<WuLiaoBean> getData() {
        return (ArrayList<WuLiaoBean>) materialList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isExistCostCenter = args.getBoolean("IsExistCostCenter");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.add_material_view, container, false);

        this.expandTabView = (ExpandTabView) view.findViewById(R.id.expandtab_view);
        this.mListView = (ListView) view.findViewById(R.id.listView);
        this.mWuliaoListAdpater = new AddMaterialListAdpater(getActivity(), materialList);
        mListView.setAdapter(mWuliaoListAdpater);

        initView();
        initListener();
        initData();

        return view;
    }

    private void initData() {

        MmtBaseTask<String, Void, ResultData<QiYeBean>> fetchInitDataTask = new MmtBaseTask<String, Void, ResultData<QiYeBean>>(getActivity(), true) {
            @Override
            protected ResultData<QiYeBean> doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchWuLiaoChenBenList";

                String result = NetUtil.executeHttpGet(url, "userID", params[0], "gcCode", params[1]);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }
                ResultData<QiYeBean> resultData = new Gson().fromJson(result, new TypeToken<ResultData<QiYeBean>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<QiYeBean> resultData) {
                String defErrMsg = "获取工厂信息失败";

                if (resultData == null) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();

                } else if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(),
                            TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_LONG).show();
                } else {
                    enterprise =  resultData.getSingleData();

                    // 构造过滤列表的数据结构
                    LinkedList<SAPBean> subList;
                    int companyIndex = -1;
                    for (GongSiBean company : enterprise.GongSiList) {
                        companyIndex++;
                        for (GongChangBean gongChangBean : company.GongChangList) {
                            companyIndexList.add(companyIndex);
                            factoryList.add(new SAPBean(gongChangBean.getCode(), gongChangBean.getName()));

                            subList = new LinkedList<SAPBean>();
                            for (WuLiaoKuBean wuLiaoKuBean : gongChangBean.WuLiaoKuList) {
                                subList.add(new SAPBean(wuLiaoKuBean.getCode(), wuLiaoKuBean.getName()));
                            }
                            warehouseList.add(subList);
                        }
                    }
                    viewLeft.notifyDataSetChanged();
                }
            }
        };
        fetchInitDataTask.setCancellable(false);
        fetchInitDataTask.mmtExecute(MyApplication.getInstance().getUserId() + "", "");

    }

    private void initView() {

        ArrayList<String> mTextArray = new ArrayList<String>();

        mTextArray.add("物料库");
        this.viewLeft = new ListTreeDoubleListView(getActivity(), factoryList, warehouseList);
        mViewArray.add(viewLeft);

        if (isExistCostCenter) {
            mTextArray.add("成本中心");
            this.viewMiddle = new ListTreeSingleListView(getActivity(), costCenterList);
            mViewArray.add(viewMiddle);
        }

        mTextArray.add("物料组");
        this.viewRight = new ListTreeSingleListView(getActivity(), materialGroupList);
        mViewArray.add(viewRight);

        expandTabView.setValue(mTextArray, mViewArray);
    }

    private void initListener() {

        viewLeft.setOnSelectListener(new ListTreeDoubleListView.OnItemSelectListener() {

            private int lastClickFactoryIndex = -1;

            @Override
            public void getValue(int lOneSelIndex, int lTwoSelIndex, String showText) {
                onRefresh(viewLeft, showText);

                curCompany = enterprise.GongSiList.get(companyIndexList.get(lOneSelIndex));
                curFactory = factoryList.get(lOneSelIndex);
                curWarehouse = warehouseList.get(lOneSelIndex).get(lTwoSelIndex);

                if (isExistCostCenter) {

                    // 工厂发生变化更新成本中心的信息
                    if (lastClickFactoryIndex != lOneSelIndex) {

                        costCenterList.clear();

                        GongSiBean gongSiBean = (GongSiBean) curCompany;
                        for (GongChangBean gongChangBean : gongSiBean.GongChangList) {
                            if (gongChangBean.getCode().equals(curFactory.getCode())) {
                                costCenterList.addAll(gongChangBean.ChenBenCenterList);
                                viewMiddle.notifyDataSetChanged();

                                lastClickFactoryIndex = lOneSelIndex;
                                break;
                            }
                        }
                    }
                }

                // 更新物料组的信息
                updateMaterialGroupList(curFactory.getCode(), curWarehouse.getCode());
            }
        });

        if (isExistCostCenter) {
            viewMiddle.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {
                @Override
                public void getValue(int selectIndex, String showText) {
                    onRefresh(viewMiddle, showText);

                    curCostCenter = costCenterList.get(selectIndex);
                }
            });
        }

        viewRight.setOnSelectListener(new ListTreeSingleListView.OnSelectListener() {

            int curSelectedIndex = -1;
            String curShowText = "";

            @Override
            public void getValue(int selectIndex, String showText) {

                // 切换物料库的时候会收集选中的物料数据（数据中需填充成本中心信息），所以必须先选定成本中心
                if (isExistCostCenter && curCostCenter == null) {
                    Toast.makeText(getActivity(), "请先选择成本中心", Toast.LENGTH_SHORT).show();
                    viewRight.clearSelection();
                    return;
                }

                if (selectIndex == curSelectedIndex && showText.equals(curShowText)) {
                    return;
                } else {
                    curSelectedIndex = selectIndex;
                    curShowText = showText;
                }

                onRefresh(viewRight, showText);

                curMaterialGroup = materialGroupList.get(selectIndex);

                // 跳转物料组的时候收集当前组中选中的物料
                collectSelectedMaterial();

                updateMaterialList(curMaterialGroup.getCode(), curFactory.getCode(), curWarehouse.getCode());
            }
        });
    }

    /**
     * 收集当前物料组中被选中的物料
     */
    private void collectSelectedMaterial() {

        for (WuLiaoBean wuLiaoBean : materialList) {

            int index = orderedMaterialList.indexOf(wuLiaoBean);

            if (index >= 0)  {
                WuLiaoBean selectedBean = orderedMaterialList.get(index);
                if (wuLiaoBean.isCheck() && wuLiaoBean.getNum() > 0) {
                    selectedBean.setNum(wuLiaoBean.getNum());
                } else {
                    orderedMaterialList.remove(index);
                }
            } else {
                if (wuLiaoBean.isCheck() && wuLiaoBean.getNum() > 0) {
                    orderedMaterialList.add(wuLiaoBean);
                }
            }
        }
    }

    public List<WuLiaoBean> getOrderedMaterialList() {
        collectSelectedMaterial();
        return orderedMaterialList;
    }

    public void clearMaterialOrder() {
        orderedMaterialList.clear();

        for (WuLiaoBean bean : materialList) {
            bean.setIsCheck(false);
            bean.setNum(0);
        }
        refreshListView();
    }

    private void onRefresh(View view, String showText) {

        expandTabView.onPressBack();
        int position = mViewArray.indexOf(view);
        if (position >= 0 && !expandTabView.getTitle(position).equals(showText)) {
            expandTabView.setTitle(showText, position);
        }
    }

    private void updateMaterialGroupList(String gcCode, String kuCode) {

        MmtBaseTask<String, Void, ResultData<WuLiaoZuBean>> getMaterialGroupTask = new MmtBaseTask<String, Void, ResultData<WuLiaoZuBean>>(getActivity(), true) {
            @Override
            protected ResultData<WuLiaoZuBean> doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchWuLiaoZuByKu";

                String result = NetUtil.executeHttpGet(url, "gcCode", params[0], "kuCode", params[1]);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }

                ResultData<WuLiaoZuBean> resultData = new Gson().fromJson(result, new TypeToken<ResultData<WuLiaoZuBean>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<WuLiaoZuBean> resultData) {
                String defErrMsg = "获取物料组信息失败";
                if (resultData == null) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (resultData.ResultCode != 200) {
                    Toast.makeText(getActivity(),
                            TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                materialGroupList.clear();
                materialGroupList.addAll(resultData.DataList);
                viewRight.notifyDataSetChanged();

                // 清空列表数据
                materialList.clear();
                refreshListView();
            }
        };
        getMaterialGroupTask.setCancellable(false);
        getMaterialGroupTask.mmtExecute(gcCode, kuCode);

    }

    private void updateMaterialList(final String groupCode, final String gcCode, final String kuCode) {

        MmtBaseTask<String, Void, ResultData<WuLiaoBean>> getMaterialTask = new MmtBaseTask<String, Void, ResultData<WuLiaoBean>>(getActivity(), true) {
            @Override
            protected ResultData<WuLiaoBean> doInBackground(String... params) {

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/FetchWuLiaoByZu";

                String result = NetUtil.executeHttpGet(url, "gcCode", gcCode, "kuCode", kuCode, "zuCode", groupCode);

                if (TextUtils.isEmpty(result)) {
                    return null;
                }

                ResultData<WuLiaoBean> resultData = new Gson().fromJson(result, new TypeToken<ResultData<WuLiaoBean>>() {
                }.getType());

                return resultData;
            }

            @Override
            protected void onSuccess(ResultData<WuLiaoBean> resultData) {

                materialList.clear();

                String defErrMsg = "获取物料列表失败";
                if (resultData == null) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                } else if (resultData.ResultCode != 200) {
                    if (!TextUtils.isEmpty(resultData.ResultMessage))
                        defErrMsg = resultData.ResultMessage;
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_LONG).show();
                } else {
                    materialList.addAll(resultData.DataList);

                    resolveDataList(materialList);
                }
                refreshListView();
            }
        };
        getMaterialTask.setCancellable(false);
        getMaterialTask.mmtExecute();
    }

    private void resolveDataList(final List<WuLiaoBean> materialList) {

        for (WuLiaoBean bean : materialList) {
            bean.GongSi = curCompany;
            bean.GongChang = curFactory;
            bean.WuLiaoKu = curWarehouse;
            bean.WuLiaoZu = curMaterialGroup;
            bean.ChenBenCenter = isExistCostCenter ? curCostCenter : new SAPBean();
        }

        for (WuLiaoBean wuLiaoBean : materialList) {
            int index = orderedMaterialList.indexOf(wuLiaoBean);
            if (index >= 0) {
                wuLiaoBean.setIsCheck(true);
                wuLiaoBean.setNum(orderedMaterialList.get(index).getNum());
            }
        }
    }

    public boolean onPressBack() {
        return !expandTabView.onPressBack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddMaterialActivity.RC_SEARCH_MATERIAL && resultCode == Activity.RESULT_OK) {
            ArrayList<WuLiaoBean> selectedMaterialList = data.getParcelableArrayListExtra("SelectedMaterialList");
            for (WuLiaoBean wuLiaoBean : selectedMaterialList) {
                for (WuLiaoBean bean : materialList) {
                    if (bean.getCode().equals(wuLiaoBean.getCode()) && bean.getName().equals(wuLiaoBean.getName())) {
                        bean.setIsCheck(true);
                        bean.setNum(wuLiaoBean.getNum());
                        break;
                    }
                }
            }
        }
        refreshListView();
    }

    private void refreshListView() {
        mWuliaoListAdpater.notifyDataSetChanged();
    }
}
