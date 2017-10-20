package com.mapgis.mmt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mapgis.mmt.engine.MemoryManager;

/**
 * 监听内存变化的广播接受者
 */
@Deprecated
public class DeviceStorageReceiver extends BroadcastReceiver {
    public DeviceStorageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(intent.getAction())){
            // 内存低
            // 提醒用户清理内存
            MemoryManager mm = MemoryManager.newInstance(context);
            mm.showAlertDialog();
        }
    }
}
