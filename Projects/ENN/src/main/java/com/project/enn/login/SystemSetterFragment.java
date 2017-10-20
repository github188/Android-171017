package com.project.enn.login;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.SwipListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.IPPortBean;
import com.mapgis.mmt.module.login.NetTestTask;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.project.enn.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ShowToast")
public class SystemSetterFragment extends Fragment implements View.OnClickListener {

    private View view;
    private List<IPPortBean> ips = new ArrayList<>();
    private List<String> serviceAddress = new ArrayList<>();
    private SwipListDialogFragment ipFragment;

    private EditText txtServer, txtPort, txtVirtualPath;
    private TextView tvProtocol;
    private RadioButton btnUnifyLogin, btnNormalLogin;

    private HashMap<String, String> httpProtocols = new HashMap<>();

    {
        httpProtocols.put("普通", "http");
        httpProtocols.put("加密", "https");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.setting_enn, container, false);

        txtServer = (EditText) view.findViewById(R.id.txtServer);
        txtPort = (EditText) view.findViewById(R.id.txtPort);
        txtVirtualPath = (EditText) view.findViewById(R.id.txtVirtualPath);
        tvProtocol = (TextView) view.findViewById(R.id.txtHttpProtocol);

        ServerConfigInfo info = ServerConnectConfig.getInstance().getServerConfigInfo();

        btnUnifyLogin = (RadioButton) view.findViewById(R.id.btnUnifyLogin);
        btnNormalLogin = (RadioButton) view.findViewById(R.id.btnNormalLogin);

        btnUnifyLogin.setOnClickListener(this);
        btnNormalLogin.setOnClickListener(this);

        String key = MyApplication.getInstance().getSystemSharedPreferences().getString("LoginStyle", "Unify");

        if (key.equals("Unify")) {
            btnUnifyLogin.setChecked(true);

            info.IpAddress = getString(R.string.login_server_ip);
            info.Port = getString(R.string.login_server_port);
            info.VirtualPath = getString(R.string.login_server_virtual_path);

            btnUnifyLogin.performClick();

        } else {
            btnNormalLogin.setChecked(true);
            btnNormalLogin.performClick();
        }

        txtServer.setText(info.IpAddress);
        txtPort.setText(info.Port);
        txtVirtualPath.setText(info.VirtualPath);

        view.findViewById(R.id.btnNetTest).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new NetTestTask(SystemSetterFragment.this).mmtExecute(
                                httpProtocols.get(tvProtocol.getText().toString()),
                                txtServer.getText().toString(), txtPort.getText().toString(),
                                txtVirtualPath.getText().toString());
                    }
                });

        view.findViewById(R.id.btnSave).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        saveServerSetter();
                    }
                });

        view.findViewById(R.id.spinnerImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ipFragment != null) {
                    ipFragment.show(getActivity().getSupportFragmentManager(), "");
                } else {
                    Toast.makeText(getActivity(), "无可选项", Toast.LENGTH_SHORT).show();
                }
            }
        });

        view.findViewById(R.id.ivProtocolMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                ListDialogFragment fragment = new ListDialogFragment("传输协议",
                        httpProtocols.keySet().toArray(new String[httpProtocols.size()]));
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {

                        tvProtocol.setText(value);
                    }
                });
                fragment.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        initIPChose(view);

        choseGPS();

        return view;
    }

    @Override
    public void onClick(View v) {

        // 统一登陆，ip/port均读取资源文件中的固定值，返回时只记统一登陆状态，不保存值
        if (v.getId() == R.id.btnUnifyLogin) {
            txtServer.setText(getString(R.string.login_server_ip));
            txtPort.setText(getString(R.string.login_server_port));
            txtVirtualPath.setText(getString(R.string.login_server_virtual_path));

            txtServer.setEnabled(false);
            txtPort.setEnabled(false);
            txtVirtualPath.setEnabled(false);

            view.findViewById(R.id.spinnerImg).setVisibility(View.GONE);
            view.findViewById(R.id.ivProtocolMore).setVisibility(View.GONE);

            String loginUrl = getString(R.string.login_url);
            if (!TextUtils.isEmpty(loginUrl) && loginUrl.startsWith("https")) {
                tvProtocol.setText("加密");
            } else {
                tvProtocol.setText("普通");
            }

        } else if (v.getId() == R.id.btnNormalLogin) {
            ServerConfigInfo info = ServerConnectConfig.getInstance().getServerConfigInfo();

            txtServer.setText(info.IpAddress);
            txtPort.setText(info.Port);
            txtVirtualPath.setText(info.VirtualPath);

            txtServer.setEnabled(true);
            txtPort.setEnabled(true);
            txtVirtualPath.setEnabled(true);

            String protocol = MyApplication.getInstance().getSystemSharedPreferences().getString("HttpProtocol", "");
            String protocolDesc = "https".equalsIgnoreCase(protocol) ? "加密" : "普通";
            tvProtocol.setText(protocolDesc);

            view.findViewById(R.id.spinnerImg).setVisibility(View.VISIBLE);
            view.findViewById(R.id.ivProtocolMore).setVisibility(View.VISIBLE);
        }
    }

    private void choseGPS() {
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

        view.findViewById(R.id.ivGPSMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment("定位方式", new String[]{"默认配置", "综合定位", "卫星定位", "华测定位", "南方定位", "蓝牙定位", "高德定位", "内测定位", "合众思壮定位", "中海达定位"});

                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        String key = "";

                        switch (arg2) {
                            case 1:
                                key = "BD";
                                break;
                            case 2:
                                key = "Native";
                                break;
                            case 3:
                                key = "HC";
                                break;
                            case 4:
                                key = "NC";
                                break;
                            case 5:
                                key = "BT";
                                break;
                            case 6:
                                key = "GD";
                                break;
                            case 7:
                                key = "RD";
                                break;
                            case 8:
                                key = "HZ";
                                break;
                            case 9:
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
    }

    public void initIPChose(final View view) {
        ips.clear();

        ips = IPPortBean.query();

        if (ips != null && ips.size() > 1) {
            serviceAddress.clear();

            for (IPPortBean ipPortBean : ips) {
                serviceAddress.add(ipPortBean.getIp() + ":" + ipPortBean.getPort());
            }

            ipFragment = new SwipListDialogFragment("选择服务地址", serviceAddress);

            ipFragment.setOnListItemClickListener(new SwipListDialogFragment.OnListItemClickListener() {
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

            ipFragment.setOnListItemDeleteClickListener(new SwipListDialogFragment.OnListItemDeleteClickListener() {
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
                }
            });
        }
    }

    /**
     * 存储网络连接参数，若文件不存在，则自动创建路径及文件，再写入参数
     * 改为存到本地DB
     */
    public void saveServerSetter() {
        try {
            String ip = txtServer.getText().toString();
            String port = txtPort.getText().toString();
            String virtualPath = txtVirtualPath.getText().toString();

            if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) || TextUtils.isEmpty(virtualPath)) {
                return;
            }

            long ret = new IPPortBean(ip, port, virtualPath).insert();

            if (ret < 0) {
                Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                return;
            }

            initIPChose(view);

            SharedPreferences.Editor editor = MyApplication.getInstance().getSystemSharedPreferences().edit();
            String style = btnUnifyLogin.isChecked() ? "Unify" : "Normal";
            editor.putString("LoginStyle", style);

            if (!"Unify".equals(style)) {
                String selectedProtocol = httpProtocols.get(tvProtocol.getText().toString());
                editor.putString("HttpProtocol", selectedProtocol);
            }
            editor.apply();

            ServerConnectConfig.getInstance().loadLoginInfo(getActivity(), true);

            Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
