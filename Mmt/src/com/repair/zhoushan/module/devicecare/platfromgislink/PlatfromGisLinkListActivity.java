package com.repair.zhoushan.module.devicecare.platfromgislink;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.repair.common.CaseSearchActivity;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.ConfigFieldsAdapter;
import com.repair.zhoushan.common.Utils;
import com.repair.zhoushan.module.devicecare.DeviceCareListFragment;
import com.repair.zhoushan.module.devicecare.TableColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/3/22.
 */
public class PlatfromGisLinkListActivity extends BaseActivity {
    public static final int TASK_SEARCH_REQUEST_CODE = 0x110;


    @Override
    protected void setDefaultContentView() {

        setSwipeBackEnable(false);

        Fragment fragment = new PlatfromGisLinkListFragment();

        Bundle argBundle = new Bundle();
        argBundle.putString("type", getIntent().getStringExtra("type"));
        fragment.setArguments(argBundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(android.R.id.content, fragment, DeviceCareListFragment.class.getName());
        ft.show(fragment);
        ft.commitAllowingStateLoss();
    }

    public static class PlatfromGisLinkListFragment extends Fragment {
        private Activity mActivity;
        private int userId;
        private String type;

        public EditText txtSearch;
        protected ConfigFieldsAdapter adapter;

        private String hintTxt = "编号、位置、设备名称";

        //从服务器获取的数据列表
        private ArrayList<DeviceModel> dataList = new ArrayList<>();

        //adapter 需要的数据列表
        private ArrayList<List<TableColumn>> adapterdataList = new ArrayList<List<TableColumn>>();

        private PullToRefreshListView mPullRefreshListView;

        private int currentPageIndex = 1; // Start from 1
        private final int pageSize = 10;
        ResultData<String> resultData = new ResultData<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mActivity = getActivity();
            this.userId = MyApplication.getInstance().getUserId();
            this.type = getArguments().getString("type");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.platform_task_list, container, false);
            initActionBar(view);
            return view;
        }

        TextView deviceType;

