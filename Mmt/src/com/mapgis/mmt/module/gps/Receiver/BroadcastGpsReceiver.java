package com.mapgis.mmt.module.gps.Receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.global.MmtMainService;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.entity.SatelliteBean;
import com.mapgis.mmt.module.gps.trans.CoordinateConvertor;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.login.UserBean;

import java.util.ArrayList;
import java.util.List;


public class BroadcastGpsReceiver extends GpsReceiver {
    private static final String TAG = "BroadcastGpsReceiver";

    // 坐标信息
    public static final String ACTION_GPGGA = "com.mapgis.mmt.global.MmtMainService";
    // 卫星信息
    public static final String ACTION_GPGSV = "com.mapgis.broadcasat.gpgsv";

    private Context context;
    private int mLastIndex = 0;
    private List<SatelliteBean> mSatelliteList = new ArrayList<>();
    private OnGpsStatusChangedListener mOnGpsStatusChangedListener;

    public void setOnGpsStatusListener(OnGpsStatusChangedListener listener) {
        this.mOnGpsStatusChangedListener = listener;
    }

    public void removeGpsStatusListener() {
        this.mOnGpsStatusChangedListener = null;
    }

    @Override
    public String start(final CoordinateConvertor coordinateConvertor) {
        this.coordinateConvertor = coordinateConvertor;

        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {
                try {
                    CoordinateConvertor convertor = new CoordinateConvertor();

                    UserBean bean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

                    if (bean != null && bean.isOffline) {
                        convertor.LoadTransParamsFromLocal(GlobalPathManager.getLocalConfigPath() + GlobalPathManager.TRANS_PARAMS_FILE);
                    } else {
                        convertor.LoadTransParamsFromWeb(GlobalPathManager.TRANS_PARAMS_FILE);
                    }

                    BroadcastGpsReceiver.this.coordinateConvertor = convertor;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        context = MyApplication.getInstance();

        context.registerReceiver(this, new IntentFilter(MmtMainService.class.getName()));

        return null;
    }

    @Override
    public void stop() {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ACTION_GPGGA:
                receiveGpgga(intent);
                break;
            case ACTION_GPGSV:
                receiveGpgsv(intent);
                break;
        }
    }

    /**
     * 接受卫星信息
     */
    private void receiveGpgsv(Intent intent) {
        try {
            String gpgsv = intent.getStringExtra("gpgsv");
//            Log.i(TAG, "接收广播：" + gpgsv);
            if (BaseClassUtil.isNullOrEmptyString(gpgsv) && !gpgsv.startsWith("$GPGSV")) {
                return;
            }
            String[] infos = gpgsv.split(",");
            int endIndex = infos.length - 1;
            // 将最后一个数据用*分割并代替原来的字符
            infos[endIndex] = infos[endIndex].split("[*]")[0];
            // 解析nmea语句
            // $GPGSV,3,1,10,	10,59,324,16	,12,35,103,23	,14,19,281,	,18,86,093,	*78

            // [3],卫星总数，一条gpgsv语句中最多包含4条语句
            int satelliteCount = formatString(infos[3]);
            if (satelliteCount == 0){
                return;
            }
            // [1],gpgsv条数
            int lineNum = formatString(infos[1]);
            // [2],当前语句编号
            int lineIndex = formatString(infos[2]);
            if (lineIndex - 1 != mLastIndex) {
                mSatelliteList.clear();
                mLastIndex = 0;
                return;
            } else {
                mLastIndex = lineIndex;
            }

            SatelliteBean satellite = null;
            for (int i = 4; i < infos.length; i++) {
                int value = formatString(infos[i]);
                switch (i % 4) {
                    case 0:// prn
                        satellite = new SatelliteBean(value);
                        break;
                    case 1://仰角
                        break;
                    case 2:// 方位角
                        break;
                    case 3://信噪比
                        if (satellite != null) {
                            satellite.setSnr(value);
                            mSatelliteList.add(satellite);
                        }
                        break;
                }
            }

            if (lineNum == lineIndex && mSatelliteList.size() == satelliteCount) {
                // 通知更新卫星状态
                if (mOnGpsStatusChangedListener != null) {
                    mOnGpsStatusChangedListener.onChangedListener(mSatelliteList);
                }

                // 通知完后清空数据重新接受卫星信息
                mSatelliteList.clear();
                mLastIndex = 0;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private int formatString(String str){
        if (BaseClassUtil.isNullOrEmptyString(str)){
            return 0;
        }else{
            return Integer.valueOf(str);
        }
    }

    /**
     * 接受坐标信息
     */
    private void receiveGpgga(Intent intent) {
        GpsXYZ xy = intent.getParcelableExtra("xy");
        if (xy != null){
            lastXY = xy;
            lastLocation = xy.getLocation();
        }
    }

    public interface OnGpsStatusChangedListener {
        void onChangedListener(List<SatelliteBean> satelliteList);
    }
}
