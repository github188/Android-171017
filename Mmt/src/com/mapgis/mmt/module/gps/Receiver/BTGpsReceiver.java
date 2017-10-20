package com.mapgis.mmt.module.gps.Receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.AppLogger;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.util.BTNmeaUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * 蓝牙GPS接收器定位，如合众思壮MG20外设蓝牙定位
 */
public class BTGpsReceiver extends GpsReceiver {
    private static final String TAG = "BTGpsReceiver";

    private BluetoothSocket mySocket;

    @Override
    public String start(CoordinateConvertor coordinateConvertor) {
        try {
            this.coordinateConvertor = coordinateConvertor;

            new ConnectThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 4.连接蓝牙线程类
    private class ConnectThread extends Thread {
        @Override
        public void run() {
            BluetoothAdapter adapter = null;
            try {
                DatabaseHelper.getInstance().insert(new AppLogger("准备连接蓝牙设备"));
                // 1.// 获取蓝牙适配器
                adapter = BluetoothAdapter.getDefaultAdapter();
                // 打开蓝牙,不做提示，强行打开
                if (!adapter.isEnabled()) {
                    adapter.enable();
                }

                for (int i = 0; i < 60; i++) {
                    openSocket(adapter);
                    if (mySocket != null) {
                        break;
                    } else {
                        MyApplication.getInstance().showMessageWithHandle("蓝牙打开失败，3秒后重新尝试......");
                        Thread.sleep(3 * 1000);
                    }
                }

                if (mySocket == null) {
                    MyApplication.getInstance().showMessageWithHandle("蓝牙打开失败，请尝试手动打开蓝牙并且重新登录程序");
                    return;
                }
                DatabaseHelper.getInstance().insert(new AppLogger("蓝牙打开成功"));

                for (int i = 0; i < 60; i++) {
                    try {
                        if (mySocket.isConnected()) {
                            // 启动接收远程设备发送过来的数据
                            new ReceiveDatas().start();
                            break;
                        } else {
                            mySocket.connect();
                        }
                    }catch(IOException ex){
                        ex.printStackTrace();
                        // socket关闭
                        openSocket(adapter);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        MyApplication.getInstance().showMessageWithHandle("蓝牙连接失败，3秒后重新尝试连接......");
                        Thread.sleep(3 * 1000);
                    }
                }

                if (!mySocket.isConnected()) {
                    MyApplication.getInstance().showMessageWithHandle("蓝牙连接失败，请尝试重启GPS外接设备并且重新登录程序");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (mySocket != null)
                        mySocket.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }finally {
                // 关闭资源
                if (adapter != null){
                    adapter.cancelDiscovery();
                }
            }
        }

        private void openSocket(BluetoothAdapter adapter) throws IOException {
            // 2. 获取蓝牙MacAddress,搜索并获取已经配对蓝牙
            String address = MyApplication.getInstance().getConfigValue("BluetoothAddress");

            if (TextUtils.isEmpty(address)) {
                return;
            }
            BluetoothDevice device = adapter.getRemoteDevice(address);
            int sdk = Build.VERSION.SDK_INT;
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            if (sdk >= 10) {
                mySocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } else {
                mySocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
        }
    }

    // 5.读取数据类
    private class ReceiveDatas extends Thread {
        @Override
        public void run() {
            InputStream mmInStream = null;
            BufferedReader reader = null;

            try {
                mmInStream = mySocket.getInputStream();

                MyApplication.getInstance().showMessageWithHandle("外接蓝牙GPS设备已经连接成功");
                DatabaseHelper.getInstance().insert(new AppLogger("外接蓝牙GPS设备已经连接成功"));
                Log.e(TAG, "外接蓝牙GPS设备已经连接成功");

                BTNmeaUtils btNmeaUtils = new BTNmeaUtils();
                // 监听输入流
                while (mySocket.isConnected()) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(mmInStream, "UTF-8")); // 实例化输入流，并获取网页代码
                        String line; // 依次循环，至到读的值为空
                        while ((line = reader.readLine()) != null) {
                            AppLogger logger = new AppLogger();
                            try {
                                logger.content = TextUtils.isEmpty(line) ? "" : line;
                                DatabaseHelper.getInstance().insert(logger);
                                btNmeaUtils.handleNmea(line);
                            } catch (Exception ex) {
                                logger.content = "Exception==>" + (TextUtils.isEmpty(ex.getMessage()) ? "" : ex.getMessage());
                                DatabaseHelper.getInstance().insert(logger);
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (reader != null){
                            reader.close();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (mmInStream != null) {
                        mmInStream.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void stop() {
        try {
            mySocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