        private void initActionBar(final View view) {

            view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.onBackPressed();
                }
            });

            txtSearch = (EditText) view.findViewById(R.id.txtSearch);
            txtSearch.setVisibility(View.VISIBLE);
            txtSearch.setHint(hintTxt);
            txtSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), CaseSearchActivity.class);
                    intent.putExtra("key", txtSearch.getText().toString());
                    intent.putExtra("searchHistoryKey", "DeviceCareSearchHistory");
                    intent.putExtra("searchHint", hintTxt);

                    startActivityForResult(intent, TASK_SEARCH_REQUEST_CODE);
                }
            });
            deviceType = (TextView) view.findViewById(R.id.deviceType);
            deviceType.setText("加载中...");
            new MmtBaseTask<Void, Void, String>(mActivity) {
                @Override
                protected String doInBackground(Void... params) {
                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                    sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                            .append(String.valueOf(userId)).append("/DeviceNames")
                            .append("?deviceType=").append(type);
                    return NetUtil.executeHttpGet(sb.toString());
                }

                @Override
                protected void onSuccess(String s) {
                    super.onSuccess(s);
                    resultData = Utils.json2ResultDataToast(String.class, context, s, "由设备类型获取该类设备名称集合错误", false);
                    if (resultData == null) {
                        return;
                    }
                    deviceType.setText(resultData.DataList.get(0));
                    //获取到设备后开始查询
                    mPullRefreshListView.setRefreshing(false);
                }
            }.mmtExecute();
            view.findViewById(R.id.layoutType).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (resultData == null || resultData.DataList.size() == 0) {
                        Toast.makeText(mActivity, "没有可选的设备", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ListDialogFragment listDialogFragment = new ListDialogFragment("台账", resultData.DataList);
                    listDialogFragment.show(((BaseActivity) mActivity).getSupportFragmentManager(), "");
                    listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            deviceType.setText(value);
                            currentPageIndex = 1;
                            loadData();
                        }
                    });
                }
            });
            initContentView(view);
        }

        private void initContentView(View view) {

            this.mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.contentListView);
            ListView actualListView = mPullRefreshListView.getRefreshableView();
            mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            registerForContextMenu(actualListView);

            // 下拉刷新时的提示文本设置
            mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
            mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新");
            mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("放开以刷新");

            // 上拉加载更多时的提示文本设置
            mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载更多");
            mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
            mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("放开以加载");

            PullToRefreshBase.OnRefreshListener2<ListView> mOnRefreshListener2 = new PullToRefreshBase.OnRefreshListener2<ListView>() {
                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME
                                    | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                    refreshView.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(label);
                    refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                    currentPageIndex = 1;
                    loadData();
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME
                                    | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                    refreshView.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(label);

                    currentPageIndex++;
                    loadData();
                }
            };
            mPullRefreshListView.setOnRefreshListener(mOnRefreshListener2);
            adapter = new ConfigFieldsAdapter(mActivity, adapterdataList);
            mPullRefreshListView.setAdapter(adapter);


            mPullRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mActivity, PlatfromGisLinkDetailActivity.class);
                    intent.putExtra("DeviceModel", new Gson().toJson(dataList.get(position - 1)));
                    startActivityForResult(intent, TASK_SEARCH_REQUEST_CODE);
                }
            });
        }

        public void loadData() {
            final String key = txtSearch.getText().toString().trim();
            final String configName = deviceType.getText().toString();
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... params) {
                    StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                    sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                            .append(String.valueOf(userId)).append("/EquipmentAccountList")
                            .append("?pageSize=").append(pageSize)
                            .append("&pageIndex=").append(currentPageIndex)
                            .append("&sortFields=ID&direction=desc")
                            .append("&configType=" + type)
                            .append("&callWay=mobile")
                            .append("&configName=" + configName);

                    String filter=" GIS编号 is null ";

                    if(MyApplication.getInstance().getConfigValue("distLinkMan",1)==1){
                        filter+=" and  挂接人='" + MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName + "' ";
                    }
                    if (!TextUtils.isEmpty(key)) {
                        filter+=(" and ((编号 like '%" + key + "%') or (位置 like '%" + key + "%') or (设备名称 like '%"+key+"%')) ");
                    }
                    sb.append("&filter="+filter);

                    return NetUtil.executeHttpGet(sb.toString().replaceAll("%", "%25").replaceAll(" ", "%20"));
                }

                @Override
                protected void onPostExecute(String jsonResult) {

                    mPullRefreshListView.onRefreshComplete();

                    ResultData<DeviceModel> newData = Utils.json2ResultDataToast(DeviceModel.class, mActivity, jsonResult, "获取任务失败", true);
                    if (newData == null) {
                        return;
                    }

                    if (newData.DataList.size() == 0) {
                        if (currentPageIndex <= 1) { // Refresh
                            Toast.makeText(mActivity, "没有记录", Toast.LENGTH_SHORT).show();
                            dataList.clear();
                            adapterdataList.clear();
                            adapter.notifyDataSetChanged();
                        } else {               // LoadMore
                            Toast.makeText(mActivity, "没有更多数据", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // dataList.ad
                        if (currentPageIndex <= 1) {
                            dataList.clear();
                            dataList.addAll(newData.DataList);
                            adapterdataList.clear();
                            adapterdataList.addAll(getTableColumnListFromDeviceModels(newData.DataList));
                            adapter.notifyDataSetChanged();
                        } else {
                            dataList.addAll(newData.DataList);
                            adapterdataList.addAll(getTableColumnListFromDeviceModels(newData.DataList));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }.executeOnExecutor(MyApplication.executorService);
        }

        List<List<TableColumn>> getTableColumnListFromDeviceModels(List<DeviceModel> dataList) {
            List<List<TableColumn>> temp = new ArrayList<>();
            for (DeviceModel deviceModel : dataList) {
                if (deviceModel.MobileRow == null || deviceModel.MobileRow.size() == 0) {
                    continue;
                }
                temp.add(deviceModel.MobileRow);
            }
            return temp;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == 1 && requestCode == TASK_SEARCH_REQUEST_CODE) {
                txtSearch.setText(data.getStringExtra("key"));
                currentPageIndex = 1;
                loadData();
                return;
            }
        }
    }
}
