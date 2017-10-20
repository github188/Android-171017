package com.repair.gisdatagather.product.xyshow;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.repair.gisdatagather.product.MmtMapToolCustrom;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.simplecache.ACache;

import java.util.concurrent.TimeUnit;

/**
 * Created by liuyunfan on 2016/5/6.
 */
public class XYShow implements XYShowInterface, View.OnClickListener {
    GisGather gisGather;
    View view;
    TextView accuracyView;
    // float accuracy = 0.0f;
    public View xView;
    public View yView;


    TextView txtCurrentX;
    TextView txtCurrentY;
    TextView txtCurrentZ;
    TextView txtCurrentState;
    TextView txtDeviceH;
    ACache aCache;

    public XYShow(GisGather gisGather, View view) {
        this.gisGather = gisGather;
        this.view = view;
        xView = view.findViewById(R.id.xEditText);
        yView = view.findViewById(R.id.yEditText);
        accuracyView = (TextView) view.findViewById(R.id.accuracy);
        gisGather.mapView.setMapTool(new MmtMapToolCustrom(gisGather.mapView, xView, yView, gisGather.dot));
    }

    public void initXYShow() {
        initXYView();

        View localDot;
        if ((localDot = view.findViewById(R.id.localDot)) != null) {
            localDot.setOnClickListener(this);
        }
        View localDeviceDot;
        if ((localDeviceDot = view.findViewById(R.id.localDeviceDot)) != null) {
            localDeviceDot.setOnClickListener(this);
        }
        View deviceHSet = view.findViewById(R.id.deviceH);
        if (deviceHSet != null) {
            deviceHSet.setOnClickListener(this);
        }

        //实时显示当前位置的xyz，精度，状态
        View currentX = view.findViewById(R.id.txtCurrentX);
        View currentY = view.findViewById(R.id.txtCurrentY);
        View currentZ = view.findViewById(R.id.txtCurrentZ);
        View currentState = view.findViewById(R.id.txtState);
        if (currentX == null) {
            return;
        }
        if (currentY == null) {
            return;
        }
        if (currentZ == null) {
            return;
        }
        if (currentState == null) {
            return;
        }
        txtCurrentX = (TextView) currentX;
        txtCurrentY = (TextView) currentY;
        txtCurrentZ = (TextView) currentZ;
        txtCurrentState = (TextView) currentState;

        gisGather.isShowCurrentLocRun = true;

        final Handler showXYHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                GpsXYZ gpsXYZdot = GpsReceiver.getInstance().getLastLocalLocation();
                if (gpsXYZdot == null) {
                    return;
                }
                txtCurrentX.setText(String.valueOf(Convert.FormatDouble(gpsXYZdot.getX())));
                txtCurrentY.setText(String.valueOf(Convert.FormatDouble(gpsXYZdot.getY())));
                txtCurrentZ.setText(String.valueOf(Convert.FormatDouble(gpsXYZdot.getZ())));
                txtCurrentState.setText(gpsXYZdot.getLocation().getProvider());
                accuracyView.setText(String.valueOf(gpsXYZdot.getLocation().getAccuracy()));
            }
        };
        MyApplication.getInstance().submitExecutorService(new Runnable() {
            @Override
            public void run() {
                while (gisGather.isShowCurrentLocRun) {
                    try {
                        showXYHandler.sendEmptyMessage(1001);
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        //设备自身高度
        View viewDeviceH = view.findViewById(R.id.txtDeviceH);
        initDeviceHView(viewDeviceH);

    }

    public void initDeviceHView(View viewDeviceH) {
        if (viewDeviceH instanceof TextView) {
            txtDeviceH = (TextView) viewDeviceH;
            if (aCache == null) {
                aCache = BaseClassUtil.getConfigACache();
            }
            String deviceHStr = aCache.getAsString("deviceH");
            double deviceHF = 0.0;
            if (!TextUtils.isEmpty(deviceHStr)) {
                deviceHF = Double.valueOf(deviceHStr);
            }
            txtDeviceH.setText(String.valueOf(deviceHF));
        }
    }


    @Override
    public void onClick(View v) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }
        String text = ((TextView) v).getText().toString();
        switch (text) {
            case "跳转": {
                paintoDot();
            }
            break;
            case "设备获取": {
                paintoDotByDevice();
            }
            break;
            case "RTK": {
                paintoDotByRTK();
            }
            case "杆高":
            case "杆高设置": {
                deviceSet(txtDeviceH);
            }
            break;
            default: {
                // Toast.makeText(gisGather.mapGISFrame, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void initXYView() {
        GpsXYZ gpsXYZdot = GpsReceiver.getInstance().getLastLocalLocation();
        if (gpsXYZdot == null) {
            return;
        }

        gisGather.dot.x = gpsXYZdot.getX();
        gisGather.dot.y = gpsXYZdot.getY();

        String xStr = String.valueOf(Convert.FormatDouble(gpsXYZdot.getX()));
        String yStr = String.valueOf(Convert.FormatDouble(gpsXYZdot.getY()));
        String zStr = String.valueOf(Convert.FormatDouble(gpsXYZdot.getZ()));
        String accuracyStr = String.valueOf(gpsXYZdot.getLocation().getAccuracy());
        String stateStr = gpsXYZdot.getLocation().getProvider();

        if (txtCurrentX != null) {
            txtCurrentX.setText(xStr);
        }
        if (txtCurrentY != null) {
            txtCurrentY.setText(yStr);
        }
        if (txtCurrentZ != null) {
            txtCurrentZ.setText(zStr);
        }
        if (accuracyView != null) {
            accuracyView.setText(accuracyStr);
        }
        if (txtCurrentState != null) {
            txtCurrentState.setText(stateStr);
        }

        if (xView instanceof EditText) {
            ((EditText) xView).setText(xStr);
        }
        if (yView instanceof EditText) {
            ((EditText) yView).setText(yStr);
        }

        if (xView instanceof TextView) {
            ((TextView) xView).setText(xStr);
        }
        if (yView instanceof TextView) {
            ((TextView) yView).setText(yStr);
        }

    }

    @Override
    public void paintoDot() {
        gisGather.mapView.panToCenter(gisGather.dot, true);
        gisGather.mapView.refresh();
    }

    @Override
    public void paintoDotByDevice() {
        initXYView();
        paintoDot();
    }

    @Override
    public void paintoDotByRTK() {
//        if (!isRTKOK) {
//            return;
//        }
//        initXY();
//        initXYView();
//        paintoDot();
    }

    public void deviceSet(final TextView txtDeviceH) {
        final EditText editTxtDeviceH = new EditText(gisGather.mapGISFrame);
        editTxtDeviceH.setLines(1);
        editTxtDeviceH.setBackgroundResource(com.mapgis.mmt.R.drawable.edit_text_default);
        if (aCache == null) {
            aCache = BaseClassUtil.getConfigACache();
        }
        String preDeviceH = aCache.getAsString("deviceH");
        String preDeviceHStr = "0.0";
        if (!TextUtils.isEmpty(preDeviceH)) {
            preDeviceHStr = preDeviceH;
        }
        editTxtDeviceH.setText(preDeviceHStr);
        OkCancelDialogFragment fragment = new OkCancelDialogFragment("请输入杆高", editTxtDeviceH);
        fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                String val = editTxtDeviceH.getText().toString();

                if (TextUtils.isEmpty(val)) {
                    return;
                }
                if (!BaseClassUtil.isNum(val)) {
                    return;
                }
                double valD = Double.valueOf(val);


                aCache.put("deviceH", String.valueOf(valD));

                txtDeviceH.setText(String.valueOf(valD));
                if (txtCurrentZ == null) {
                    return;
                }
                String curZStr = txtCurrentZ.getText().toString();
                if (!BaseClassUtil.isNum(curZStr)) {
                    return;
                }
                double curZ = Double.valueOf(curZStr);
                double nowZ = curZ - valD;
                txtCurrentZ.setText(String.valueOf(nowZ));
            }
        });
        fragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
    }
}
