package com.mapgis.mmt.global;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import com.aidl.IGisServer;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.Document;
import com.zondy.mapgis.map.Map;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class MmtGISService extends Service {
    Document document = new Document();
    Map map = new Map();

    public void onCreate() {
        super.onCreate();
    }

    IGisServer.Stub iGisServer = new IGisServer.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getMapName() throws RemoteException {
            return "";
        }

        @Override
        public void setMapName(String mapName) throws RemoteException {
            loadMap(mapName);
        }

        @Override
        public String getEntireExtent(String mapName) throws RemoteException {
            return map.getEntireRange().toString();
        }

        @Override
        public byte[] getVectorImage(String mapName, long imgWidth, long imgHeight,
                                     double dispRectXmin, double dispRectYmin, double dispRectXmax, double dispRectYmax,
                                     String strShowLayers, int imageType) throws RemoteException {
            Bitmap bitmap = null;
            ByteArrayOutputStream out = null;
            byte[] buffer = null;
            long tick = new Date().getTime();

            try {
                if (map == null || map.getLayerCount() == 0) {
                    loadMap(mapName);
                }

                bitmap = Bitmap.createBitmap((int) imgWidth, (int) imgHeight, Bitmap.Config.ARGB_8888);

                map.outputToBitmap(
                        new Rect(dispRectXmin, dispRectYmin, dispRectXmax, dispRectYmax), bitmap);

                out = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.flush();

                buffer = out.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();

                    if (bitmap != null)
                        bitmap.recycle();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                int size = buffer != null ? buffer.length : 0;

                BaseClassUtil.logd(this, "get_image[time:" + (new Date().getTime() - tick) + "ms," +
                        "size:" + (size < 1000 ? (size + "byte") : (size / 1000 + "kb")) + "]");
            }

            return buffer;
        }
    };

    private synchronized void loadMap(String mapName) {
        try {
            if (map != null && map.getLayerCount() > 0)
                return;

            String path = MyApplication.getInstance().getMapFilePath() + mapName + "/" + mapName + ".mapx";

            document.open(path);

            map = document.getMaps().getMap(0);

            BaseClassUtil.logd(this, "finish set map[" + mapName + "]");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BaseClassUtil.loge(this, "开始启动后台GIS服务onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        BaseClassUtil.loge(this, "开始绑定后台GIS服务onBind");

        return iGisServer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        BaseClassUtil.loge(this, "onUnbind");

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        BaseClassUtil.loge(this, "onRebind");

        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        document.close(false);
    }
}
