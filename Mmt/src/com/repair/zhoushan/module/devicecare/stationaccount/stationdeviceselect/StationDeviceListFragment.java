package com.repair.zhoushan.module.devicecare.stationaccount.stationdeviceselect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.filtermenubar.FilterMenuBar;
import com.filtermenubar.Node;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.common.Constants;
import com.repair.zhoushan.common.Utils;

import java.util.ArrayList;
import java.util.List;

public class StationDeviceListFragment extends Fragment {

    private final String bizName = "场站设备";

    private final ArrayList<DeviceModel> dataList = new ArrayList<DeviceModel>();

    private FilterMenuBar filterMenuBar;
    private ListView listView;
    private DeviceModelAdapter listAdapter;

    //region Filter Bar.
    private final List<Station> stationList = new ArrayList<>();

    private static final String FLAG_ALL = "全部";
    private final String filterGroupStation = "场站";
    private final String filterGroupDeviceType = "设备类型";

    private Station curStation;
    private String curDeviceType;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_device_select, container, false);

        this.listView = (ListView) view.findViewById(R.id.listView);
        this.listAdapter = new DeviceModelAdapter(getActivity(), dataList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdapter.onItemClick(position);
            }
        });

        initFilterBar(view);

        return view;
    }

    private void initFilterBar(View view) {

        this.filterMenuBar = (FilterMenuBar) view.findViewById(R.id.filterView);
        filterMenuBar.setOnFilterItemSelectedListener(new FilterMenuBar.OnFilterItemSelectedListener() {
            @Override
            public void onFilterItemSelected(List<List<Node>> selectedGroups, int invokedGroupIndex) {
                for (List<Node> nodeList : selectedGroups) {
                    String groupName = nodeList.get(0).getShowName();
                    Node selectedNode = nodeList.get(nodeList.size() - 1);
                    if (filterGroupStation.equals(groupName)) {
                        Station station = new Station(Integer.parseInt(selectedNode.getValue()), selectedNode.getShowName());
                        int index = stationList.indexOf(station);
                        curStation= stationList.get(index);
                    } else if (filterGroupDeviceType.equals(groupName)) {
                        curDeviceType = selectedNode.getShowName();
                    }
                }
                reloadListData();
            }
        });

        getFilterData();
    }

    /**
     * 获取过滤条件：场站列表和设备列表（两者间没有关联关系）
     */
    private void getFilterData() {

        MmtBaseTask<Void, Void, String[]> mmtBaseTask = new MmtBaseTask<Void, Void, String[]>(getActivity()) {

            @Override
            protected String[] doInBackground(Void... params) {

                // 0.StationName;  1.DeviceType;
                String[] results = new String[2];

                String url0 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/FetchStationList";

                results[0] = NetUtil.executeHttpGet(url0, "userID", userID, "bizName", bizName, "level", String.valueOf(1));

                String url1 = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/" + userID + "/DeviceNames";

                results[1] = NetUtil.executeHttpGet(url1, "bizName", bizName, "level", String.valueOf(2));

                return results;
            }

            @Override
            protected void onSuccess(String[] results) {

                ResultData<Station> resultData;

                // Station
                String defErrMsg = "获取场站信息失败";
                if (TextUtils.isEmpty(results[0])) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                } else {

                    resultData = new Gson().fromJson(results[0], new TypeToken<ResultData<Station>>() {
                    }.getType());

                    if (resultData == null) {
                        Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                    } else if (resultData.ResultCode != 200) {
                        Toast.makeText(getActivity(),
                                TextUtils.isEmpty(resultData.ResultMessage) ? defErrMsg
                                        : resultData.ResultMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        stationList.addAll(resultData.DataList);
                    }
                }

                // 场站信息获取失败即终止
                if (stationList.size() == 0) {
                    Toast.makeText(getActivity(), defErrMsg, Toast.LENGTH_SHORT).show();
                    return;
                }

                Node stationRoot = new Node(filterGroupStation, filterGroupStation);
                for (Station station : stationList) {
                    stationRoot.addChild(new Node(station.Name, String.valueOf(station.ID), station.DeviceType));
                }
                filterMenuBar.appendMenuItem(stationRoot);
                curStation = stationList.get(0);

                // Device type
                ResultData<String> deviceTypeData = Utils.json2ResultDataToast(String.class,
                        getActivity(), results[1], "获取设备类型列表失败", false);

                List<String> deviceTypeList = new ArrayList<>();
                deviceTypeList.add(FLAG_ALL);
                if (deviceTypeData != null) {
                    deviceTypeList.addAll(deviceTypeData.DataList);
                }
                Node deviceTypeRoot = Node.createSimpleTree(filterGroupDeviceType, deviceTypeList, FLAG_ALL);
                filterMenuBar.appendMenuItem(deviceTypeRoot);

                reloadListData();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void reloadListData() {

        MmtBaseTask<String, Void, String> mmtBaseTask = new MmtBaseTask<String, Void, String>(getActivity()) {
            @Override
            protected String doInBackground(String... params) {
                if (curStation == null) {
                    return null;
                }

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                sb.append("/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/")
                        .append(userID).append("/StationEquipsList")
                        .append("?bizName=").append(bizName)
                        .append("&level=").append(String.valueOf("2"))
                        .append("&parentDevice=").append(curStation.DeviceType)
                        .append("&deviceID=").append(curStation.ID);

                if (TextUtils.isEmpty(curDeviceType) || FLAG_ALL.equals(curDeviceType)) {
                    sb.append("&deviceName=").append("");
                } else {
                    sb.append("&deviceName=").append(curDeviceType);
                }

                return NetUtil.executeHttpGet(sb.toString());
            }

            @Override
            protected void onSuccess(String jsonResult) {

                ResultData<DeviceModel> result = Utils.json2ResultDataToast(DeviceModel.class,
                        getActivity(), jsonResult, "获取设备列表失败", true);

                if (result == null) return;

                dataList.clear();
                dataList.addAll(result.DataList);
                listAdapter.notifyDataSetChanged();
                if (dataList.size() > 0) {
                    listView.smoothScrollToPosition(0);
                }
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    // Station Model
    public final class Station {
        public int ID;
        public String Name;
        public String DeviceType;

        public Station(int ID, String name) {
            this.ID = ID;
            this.Name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Station station = (Station) o;

            if (ID != station.ID) return false;
            return Name.equals(station.Name);

        }

        @Override
        public int hashCode() {
            int result = ID;
            result = 31 * result + Name.hashCode();
            return result;
        }
    }

    final class DeviceModelAdapter extends BaseAdapter {

        private final ArrayList<DeviceModel> dataList;
        private final LayoutInflater mLayoutInflater;
        private final Activity mContext;

        private int deviceTypeIndex = -2;
        private int deviceNameIndex = -2;
        private int deviceNoIndex = -2;

        public DeviceModelAdapter(Activity mActivity, ArrayList<DeviceModel> dataList) {
            this.mContext = mActivity;
            this.mLayoutInflater = LayoutInflater.from(mActivity);
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.base_list_item, parent, false);
            }

            DeviceModel DeviceModel = dataList.get(position);

            if (deviceTypeIndex == -2) {
                deviceTypeIndex = DeviceModel.getColumnIndexFromWebRow("设备类型");
                deviceNameIndex = DeviceModel.getColumnIndexFromWebRow("名称");
                deviceNoIndex = DeviceModel.getColumnIndexFromWebRow("编号");
            }

            ((TextView) MmtViewHolder.get(convertView, R.id.itemIndex)).setText((position + 1) + ".");

            String tempStr;
            TextView deviceNo = MmtViewHolder.get(convertView, R.id.desc_top_left);
            deviceNo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            deviceNo.getPaint().setFakeBoldText(true);
            if ((deviceNoIndex < 0) || TextUtils.isEmpty(tempStr = DeviceModel.WebRow.get(deviceNoIndex).FieldValue)) {
                tempStr = "设备编号";
                deviceNo.setTextColor(0xFF808A87);
            } else {
                deviceNo.setTextColor(Color.BLACK);
            }
            deviceNo.setText(tempStr);

            TextView deviceType = MmtViewHolder.get(convertView, R.id.desc_top_right);
            deviceType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            //deviceType.setTextColor(Color.parseColor("#673ab7"));
            if ((deviceTypeIndex == -1) || TextUtils.isEmpty(tempStr = DeviceModel.WebRow.get(deviceTypeIndex).FieldValue)) {
                tempStr = "设备类型";
                deviceType.setTextColor(0xFF808A87);
            } else {
                deviceType.setTextColor(Color.BLACK);
            }
            deviceType.setText(tempStr);

            TextView deviceName = MmtViewHolder.get(convertView, R.id.desc_mid_left);
            deviceName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            deviceName.setTextColor(Color.BLACK);
            if ((deviceNameIndex == -1) || TextUtils.isEmpty(tempStr = DeviceModel.WebRow.get(deviceNameIndex).FieldValue)) {
                tempStr = "设备名称";
                deviceName.setTextColor(0xFF808A87);
            } else {
                deviceName.setTextColor(Color.BLACK);
            }
            deviceName.setText(tempStr);

            MmtViewHolder.get(convertView, R.id.desc_mid_bottom_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.desc_bottom_left).setVisibility(View.GONE);
            MmtViewHolder.get(convertView, R.id.desc_bottom_right).setVisibility(View.GONE);

            return convertView;
        }

        public void onItemClick(int position) {

            DeviceModel deviceModel = dataList.get(position);

            Intent intent = new Intent(mContext, StationDevicePropertyActivity.class);
            intent.putExtra("DeviceName", deviceModel.DeviceName); // "阀门"
            intent.putExtra("DeviceId", deviceModel.ID); // "60"

            mContext.startActivityForResult(intent, Constants.DEFAULT_REQUEST_CODE);
            MyApplication.getInstance().startActivityAnimation(mContext);
        }
    }

}
