package com.mapgis.mmt.module.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BuildConfig;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.SwipListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.IPPortBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/6/20.
 */
@SuppressLint("ShowToast")
public class SystemSetterFragment extends Fragment {
    protected View view;
    protected List<IPPortBean> ips = new ArrayList<IPPortBean>();
    protected List<String> serviceAddress = new ArrayList<String>();
    protected SwipListDialogFragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.setting, null);
        checkNetworkInfo(view);

        ((EditText) view.findViewById(R.id.txtServer))
                .setText(ServerConnectConfig.getInstance()
                        .getServerConfigInfo().IpAddress);
        ((EditText) view.findViewById(R.id.txtPort))
                .setText(ServerConnectConfig.getInstance()
                        .getServerConfigInfo().Port);
        ((EditText) view.findViewById(R.id.txtVirtualPath))
                .setText(ServerConnectConfig.getInstance()
                        .getServerConfigInfo().VirtualPath);

        view.findViewById(R.id.btnNetTest).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        boolean isSaveSuccess = saveServerSetter();
                        if (!isSaveSuccess) {
                            return;
                        }
                        new NetTestTask(SystemSetterFragment.this)
                                .mmtExecute(ServerConnectConfig.getInstance().getHttpProtocol(),
                                        ((EditText) view.findViewById(R.id.txtServer)).getText().toString().replaceAll("\\s*",""),
                                        ((EditText) view.findViewById(R.id.txtPort)).getText().toString().replaceAll("\\s*",""),
                                        ((EditText) view.findViewById(R.id.txtVirtualPath)).getText().toString().replaceAll("\\s*",""));
                    }
                });

        view.findViewById(R.id.btnSave).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String server = ((EditText) view
                                .findViewById(R.id.txtServer)).getText()
                                .toString().replaceAll("\\s*","");
                        String port = ((EditText) view
                                .findViewById(R.id.txtPort)).getText()
                                .toString().replaceAll("\\s*","");
                        String virtualPath = ((EditText) view
                                .findViewById(R.id.txtVirtualPath)).getText()
                                .toString().replaceAll("\\s*","");

                        try {
                            saveServerSetter(server, port, virtualPath);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        view.findViewById(R.id.spinnerImg)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.show(getActivity()
                                .getSupportFragmentManager(), "");

                    }
                });

        initIPChose(view);

        final TextView txtGPS = (TextView) view.findViewById(R.id.txtGPS);

        String key = MyApplication.getInstance().getSystemSharedPreferences().getString("GpsReceiver", "");

        switch (key) {
            case "BD":
                txtGPS.setText("综合定位");
                break;
            case "Native":
                txtGPS.setText("卫星定位");
                break;
            case "HC":
                txtGPS.setText("华测定位");
                break;
            case "NC":
                txtGPS.setText("南方定位");
                break;
            case "BT":
                txtGPS.setText("蓝牙定位");
                break;
            case "GD":
                txtGPS.setText("高德定位");
                break;
            case "RD":
                txtGPS.setText("内测定位");
                break;
            case "NMEA":
            case "HZ":
                txtGPS.setText("合众思壮定位");
                break;
            case "ZHD":
                txtGPS.setText("中海达定位");
                break;
        }
        final List<String> items = new ArrayList<String>() {{
            add("默认配置");
            add("综合定位");
            add("卫星定位");
            add("华测定位");
            add("南方定位");
            add("蓝牙定位");
            add("高德定位");
            add("内测定位");
            add("合众思壮定位");
            add("中海达定位");
        }};
        view.findViewById(R.id.ivGPSMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //boolean show_test_location = "1".equals(MyApplication.getInstance().getString(R.string.show_test_location));
                if (BuildConfig.isRelease ==1){
                    items.remove("内测定位");
                }
//                if (!show_test_location) {
//                    items.remove("内测定位");
//                }
                ListDialogFragment fragment = new ListDialogFragment("定位方式", items);

                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        String key = "";

                        switch (value) {
                            case "综合定位":
                                key = "BD";
                                break;
                            case "卫星定位":
                                key = "Native";
                                break;
                            case "华测定位":
                                key = "HC";
                                break;
                            case "南方定位":
                                key = "NC";
                                break;
                            case "蓝牙定位":
                                key = "BT";
                                break;
                            case "高德定位":
                                key = "GD";
                                break;
                            case "内测定位":
                                key = "RD";
                                break;
                            case "合众思壮定位":
                                key = "HZ";
                                break;
                            case "中海达定位":
                                key = "ZHD";
                                break;
                        }

                        MyApplication.getInstance().putConfigValue("GpsReceiver", key);
                        txtGPS.setText(value);

                        MyApplication.getInstance().getSystemSharedPreferences().edit().putString("GpsReceiver", key).apply();
                    }
                });

                fragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        return view;
    }

    public void initIPChose(final View view) {
        ips.clear();
        ips = IPPortBean.query();
        if (ips != null && ips.size() > 1) {
            view.findViewById(R.id.spinnerImg).setVisibility(View.VISIBLE);
            serviceAddress.clear();
            for (IPPortBean ipPortBean : ips) {
                serviceAddress.add(ipPortBean.getIp() + ":" + ipPortBean.getPort());
            }
            fragment = new SwipListDialogFragment(
                    "选择服务地址", serviceAddress);
            fragment.setOnListItemClickListener(new SwipListDialogFragment.OnListItemClickListener() {
                @Override
                public void setOnListItemClick(int position, String value) {
                    ((EditText) view.findViewById(R.id.txtServer))
                            .setText(ips.get(position).getIp());
                    ((EditText) view.findViewById(R.id.txtPort))
                            .setText(ips.get(position).getPort());
                    ((EditText) view.findViewById(R.id.txtVirtualPath))
                            .setText(ips.get(position).getVirtualPath());
                }
            });
            fragment.setOnListItemDeleteClickListener(new SwipListDialogFragment.OnListItemDeleteClickListener() {
                @Override
                public void setOnListItemDeleteClick(int position) {
                    IPPortBean.delete(ips.get(position));
                    ips.remove(position);

                    //删除的数据如果和IP地址栏填的一样需要更新IP地址栏数据，不然又会插入该数据
                    //这里不管怎样都用新数据填充编辑栏
                    if (ips.size() > 0) {
                        ((EditText) view.findViewById(R.id.txtServer))
                                .setText(ips.get(0).getIp());
                        ((EditText) view.findViewById(R.id.txtPort))
                                .setText(ips.get(0).getPort());
                        ((EditText) view.findViewById(R.id.txtVirtualPath))
                                .setText(ips.get(0).getVirtualPath());
                    }
                    initIPChose(view);
                    //  serviceAddress已删除position的数据
                    //  serviceAddress.remove(position);
                }
            });
        } else {
            view.findViewById(R.id.spinnerImg).setVisibility(View.GONE);
        }
    }

    /**
     * 存储网络连接参数，若文件不存在，则自动创建路径及文件，再写入参数
     * 改为存到本地DB
     */
    public void saveServerSetter(String ip, String port, String virtualPath)
            throws IOException {
        if (BaseClassUtil.isNullOrEmptyString(ip) || BaseClassUtil.isNullOrEmptyString(port) || BaseClassUtil.isNullOrEmptyString(virtualPath)) {
            return;
        }
        long ret = new IPPortBean(ip, port, virtualPath).insert();
        if (ret < 0) {
            Toast.makeText(getActivity(), "保存失败",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        initIPChose(view);

        ServerConnectConfig.getInstance().loadLoginInfo(this.getActivity(), true);
        Toast.makeText(getActivity(), "保存成功",
                Toast.LENGTH_SHORT).show();
    }

    public boolean saveServerSetter() {
        try {
            String ip = ((EditText) view
                    .findViewById(R.id.txtServer)).getText()
                    .toString().replaceAll("\\s*","");
            String port = ((EditText) view
                    .findViewById(R.id.txtPort)).getText()
                    .toString().replaceAll("\\s*","");
            String virtualPath = ((EditText) view
                    .findViewById(R.id.txtVirtualPath)).getText()
                    .toString().replaceAll("\\s*","");

            if (BaseClassUtil.isNullOrEmptyString(ip)) {
                Toast.makeText(getActivity(), "IP地址不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (BaseClassUtil.isNullOrEmptyString(port)) {
                Toast.makeText(getActivity(), "端口不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (BaseClassUtil.isNullOrEmptyString(virtualPath)) {
                Toast.makeText(getActivity(), "虚拟目录不能为空", Toast.LENGTH_SHORT).show();
                return false;
            }
            long ret = new IPPortBean(ip, port, virtualPath).insert();
            if (ret < 0) {
                return false;
            }
            initIPChose(view);

            ServerConnectConfig.getInstance().loadLoginInfo(this.getActivity(), true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void checkNetworkInfo(View view) {
        ConnectivityManager conMan = (ConnectivityManager) getActivity().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // mobile 3G Data Network
        NetworkInfo mobileNetworkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean is3GOpen = mobileNetworkInfo != null && "CONNECTED".equalsIgnoreCase(mobileNetworkInfo.getState().toString());
        ((TextView) view.findViewById(R.id.txt3G))
                .setText(is3GOpen ? "已开启" : "未开启");

        NetworkInfo wifiNetworkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiOpen = wifiNetworkInfo != null && "CONNECTED".equalsIgnoreCase(wifiNetworkInfo.getState().toString());
        ((TextView) view.findViewById(R.id.txtWifi))
                .setText(isWifiOpen ? "已开启" : "未开启");
    }
}