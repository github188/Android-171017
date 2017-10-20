package com.mapgis.mmt.module.gps.gpsstate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.mapgis.mmt.AppStyle;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.config.SharePreferenceConfig;
import com.mapgis.mmt.constant.GlobalPathManager;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.Receiver.BroadcastGpsReceiver;
import com.mapgis.mmt.module.gps.entity.SatelliteBean;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class GpsStatesFragment extends Fragment implements View.OnClickListener
        , GpsStatus.Listener, LocationListener, BroadcastGpsReceiver.OnGpsStatusChangedListener {
    private final static int SIGNAL_DELAY_REFRESH_DATA = 0;
    private final static int FREQUENCY_REFRESH_DATA = 1000;
    private final static int COUNT_DEFAULT_BAR = 7;
    private final static float mYAxisMaximum = 105F;

    private final DecimalFormat mDecimalFormat = new DecimalFormat("0.00000");
    private BroadcastGpsReceiver mInstance;

    private int mNumUsedStatellite;
    private ArrayList<Integer> mColorList;
    private List<GpsSatellite> mSatelliteList; // GPS卫星
    private List<SatelliteBean> mBTSatelliteList; // 蓝牙所监听到的卫星信息
    private LocationManager mLocationManager = null;

    private ImageButton mRightImgBtn = null;
    private TextView mTvTotalStatellite = null;
    private TextView mTvUsedStatellite = null;
    private TextView mTvTime = null;
    private TextView mTvProvider = null;
    private TextView mTvLongtitude = null;
    private TextView mTvLatitude = null;
    private TextView mTvAccuracy = null;
    private TextView mTvSrc = null;
    private TextView mTvX = null;
    private TextView mTvY = null;
    private TextView mTvAltitude = null;
    private TextView mTvSpeed = null;
    private BarChart mChart = null;
    /**
     * 每秒钟刷新一次数据
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SIGNAL_DELAY_REFRESH_DATA:
                    setLocationData();
                    mHandler.sendEmptyMessageDelayed(SIGNAL_DELAY_REFRESH_DATA, FREQUENCY_REFRESH_DATA);
                    break;
                default:
                    break;
            }
        }
    };

    public static Fragment getInstance() {
        return new GpsStatesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gps_state, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (isGpsEnable()) {
                ((BaseActivity) getActivity()).showToast(getResources().getString(R.string.gps_opened));
            } else {
                ((BaseActivity) getActivity()).showToast(getResources().getString(R.string.gps_closed));
            }

            mSatelliteList = new ArrayList<>(); // 卫星信号
            mColorList = new ArrayList<>();

            initView(view);

            initData();

            initListener();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {
        mChart = (BarChart) view.findViewById(R.id.mpChart);

        mTvTotalStatellite = (TextView) view.findViewById(R.id.tv_total_statellite);
        mTvUsedStatellite = (TextView) view.findViewById(R.id.tv_used_statellite);
        mTvTime = (TextView) view.findViewById(R.id.tv_time);
        mTvProvider = (TextView) view.findViewById(R.id.tv_provider);
        mTvLongtitude = (TextView) view.findViewById(R.id.tv_longitude);
        mTvLatitude = (TextView) view.findViewById(R.id.tv_latitude);
        mTvAccuracy = (TextView) view.findViewById(R.id.tv_accuracy);
        mTvSrc = (TextView) view.findViewById(R.id.tv_src);
        mTvX = (TextView) view.findViewById(R.id.tv_x);
        mTvY = (TextView) view.findViewById(R.id.tv_y);
        mTvAltitude = (TextView) view.findViewById(R.id.tv_altitude);
        mTvSpeed = (TextView) view.findViewById(R.id.tv_speed);

        View btnReportMMt = view.findViewById(R.id.btnReportMMt);

        int customBtnStyleResource = AppStyle.getCustromBtnStyleResource();
        if (customBtnStyleResource > 0) {
            btnReportMMt.setBackgroundResource(customBtnStyleResource);
        }

        btnReportMMt.setOnClickListener(this);

        initCharView();

        mRightImgBtn = ((GpsStateActivity) getActivity()).getBaseRightImageView();
        mRightImgBtn.setVisibility(View.VISIBLE);
        setRightImageButton(isGpsEnable());
    }

    private void initData() {
        mTvProvider.setText(String.format("方式：%s", SharePreferenceConfig.getGpsProvider()));
        setLocationData();
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(SIGNAL_DELAY_REFRESH_DATA, FREQUENCY_REFRESH_DATA);
        }
    }

    private void initListener() {
        mRightImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterSystemGpsSetting();
            }
        });

        mLocationManager.addGpsStatusListener(this);

        GpsReceiver instance = GpsReceiver.getInstance();
        String locationType = MyApplication.getInstance().getSystemSharedPreferences().getString("GpsReceiver", "");
        if (instance instanceof BroadcastGpsReceiver && "BT".equals(locationType)) {
            mTvUsedStatellite.setVisibility(View.GONE);
            mInstance = (BroadcastGpsReceiver) instance;
            mInstance.setOnGpsStatusListener(this);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        }
    }

    private void setGpsLocationData(@Nullable Location location) {
        if (location != null) {
            mTvAltitude.setText(String.format(Locale.CHINA, "海拔：%.1f", location.getAltitude()));
            mTvSpeed.setText(String.format(Locale.CHINA, "速度：%.1f", location.getSpeed()));
        } else {
            mTvAltitude.setText(String.format(Locale.CHINA, "海拔：%.1f", 0.0));
            mTvSpeed.setText(String.format(Locale.CHINA, "速度：%.1f", 0.0));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        setGpsLocationData(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event) {
        try {
            if (mLocationManager == null) {
                return;
            }
            GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                updateChartData(gpsStatus);
            } else if (event == GpsStatus.GPS_EVENT_STARTED) {
                setRightImageButton(true);
            } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                setRightImageButton(false);
                updateChartData(gpsStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChangedListener(List<SatelliteBean> satelliteList) {
        updateChartData(satelliteList);
    }

    private void updateChartData(List<SatelliteBean> satelliteList) {
        try {
            ArrayList<BarEntry> barEntries = getBarEntries(satelliteList);
            if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                refreshChartData(barEntries);
            } else {
                setChartData(barEntries);
            }
            mChart.invalidate();
            setStatelliteNumView(satelliteList.size(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<BarEntry> getBarEntries(List<SatelliteBean> satelliteList) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        mBTSatelliteList = new ArrayList<>();
        if (satelliteList != null && satelliteList.size() > 0) {
            // GPS状态不为空
            int count = 0;
            mColorList.clear();

            for (SatelliteBean satellite : satelliteList) {
                mBTSatelliteList.add(satellite);
                if (satellite.getSnr() < 0) {
                    continue;
                }

                if (satellite.usedInFix()) {
                    // 卫星正在使用
                    ++mNumUsedStatellite;
                    mColorList.add(getResources().getColor(R.color.colorPrimary));
                } else {
                    // 么有使用
                    mColorList.add(getResources().getColor(R.color.text_lightGray));
                }

                count++;
                barEntries.add(new BarEntry(count, satellite.getSnr()));
            }
        }

        int size = barEntries.size();

        if (size > 0 && size < COUNT_DEFAULT_BAR) {
            for (int i = 0; i < COUNT_DEFAULT_BAR - size; i++) {
                BarEntry barEntry = new BarEntry(size + i + 1, -1);
                barEntries.add(barEntry);
            }
        }

        return barEntries;
    }

    /**
     * 刷新表格数据
     */
    private void updateChartData(GpsStatus gpsStatus) {
        try {
            mSatelliteList.clear();
            mNumUsedStatellite = 0;

            ArrayList<BarEntry> barEntries = getBarEntries(gpsStatus);

            if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
                refreshChartData(barEntries);
            } else {
                setChartData(barEntries);
            }
            mChart.invalidate();
            setStatelliteNumView(mSatelliteList.size(), mNumUsedStatellite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private ArrayList<BarEntry> getBarEntries(GpsStatus status) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        if (status != null) {
            // GPS状态不为空
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            int count = 0;
            mColorList.clear();

            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                if (s.getSnr() < 0) {
                    continue;
                }

                if (s.usedInFix()) {
                    // 卫星正在使用
                    ++mNumUsedStatellite;
                    mColorList.add(getResources().getColor(R.color.colorPrimary));
                } else {
                    // 么有使用
                    mColorList.add(getResources().getColor(R.color.text_lightGray));
                }

                mSatelliteList.add(s);
                count++;
                barEntries.add(new BarEntry(count, s.getSnr()));
            }
        }

        int size = barEntries.size();

        if (size > 0 && size < COUNT_DEFAULT_BAR) {
            for (int i = 0; i < COUNT_DEFAULT_BAR - size; i++) {
                BarEntry barEntry = new BarEntry(size + i + 1, -1);
                barEntries.add(barEntry);
            }
        }

        return barEntries;
    }

    private void enterSystemGpsSetting() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void setRightImageButton(boolean isGpsEnable) {
        if (isGpsEnable) {
            // Gps可用
            mRightImgBtn.setImageResource(R.drawable.gps_open);
        } else {
            mRightImgBtn.setImageResource(R.drawable.gps_close);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setRightImageButton(isGpsEnable());
            if (mInstance != null) {
                IntentFilter filter = new IntentFilter(BroadcastGpsReceiver.ACTION_GPGSV);
                getActivity().registerReceiver(mInstance, filter);
                mInstance.setOnGpsStatusListener(this);
            } else {
                updateChartData(mLocationManager.getGpsStatus(null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (mInstance != null) {
                getActivity().unregisterReceiver(mInstance);
                mInstance.removeGpsStatusListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mLocationManager != null) {
                mLocationManager.removeGpsStatusListener(this);
                mLocationManager.removeUpdates(this);
            }
            mHandler.removeMessages(SIGNAL_DELAY_REFRESH_DATA);
            mChart.clear();
            mLocationManager = null;
            mSatelliteList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /**
     * GPS是否开启
     *
     * @return true开启，fasle关闭
     */
    private boolean isGpsEnable() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 填充柱状图数据
     */
    private void setChartData(ArrayList<BarEntry> barEntries) {
        BarDataSet snrBarDataSet;
        snrBarDataSet = new BarDataSet(barEntries, "");
        snrBarDataSet.setColors(mColorList);
        snrBarDataSet.setValueFormatter(new DataValueFormatter());

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(snrBarDataSet);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        mChart.setData(data);
    }

    /**
     * 刷新柱状图数据
     */
    private void refreshChartData(ArrayList<BarEntry> barEntries) {
        BarDataSet snrBarDataSet;
        snrBarDataSet = (BarDataSet) mChart.getData().getDataSetByIndex(0);
        snrBarDataSet.setValues(barEntries);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
    }

    private void setStatelliteNumView(int totalCount, int usedCount) {
        mTvTotalStatellite.setText(String.format(Locale.CHINA, "搜索到的卫星：%d", totalCount));
        mTvUsedStatellite.setText(String.format(Locale.CHINA, "使用中的卫星：%d", usedCount));
    }

    private void initCharView() {
        setBarChartStyle();
        updateChartData(mLocationManager.getGpsStatus(null));
    }

    private void setBarChartStyle() {
        mChart.setScaleEnabled(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setNoDataText(getString(R.string.not_search_statellite));
        mChart.getDescription().setEnabled(false);
        mChart.setMaxVisibleValueCount(60);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(0f); // only intervals of 1 day
        xAxis.setLabelCount(COUNT_DEFAULT_BAR, false);
        xAxis.setValueFormatter(new XValueFormatter());

        YValueFormatter yValueFormatter = new YValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setValueFormatter(yValueFormatter);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(mYAxisMaximum);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(6, false);
        rightAxis.setValueFormatter(yValueFormatter);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        rightAxis.setAxisMaximum(mYAxisMaximum);
        rightAxis.setEnabled(false);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setEnabled(false);
    }

    /**
     * 设置定位信息
     */
    private void setLocationData() {
        try {
            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            String latitude;    // 纬度
            String longitude;   // 经度
            String accuracy;    // 精度
            String src;         // 来源
            Location location = xyz.getLocation();

            if (location == null
                    || MyApplication.getInstance().getConfigValue("RandomGPS").length() > 0) {
                latitude = "-"/*"无纬度数据"*/;
                longitude = "-"/*"无经度数据"*/;
                accuracy = "-"/*"无精度数据"*/;
                src = MyApplication.getInstance().getSystemSharedPreferences().getString("GpsReceiver", "");
                mTvAltitude.setText(String.format(Locale.CHINA, "海拔：%.1f", 0.0));
                mTvSpeed.setText(String.format(Locale.CHINA, "速度：%.1f", 0.0));
            } else {
                latitude = mDecimalFormat.format(location.getLatitude());
                longitude = mDecimalFormat.format(location.getLongitude());
                accuracy = String.valueOf(location.getAccuracy());

                if (TextUtils.isEmpty(location.getProvider())) {
                    src = MyApplication.getInstance().getSystemSharedPreferences().getString("GpsReceiver", "");
                } else {
                    src = location.getProvider();
                }
                mTvAltitude.setText(String.format(Locale.CHINA, "海拔：%.1f", location.getAltitude()));
                mTvSpeed.setText(String.format(Locale.CHINA, "速度：%.1f", location.getSpeed()));
            }

            mTvTime.setText(String.format("时间：%s", formatTime(xyz.getReportTime())));
            mTvLatitude.setText(String.format("纬度：%s", latitude));
            mTvLongtitude.setText(String.format("经度：%s", longitude));
            mTvAccuracy.setText(String.format("精度：%s", accuracy));
            mTvSrc.setText(String.format("源自：%s", src));
            mTvX.setText(String.format("X 值：%s", String.valueOf(xyz.getX())));
            mTvY.setText(String.format("Y 值：%s", String.valueOf(xyz.getY())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTime(String time) {
        String formatime = "-";
        try {
            if (BaseClassUtil.isNullOrEmptyString(time)) {
                return formatime;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date date = sdf.parse(time);
            sdf.applyLocalizedPattern("HH点mm分ss秒");
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formatime;
    }

    @Override
    public void onClick(View v) {

        new MmtBaseTask<String, Integer, Boolean>(getActivity()) {

            protected Boolean doInBackground(String... strings) {
                String path = GlobalPathManager.getLocalConfigPath();
                return uploadFiles(path, "mmt.db", "MMT/DB/" + MyApplication.getInstance().getUserId() + ".zip");
            }

            @Override
            protected void onSuccess(Boolean isSuccess) {
                Toast.makeText(getActivity(), "发送" + (isSuccess ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
            }
        }.mmtExecute();
    }

    private boolean uploadFiles(String path, String fileName, String reportFileNames) {
        long tick = new Date().getTime();

        try {
            String srcFilePath = (path + fileName).trim();

            File srcFile = new File(srcFilePath);

            if (!srcFile.exists()) {
                return false;
            }

            FileInputStream fis = new FileInputStream(srcFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            File targetFile = new File(path + "temp.zip");
            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            ZipOutputStream zos = new ZipOutputStream(bos);
            zos.putNextEntry(new ZipEntry(MyApplication.getInstance().getUserId() + ".db"));

            //进行写操作
            int j;

            byte[] buffer = new byte[100 * 1024];

            while ((j = bis.read(buffer)) > 0) {
                zos.write(buffer, 0, j);
            }

            //关闭输入流
            bis.close();
            zos.flush();
            zos.close();

            BaseClassUtil.logd(this, "file to zip finish:" + (new Date().getTime() - tick) + "ms");

            tick = new Date().getTime();

            byte[] dataBytes = FileUtil.file2byte(targetFile);

            BaseClassUtil.logd(this, "zip to byte finish:" + (new Date().getTime() - tick) + "ms");

            tick = new Date().getTime();

            String serverPath = Uri.encode(reportFileNames.trim());
            String url = ServerConnectConfig.getInstance().getMobileBusinessURL()
                    + "/BaseREST.svc/UploadByteResource?path=" + serverPath;

            NetUtil.executeHttpPost(url, dataBytes, 60, 3600);

            BaseClassUtil.logd(this, "byte to server finish:" + (new Date().getTime() - tick) + "ms");

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            BaseClassUtil.logd(this, "byte to server exception:" + (new Date().getTime() - tick) + "ms");
        }

        return false;
    }
/*
* 压缩mmt.db文件方便web下载
* */

    /**
     * 格式化数据
     */
    private class DataValueFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            if (mInstance == null) {
                if ((int) entry.getX() > mSatelliteList.size()) {
                    return "";
                }
            } else {
                if ((int) entry.getX() > mBTSatelliteList.size()) {
                    return "";
                }
            }
            return (int) v + "";
        }
    }

    /**
     * x轴数据的格式化
     */
    private class XValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float v, AxisBase axisBase) {
            int index = (int) v;
            if (mInstance == null) {
                return getGpsFormattedValue(index);
            } else {
                return getBTFormattedValue(index);
            }
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }

    private String getBTFormattedValue(int index) {
        if (mBTSatelliteList == null || index > mBTSatelliteList.size() || index < 1) {
            return "";
        } else {
            SatelliteBean t = mBTSatelliteList.get(index - 1);
            return t.getPrn() + "";
        }
    }

    @NonNull
    private String getGpsFormattedValue(int index) {
        if (mSatelliteList == null || index > mSatelliteList.size() || index < 1) {
            return "";
        } else {
            GpsSatellite t = mSatelliteList.get(index - 1);
            return t.getPrn() + "";
        }
    }

    /**
     * 格式化Y值
     */
    private class YValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float v, AxisBase axisBase) {
            if (v < 0) {
                axisBase.setDrawLabels(false);
                return "";
            } else {
                return (int) v + "";
            }
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }
}