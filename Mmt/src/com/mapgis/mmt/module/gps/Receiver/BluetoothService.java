package com.mapgis.mmt.module.gps.Receiver;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.module.gis.MapGISFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Comclay on 2017/2/24.
 * 用于选择蓝牙设备的线程
 */

public class BluetoothService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);
        // 开启连接蓝牙的线程
        new ConnectThread().start();
        return i;
    }

    Handler btSelectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final BluetoothDevice[] devices = (BluetoothDevice[]) msg.obj;
            if (devices.length == 1){
                startMainService(devices[0]);
                return;
            }

            List<String> deviceNames = new ArrayList<>();
            for (BluetoothDevice bluetoothDevice : devices) {
                deviceNames.add(bluetoothDevice.getName());
            }

            ListDialogFragment listDialogFragment = new ListDialogFragment("选择蓝牙设备", deviceNames);
            listDialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    startMainService(devices[arg2]);
                }
            });
            listDialogFragment.setCancelable(false);
            BaseActivity baseActivity = (BaseActivity) AppManager.currentActivity();
            if (baseActivity != null) {
                listDialogFragment.show(baseActivity.getSupportFragmentManager(), "");
            }
        }
    };

    private void startMainService(BluetoothDevice device) {
        MyApplication.getInstance().putConfigValue("BluetoothAddress", device.getAddress());
        MyApplication.getInstance().Start((MapGISFrame) AppManager.getActivity(MapGISFrame.class));
        stopSelf();
    }

    // 4.连接蓝牙线程类
    private class ConnectThread extends Thread {
        @Override
        public void run() {
            BluetoothAdapter adapter = null;
            try {
                // 1.// 获取蓝牙适配器
                adapter = BluetoothAdapter.getDefaultAdapter();
                // 打开蓝牙,不做提示，强行打开
                if (!adapter.isEnabled()) {
                    MyApplication.getInstance().showMessageWithHandle("正在打开蓝牙");
                    adapter.enable();
                    Thread.sleep(500);
                }

                for (int i = 0; i < 60; i++) {
                    // 2. 获取蓝牙MacAddress,搜索并获取已经配对蓝牙
                    Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
                    BluetoothDevice[] devices = deviceSet.toArray(new BluetoothDevice[deviceSet.size()]);

                    if (devices.length < 1) {
                        MyApplication.getInstance().showMessageWithHandle("不存在已配对的蓝牙外设，10秒后将重试");
                        Thread.sleep(10 * 1000);
                    } else {

                        Activity activity;
                        while (true) {
                            synchronized (this) {
                                activity = AppManager.currentActivity();
                                if (activity == null || !activity.getClass().getSimpleName()
                                        .equals(MapGISFrame.class.getSimpleName())){
                                    break;
                                }
                            }
                            Thread.sleep(10);
                        }

                        Message message = btSelectHandler.obtainMessage();
                        message.obj = devices;
                        message.what = 1002;
                        btSelectHandler.sendMessage(message);
                        return;
                    }
                }
                MyApplication.getInstance().showMessageWithHandle("不存在已配对的蓝牙外设，请配置好蓝牙后再重新登录");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (adapter != null) {
                    adapter.cancelDiscovery();
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
