package com.customform.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.EventReportCache;
import com.mapgis.mmt.module.bluetooth.AutoPairing;
import com.mapgis.mmt.module.bluetooth.BluetoothViewerService;
import com.mapgis.mmt.module.bluetooth.CHexConver;
import com.mapgis.mmt.module.bluetooth.DeviceListActivity;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.RealTimeCharDialogFragment;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 浓度
 * Created by zoro at 2017/9/1.
 */
class MmtConcentrationView extends MmtBaseView {

    private static final int REQUEST_ENABLE_BT = 11;
    private static final int REQUEST_CONNECT_DEVICE = 22;

    private FlowBeanFragment flowBeanFragment;

    MmtConcentrationView(Context context, GDControl control) {
        super(context, control);
        this.flowBeanFragment = getBeanFragment();
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_radiobutton;
    }

    public View build() {
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        view.getButton().setImageResource(R.drawable.ic_autorenew);

        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.setValue(control.Value.length() != 0 ? control.Value : control.DefaultValues);

        // "浓度"控件，对接蓝牙
        if (!control.isReadOnly()) {
            initBluetooth(view);
        }

        return view;
    }

    //region Bluetooth 汉威电子检漏仪

    /**
     * Member object for the services
     */
    private BluetoothViewerService mBluetoothService = null;
    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    // State variables
    private volatile boolean paused = false;

    private boolean connected = false;
    private AutoPairing pairing = null;

    // The Handler that gets information back from the BluetoothService
    private Handler mHandler;
    private ImageButtonView concentrationView;

    private Matcher concentrationMatcher;
    private RealTimeCharDialogFragment concentrationFragment;
    private String concentrationUnit;

    private void initBluetooth(View formView) {

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (formView instanceof ImageButtonView) {
            this.concentrationView = (ImageButtonView) formView;
            concentrationView.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (flowBeanFragment != null) {
                            flowBeanFragment.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                        }
                    } else {
                        // Otherwise, setup the chat session
                        setupConnect(false);
                    }
                }
            });
        }
    }

    private void showConcentrationRealtimeChart() {
        concentrationFragment = RealTimeCharDialogFragment.newInstance();

        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
            if (fragment instanceof FlowBeanFragment) {
                concentrationFragment.show(fragment.getChildFragmentManager(), RealTimeCharDialogFragment.TAG);
                break;
            }
        }
        concentrationFragment.setOnConfirmListener(new RealTimeCharDialogFragment.OnConfirmListener() {
            @Override
            public void onConfirmClicked(float maxValue, float minValue) {
                if (concentrationView != null) {
                    concentrationView.setValue(maxValue + " " + concentrationUnit);
                }
            }
        });
    }

    private void setupConnect(boolean isInitial) {

        if (mBluetoothService == null || isInitial) {
            this.pairing = new AutoPairing(context);
            this.pairing.init("1234");

            connected = false;
            // Initialize UI handler
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Activity host = getActivity();
                    if (host == null || host.isFinishing()) {
                        return;
                    }
                    switch (msg.what) {
                        case BluetoothViewerService.MSG_CONNECTED:
                            showTipMessage("蓝牙连接配对成功");
                            connected = true;
                            refreshUnit();
                            BluetoothDevice bluetoothDevice = (BluetoothDevice) msg.obj;
                            EventReportCache connCache = new EventReportCache(0, "PairedBluetoothDevice", bluetoothDevice.getAddress());
                            connCache.insert();
                            showConcentrationRealtimeChart();
                            break;
                        case BluetoothViewerService.MSG_CONNECTING:
                            showTipMessage("蓝牙连接配对中...");
                            connected = false;
                            break;
                        case BluetoothViewerService.MSG_NOT_CONNECTED:
                            connected = false;
                            break;
                        case BluetoothViewerService.MSG_CONNECTION_FAILED:
                            showTipMessage("蓝牙连接配对失败");
                            connected = false;
                            retryChoice();
                            break;
                        case BluetoothViewerService.MSG_CONNECTION_FAILED_WITH_RETRY:
                            showTipMessage("蓝牙连接配对失败，3秒后重新尝试连接......");
                            connected = false;
                            break;
                        case BluetoothViewerService.MSG_CONNECTION_LOST:
                            connected = false;
                            break;
                        case BluetoothViewerService.MSG_LINE_READ:
                            // 发送获取单位的命令
                            if (!CHexConver.isUnit) {
                                refreshUnit();
                            }

                            final String newMessage = (String) msg.obj;
                            if (concentrationView != null) {
                                if (concentrationMatcher == null) {
                                    concentrationMatcher = Pattern.compile("(\\d)+(\\.)?(\\d)*").matcher("");
                                }
                                float newValue = 0;
                                try {
                                    concentrationMatcher.reset(newMessage);
                                    if (concentrationMatcher.find()) {
                                        String newValueStr = concentrationMatcher.group();
                                        newValue = Float.parseFloat(newValueStr);
                                        if (TextUtils.isEmpty(concentrationUnit)) {
                                            concentrationUnit = newMessage.replace(newValueStr, "");
                                        }
                                    }
                                    if (concentrationFragment != null) {
                                        concentrationFragment.addEntry(newValue);
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                    }
                }
            };

            // Initialize the BluetoothChatService to perform bluetooth connections
            mBluetoothService = new BluetoothViewerService(mHandler);
        }

        if (!connected) {
            List<EventReportCache> pairedDeviceList = DatabaseHelper.getInstance().query(EventReportCache.class, "userId=0 and key='PairedBluetoothDevice'");

            if (pairedDeviceList.size() > 0) {
                EventReportCache pairedDevice = pairedDeviceList.get(0);
                Intent data = new Intent();
                data.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, pairedDevice.getValue());
                onActivityResult(REQUEST_CONNECT_DEVICE, Activity.RESULT_OK, data);
            } else {
                // connect device
                startDeviceListActivity();
            }

        } else {
            // send command
            sendMessageBackground("A1 00");
            showConcentrationRealtimeChart();
        }
    }

    private void retryChoice() {

        new AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("提示")
                .setMessage("蓝牙连接配对失败，是否继续重试？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHelper.getInstance().delete(EventReportCache.class, "userId=0 and key='PairedBluetoothDevice'");
                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setupConnect(false);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void startDeviceListActivity() {
        if (flowBeanFragment != null) {
            Intent serverIntent = new Intent(context, DeviceListActivity.class);
            flowBeanFragment.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    private void showTipMessage(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // 获取单位
    private void refreshUnit() {
        sendMessageBackground("A2 00");
    }

    private void sendMessageBackground(final CharSequence chars) {
        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {
                sendMessage(chars);
            }
        });
    }

    private void sendMessage(CharSequence chars) {
        if (chars.length() > 0) {
            if (mBluetoothService.getState() != BluetoothViewerService.STATE_CONNECTED) {
                Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                return;
            }

            String temp = "A5 5A 01 02 03 04 05 06 07 08 09 0A 0B 0C "; //命令头
            String message = chars.toString(); //命令
            String jym = CHexConver.GetAndBytes(CHexConver.hex2byte(temp + message)); //校验码

            try {
                mBluetoothService.write(CHexConver.hex2byte(temp + message + jym + "\n"));
                paused = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConnect(true);
                }
                return true;
            }
            case REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothService.connect(device);
                }
                return true;
            }
        }
        return false;
    }
    //endregion

    @Override
    public void onDestroy() {
        if (mBluetoothService != null) {
            mBluetoothService.stop();
            mBluetoothService = null;
            mHandler.removeCallbacksAndMessages(null);
            pairing.unInit();
        }
    }
}
