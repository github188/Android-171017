package com.mapgis.mmt.module.systemsetting.backgruoundinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.systemsetting.backgruoundinfo.items.BackgroundTaskStateItem;
import com.mapgis.mmt.module.systemsetting.backgruoundinfo.items.NetRequestItem;
import com.mapgis.mmt.module.systemsetting.backgruoundinfo.items.TrafficSettingItem;
import com.mapgis.mmt.module.systemsetting.setting.ISettingItem;
import com.mapgis.mmt.module.systemsetting.setting.settingitem.LockScreenSettingItem;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/22 0022.
 */

public class BackgroundInfoFragment extends Fragment {
    private List<ISettingItem> mSettingItemList;

    public BackgroundInfoFragment() {

    }

    public static BackgroundInfoFragment newInstance() {
        return new BackgroundInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_setting_bginfo, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        try{
            mSettingItemList = new ArrayList<>();
            //流量统计
            mSettingItemList.add(new TrafficSettingItem(getActivity(),findView(view,R.id.itemTraffic)));
            // 后台任务
            mSettingItemList.add(new BackgroundTaskStateItem(getActivity(), findView(view, R.id.itemBackgroundTask)));
            //网络请求
            mSettingItemList.add(new NetRequestItem(getActivity(),findView(view,R.id.itemNetRequest)));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        LockScreenSettingItem lockItem = (LockScreenSettingItem) getItemView(R.id.itemLockScreen);
        if (lockItem != null){
            lockItem.setPasswordTypeText();
        }
    }

    private ISettingItem getItemView(int id) {
        for (ISettingItem item : mSettingItemList) {
            if (item.getViewId() == id) {
                return item;
            }
        }
        return null;
    }
    private View findView(View view, int id) {
        return view.findViewById(id);
    }
}
