package com.project.enn.dahua;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.mapgis.mmt.module.gis.MapGISFrame;
import com.project.enn.dahua.service.AlarmHeartService;
import com.project.enn.dahua.service.DaHuaService;

/**
 * Created by Comclay on 2017/4/21.
 * 大华服务的帮助类
 */

public class ServiceHelper {

    private ServiceConnection mHeartConnImpl;
    private ServiceConnection mDaHuaConnImpl;

    private ServiceHelper() {
    }

    private static ServiceHelper mInstance = new ServiceHelper();

    public static ServiceHelper getInstance() {
        return mInstance;
    }

    /**
     * DaHuaService绑定到context上
     */
    public void bindDaHuaService(MapGISFrame gisFrame) {
        mDaHuaConnImpl = new ServiceConnectionImpl();
        Intent intent = new Intent(gisFrame, DaHuaService.class);
        gisFrame.bindService(intent, mDaHuaConnImpl, Context.BIND_AUTO_CREATE);
    }

    public void unbindDaHuaService(MapGISFrame gisFrame) {
        gisFrame.unbindService(mDaHuaConnImpl);
    }

    public void bindHeartService(DaHuaService daHuaService) {
        mHeartConnImpl = new ServiceConnectionImpl();
        Intent intent = new Intent(daHuaService, AlarmHeartService.class);
        daHuaService.bindService(intent, mHeartConnImpl, Context.BIND_AUTO_CREATE);
    }

    /*停止报警心跳服务*/
    public void unbindHeartService(DaHuaService daHuaService) {
        if (AlarmHeartService.isRun && mHeartConnImpl != null) {
//            daHuaService.unbindService(mHeartConnImpl);
            Intent intent = new Intent(daHuaService, AlarmHeartService.class);
            daHuaService.stopService(intent);
        }
    }

    private class ServiceConnectionImpl implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class DaHuaServiceBinder extends Binder {

    }
}
