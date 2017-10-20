package com.mapgis.mmt.module.systemsetting.download;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.module.systemsetting.SettingUtil;
import com.mapgis.mmt.module.systemsetting.itemwidget.SwitchItemSettingView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/4/17.
 * 下载管理
 */

public class DownloadFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "DownloadFragment";

    private static final String PARAM_REFRESH = "refresh";

    private DownloadBroadcastReceiver mDownloadReceiver;

    private TextView mTvMapPath;
    private SwitchItemSettingView mAutoDownloadOnWiFi;
    private RecyclerView mDownloadrecyclerView;
    private DownloadAdapter mAdapter;
    private List<DownloadInfo> mDownloadList;

    private boolean isRefresh = false;

    // 移动网络时下载提醒
    private boolean isMobileOk = false;

    public DownloadFragment() {
    }

    public static DownloadFragment newInstance(boolean isRefresh) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_REFRESH, isRefresh);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isRefresh = bundle.getBoolean(PARAM_REFRESH, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_layout, container, false);
        mTvMapPath = (TextView) view.findViewById(R.id.tv_map_save_path);
        mAutoDownloadOnWiFi = (SwitchItemSettingView) view.findViewById(R.id.autoDownloadOnWiFi);
        mDownloadrecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initView();
        initData();
    }

    private void initView() {
        mTvMapPath.setText(String.format("地图存储位置：%s", getMapPath()));
        mTvMapPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterSystemFileManagerView();
            }
        });
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setSupportsChangeAnimations(false);
        mDownloadrecyclerView.setItemAnimator(defaultItemAnimator);
        mDownloadrecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        // WiFi下自动下载设置初始化
        mAutoDownloadOnWiFi.setSwitchChecked(SettingUtil.loadAutoDownloadSetting());
        mAutoDownloadOnWiFi.setOnCheckedChangeListener(this);
    }

    private String getMapPath() {
        return Battle360Util.getFixedPath(Battle360Util.GlobalPath.Map);
    }

    /*进入系统文件管理界面*/
    private void enterSystemFileManagerView() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(getMapPath()), "*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SettingUtil.saveAutoDowloadSetting(isChecked);
    }

    private void initData() {
        mDownloadList = new ArrayList<>();
        mAdapter = new DownloadAdapter(getActivity(), mDownloadList);
        mAdapter.setHasStableIds(true);
        mDownloadrecyclerView.setAdapter(mAdapter);

        if (!isRefresh) {
            // 不刷新布局
            mDownloadList.addAll(DownloadManager.getInstance().getFileList());
            mAdapter.notifyDataSetChanged();
        } else {
            // 刷新布局
        }
    }

    private void refreshData() {
        /*DownloadManager.getInstance().queryDownMapsTask(new DownloadManager.QueryDownMapCallback() {
            @Override
            public void querySuccess(List<DownloadMap> needDownloadMaps) {
                if (needDownloadMaps == null || needDownloadMaps.size() == 0) {
                    // 地图已经是最新的了
                    return;
                }
                mDownloadMaps=new ArrayList<>();
                mDownloadMaps.addAll(needDownloadMaps);
            }

            @Override
            public void queryFailed() {

            }
        });*/
    }

    @Override
    public void onStart() {
        mDownloadReceiver = DownloadBroadcastReceiver.registReceiver(getActivity()
                , DownloadBroadcastReceiver.ACTION_RECEIVER_UPDATE);
        mDownloadReceiver.setOnDownloadNotificationListener(
                new DownloadBroadcastReceiver.OnDownloadNotificationListener() {
                    @Override
                    public void onNotify(DownloadInfo info) {
                        int index = mDownloadList.indexOf(info);
                        if (index != -1) {
                            mDownloadList.remove(info);
                            mDownloadList.add(index, info);
                            mAdapter.notifyItemChanged(index);
                        }
                    }
                });
        super.onStart();
    }

    @Override
    public void onPause() {
        try {
            DownloadManager.getInstance().reloadData(this.mDownloadList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        DownloadBroadcastReceiver.unregistReceiver(getActivity(), mDownloadReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        DownloadHolderManager.getInstance().clearDownloadHolder();
        super.onDestroy();
    }
}