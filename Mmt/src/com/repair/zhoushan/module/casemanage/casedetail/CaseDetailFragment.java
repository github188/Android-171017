package com.repair.zhoushan.module.casemanage.casedetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.R;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.entity.FeedbackInfo;

import java.util.ArrayList;
import java.util.List;

public class CaseDetailFragment extends Fragment {

    private FrameLayout maintenanceFormMid;
    private LinearLayout maintenanceFormTop;
    private MultiSwitchButton switchButton;

    public List<Fragment> fragments = new ArrayList<>();
    private int showFragmentIndex = 0;

    private ArrayList<String> groupTitles = new ArrayList<>();
    private ArrayList<GDFormBean> gdFormBeans;
    private CaseItem caseItemEntity;

    private List<FeedbackInfo> feedbackInfos;

    private CaseDetailFragment(String[] groupTitles, ArrayList<GDFormBean> gdFormBeans, CaseItem caseItem) {

        this.gdFormBeans = gdFormBeans;
        this.caseItemEntity = caseItem;

        for (String title : groupTitles) {
            this.groupTitles.add(title);
        }
    }

    public CaseDetailFragment(ArrayList<GDFormBean> gdFormBeans, CaseItem caseItem) {
        this(new String[]{"基本信息", "办理过程"}, gdFormBeans, caseItem);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maintenance_form, container, false);

        maintenanceFormMid = (FrameLayout) view.findViewById(R.id.maintenanceFormMid);
        maintenanceFormTop = (LinearLayout) view.findViewById(R.id.maintenanceFormTop);

        switchButton = (MultiSwitchButton) view.findViewById(R.id.maintenanceFormTitle);
        switchButton.setOnScrollListener(new MultiSwitchButton.OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                if (index != showFragmentIndex) {
                    showFragment(index);
                }
            }
        });

        return view;
    }

    /**
     * 显示指定的Fragment
     *
     * @param index 位置
     */
    public void showFragment(int index) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        ft.hide(fragments.get(showFragmentIndex));
        ft.show(fragments.get(index));

        showFragmentIndex = index;

        ft.commit();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {

            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

            // 基本信息
            Fragment fragment0 = new CaseDetailFlowFragment(groupTitles, gdFormBeans);

            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            frameLayout.setId(frameLayout.hashCode());
            maintenanceFormMid.addView(frameLayout);
            ft.replace(frameLayout.getId(), fragment0);

            fragments.add(fragment0);

            // 办理过程
            Fragment fragment1 = new CaseHandleProcedureFragment();
            Bundle augBundle = new Bundle();
            augBundle.putParcelable("ListItemEntity", caseItemEntity);
            fragment1.setArguments(augBundle);

            FrameLayout frameLayout1 = new FrameLayout(getActivity());
            frameLayout1.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            frameLayout1.setId(frameLayout1.hashCode());

            maintenanceFormMid.addView(frameLayout1);
            ft.replace(frameLayout1.getId(), fragment1);
            ft.hide(fragment1);

            fragments.add(fragment1);

            // 多次反馈
            Bundle bundle = getArguments();
            if (bundle != null) {
                String feedbackInfoStr = bundle.getString("feedbackInfos");
                if (!TextUtils.isEmpty(feedbackInfoStr)) {
                    feedbackInfos = new Gson().fromJson(feedbackInfoStr, new TypeToken<List<FeedbackInfo>>() {
                    }.getType());
                }

                if (feedbackInfos != null) {
                    for (FeedbackInfo fbi : feedbackInfos) {
                        String fbBizName = fbi.FBBiz;
                        String tablename = fbi.FBTable;
                        if (TextUtils.isEmpty(fbBizName) || TextUtils.isEmpty(tablename)) {
                            continue;
                        }
                        this.groupTitles.add(fbi.FBBiz + "列表");
                        Fragment feedBackFragment = new FeedBackListFragment();

                        Bundle augBundle2 = new Bundle();
                        augBundle2.putBoolean("isRead", fbi.isRead);
                        augBundle2.putString("bizName", fbBizName);
                        augBundle2.putString("caseNo", caseItemEntity.CaseNo);
                        augBundle2.putString("tableName", tablename);

                        augBundle2.putString("feedbackInfo",new Gson().toJson(fbi));
                        feedBackFragment.setArguments(augBundle2);

                        FrameLayout frameLayout2 = new FrameLayout(getActivity());
                        frameLayout2.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                        frameLayout2.setId(frameLayout2.hashCode());
                        maintenanceFormMid.addView(frameLayout2);

                        ft.replace(frameLayout2.getId(), feedBackFragment);
                        ft.hide(feedBackFragment);

                        fragments.add(feedBackFragment);
                    }
                }
            }

            ft.commitAllowingStateLoss();

            switchButton.setContent(groupTitles);
            // 若只有一个标题，则不显示
            if (groupTitles.size() == 0) {
                maintenanceFormTop.setVisibility(View.GONE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}