/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mapgis.mmt.module.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("NewApi")
public class BluetoothViewerService {

    private static final String TAG = BluetoothViewerService.class.getSimpleName();
    private static final boolean D = true;

    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MSG_NOT_CONNECTED = 30;
    public static final int MSG_CONNECTING = 31;
    public static final int MSG_CONNECTED = 32;
    public static final int MSG_CONNECTION_FAILED = 33;
    public static final int MSG_CONNECTION_FAILED_WITH_RETRY = 34;
    public static final int MSG_CONNECTION_LOST = 35;
    public static final int MSG_LINE_READ = 21;
    public static final int MSG_BYTES_WRITTEN = 22;

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;


    // Name for the SDP record when creating server socket
    private static final String NAME = "OBDDTC";

    // Unique UUID for this application    									 				
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // The retry times when connecting to a bluetooth device.
    private static final int CONNECT_RETRY_TIMES = 3;

    /**
     * Prepare a new Bluetooth session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothViewerService(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        try {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
            sendMessage(MSG_CONNECTING, device);

        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
        sendMessage(MSG_CONNECTED, device);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.shutdown();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
        sendMessage(MSG_NOT_CONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    private void sendLineRead(String line) {
        mHandler.obtainMessage(MSG_LINE_READ, -1, -1, line).sendToTarget();
    }

    private void sendBytesWritten(byte[] bytes) {
        mHandler.obtainMessage(MSG_BYTES_WRITTEN, -1, -1, bytes).sendToTarget();
    }

    private void sendMessage(int messageId, BluetoothDevice device) {
        mHandler.obtainMessage(messageId, -1, -1, device).sendToTarget();
    }

    private void sendMessage(int messageId) {
        mHandler.obtainMessage(messageId).sendToTarget();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        sendMessage(MSG_CONNECTION_FAILED);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);
        sendMessage(MSG_CONNECTION_LOST);
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private volatile boolean canceled;

        public ConnectThread(BluetoothDevice device) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            mmDevice = device;
            BluetoothSocket tmp = null;
            canceled = false;

            Log.i(TAG, "calling device.createRfcommSocket with channel 1 ...");
            try {
                // call hidden method, see BluetoothDevice source code for more details:
                // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/bluetooth/BluetoothDevice.java

                if (Build.MANUFACTURER.contains("Lenovo")) {
                    Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                    tmp = (BluetoothSocket) m.invoke(device, 1);  // channel = 1
                    Log.i(TAG, "setting socket to result of 反射");
                } else {
                    int sdk = Integer.parseInt(Build.VERSION.SDK);
                    if (sdk >= 14) {
                        tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                        Log.i(TAG, "setting socket to result of createRfcommSocket");
                    } else {
                        Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                        tmp = (BluetoothSocket) m.invoke(device, 1);  // channel = 1
                        Log.i(TAG, "setting socket to result of 反射");
                    }
                }
                //联想手机使用如下代码连接蓝牙
                //Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{ int.class });
                //tmp = (BluetoothSocket) m.invoke(device, 1);  // channel = 1
                //联想以为手机使用如下代码连接蓝牙

//            	Log.i(TAG, "系统版本："+sdk);
////				if (sdk >= 14) {
////					tmp = device.createRfcommSocketToServiceRecord(UUID_COM);
////					Log.i(TAG, "setting socket to result of createRfcommSocket");
////				} else {
////					Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{ int.class });
////	                tmp = (BluetoothSocket) m.invoke(device, 1);  // channel = 1
////	                Log.i(TAG, "setting socket to result of 反射");
////				}
//            	if(sdk >10){
//            		//sdk 2.3以上需要用此方法连接，否则连接不上，会报 java.io.IOException: Connection refused 异常
//            		tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_COM);
//            		
//            	}else if(sdk ==10){
//                  Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{ int.class });
//                  tmp = (BluetoothSocket) m.invoke(device, 1);
//            	}else {
//            		tmp = device.createRfcommSocketToServiceRecord(UUID_COM);
//            	}
            } catch (Exception e) {
                try {
                    int sdk = Build.VERSION.SDK_INT;
                    if (sdk >= 10) {
                        tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    } else {
                        tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "create() failed", ex);
                }
                Log.e(TAG, e.getMessage(), e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket with failure retry (interval 3 seconds)
            int retryTimes = 0;
            while (retryTimes < CONNECT_RETRY_TIMES) {
                retryTimes++;
                try {
                    if (canceled || mmSocket.isConnected()) {
                        break;
                    }
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    sendMessage(MSG_CONNECTION_FAILED_WITH_RETRY);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e1) {}
                }
            }

            if (!mmSocket.isConnected()) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothViewerService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                canceled = true;
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        private boolean stop = false;
        private boolean hasReadAnything = false;

        public void shutdown() {
            stop = true;
            if (!hasReadAnything) return;
            if (mmInStream != null) {
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of InputStream failed.");
                }
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            int size = 0;
            List<String> dataArray = new ArrayList<String>();
            String manager;

            while (!stop) {
                try {
                    //之所以出现数据分段接收的问题，是因为读取socket的时候，发送端的数据还没有完全加入输入数据流，这边就开始读取了。
                    //解决办法：在读取输入流之前，让读取线程休眠一段时间再读取，这样所发送的数据就已经完整地加入了输入流。
                    sleep(10l);

                    if (mmSocket != null) {
                        size = mmInStream.available();
                        manager = "";
                    }

                    byte[] buf_data = new byte[size];
                    size = mmInStream.read(buf_data, 0, size);

                    for (int i = 0; i < buf_data.length; ++i) {

                        manager = Integer.toHexString(buf_data[i] & 0xFF).toUpperCase();
                        if (manager.length() == 1) {//补零
                            manager = "0" + manager;
                        }

                        if ((manager.equals("A5")) && (dataArray.size() != 0)) {
                            String result = "";
                            for (int j = 0; j < dataArray.size(); ++j) {
                                result = result + dataArray.get(j) + " ";
                            }

                            if (D) Log.i("接收数据：", result);
                            // sendLineRead("接收数据：" + result);

                            // result = CHexConver.bxTran(result);
                            // TODO: 2/22/17 Temporay code
                            if (!CHexConver.isUnit) {
                                result = CHexConver.bxTran(result);
                            } else {
                                result = CHexConver.bxTranConcentration(result);
                                if (!TextUtils.isEmpty(result)) {
                                    sendLineRead(result);
                                }
                            }

                            dataArray.clear();
                        }
                        dataArray.add(manager);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param bytes The bytes to write
         */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                sendBytesWritten(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
