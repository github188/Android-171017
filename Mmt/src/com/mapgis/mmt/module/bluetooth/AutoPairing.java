package com.mapgis.mmt.module.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import java.lang.reflect.Method;


/**
 * 蓝牙自动配对的工具类
 * <p>使用方法在onCreate函数: pairing = new AutoPairing(this); pairing.init("0000");</p>
 * <p>在onDestroy函数: pairing.unInit();</p>
 */
public class AutoPairing {

    private boolean autoPairing = true;
    private String pairingCode = "0000";
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private boolean debug = false;

    public AutoPairing(Context context) {
        this.context = context;
    }

    public void init(String pairingCode) {
        this.pairingCode = pairingCode;
        initBroadcast();
    }

    public void unInit() {
        this.pairingCode = "0000";
        uninitBroadcast();
    }

    public boolean setPin(BluetoothDevice btDevice, byte[] pin) {
        try {
            Class<? extends BluetoothDevice> btClass = btDevice.getClass();
            Method setPinRef = btClass.getDeclaredMethod("setPin", byte[].class);
            Boolean ret = (Boolean) setPinRef.invoke(btDevice, pin);
            return ret.booleanValue();

        } catch (Exception e) { // SecurityException | IllegalArgumentException
            e.printStackTrace();
        }
        return false;
    }

    private void initBroadcast() {

        if (null == context)
            return;

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
                    if (!autoPairing)
                        return;

                    boolean ret = setPin(device, pairingCode.getBytes());
                    try {
                        ClsUtils.createBond(device);
                        if (android.os.Build.MANUFACTURER.indexOf("Lenovo") != -1) {
                            ClsUtils.cancelPairingUserInput(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    DBG("setPin: " + ret);
                }
            }

        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void uninitBroadcast() {

        if (null == context)
            return;

        if (broadcastReceiver != null)
            context.unregisterReceiver(broadcastReceiver);
    }

    private void DBG(String msg) {
        if (!debug)
            return;
        if (null == context)
            return;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}

