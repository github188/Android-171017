package com.patrol.module.posandpath2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrol.module.posandpath2.beans.PersonInfo;
import com.patrol.module.posandpath2.beans.UserInfo;
import com.patrol.module.posandpath2.detailinfo.DetailInfoActivity;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;

/**
 * Created by Comclay on 2017/3/16.
 * 自定义位置Annotation上气泡窗体
 */

public class PosMapViewAnnotationListener extends DefaultMapViewAnnotationListener {
    private AsyncTask<String, Void, String> mAsyncTask;
    private final static int POINT_COUNT = 4;
    private int count = 0;
    private TextView mTvAddr;
    private LocatingThread mThread;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mThread == null || mThread.isStop) {
                return;
            }
            int num = POINT_COUNT - msg.arg1 % POINT_COUNT - 1;
            String s = "正在定位具体位置";
            for (int i = 0; i < num; i++) {
                s += ".";
            }
            mTvAddr.setText(s);
        }
    };

    @Override
    public void mapViewClickAnnotationView(MapView mapView, AnnotationView annotationView) {
        /*Annotation annotation = annotationView.getAnnotation();
        if (annotation instanceof DataBindAnnotation) {
            initMapLayoutView(Case.CASE_PATROLLER_PATH);
            UserInfo userInfo = (UserInfo) ((DataBindAnnotation) annotation).getT();
            showPathOnMap(userInfo.Perinfo.USERID, userInfo.Perinfo.name, null);
        }*/
    }

    @Override
    public boolean mapViewWillHideAnnotationView(MapView mapview, AnnotationView annotationview) {
        if (mThread != null) {
            mThread.isStop = false;
            mThread = null;
        }

        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        return super.mapViewWillHideAnnotationView(mapview, annotationview);
    }

    @Override
    public AnnotationView mapViewViewForAnnotation(MapView mapview, Annotation annotation) {
        Context context = mapview.getContext();
        AnnotationView annotationView = new AnnotationView(annotation, context);
        annotationView.setPanToMapViewCenter(false);
        if (context instanceof Activity && annotation instanceof DataBindAnnotation) {
            setAnnotationContentView((DataBindAnnotation) annotation, context, annotationView);
        }
        return annotationView;
    }

    private void setAnnotationContentView(DataBindAnnotation annotation, final Context context, final AnnotationView annotationView) {
        View calloutView = annotationView.getCalloutView();
//      calloutView.setBackgroundColor(context.getResources().getColor(R.color.color_ffffff));
        final View contentView = ((Activity) context).getLayoutInflater().inflate(R.layout.view_content_annotation_tip
                , (ViewGroup) calloutView
                , false);
        annotationView.setCalloutContentView(contentView);

        final UserInfo userInfo = (UserInfo) annotation.getT();
        PersonInfo perinfo = userInfo.Perinfo;
        ((TextView) contentView.findViewById(R.id.tv_name)).setText(perinfo.name);
        ((TextView) contentView.findViewById(R.id.tv_dept)).setText(perinfo.partment);
        ((TextView) contentView.findViewById(R.id.tv_role)).setText(perinfo.Role);
        contentView.findViewById(R.id.iv_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailInfoActivity.class);
                intent.putExtra(DetailInfoActivity.ARG_MAP_OBJECT, userInfo);
                context.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation((Activity) context);
            }
        });

        mTvAddr = ((TextView) contentView.findViewById(R.id.tv_address));
        // 已经获取过位置
        if (!BaseClassUtil.isNullOrEmptyString(userInfo.point.address)) {
            mTvAddr.setText(userInfo.point.address);
            return;
        }

        // 这里调用百度地图查询当前的位置
        mTvAddr.setText(R.string.text_locating);
        mThread = new LocatingThread();
        mThread.start();

        mAsyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String position[] = params[0].split(",");

                GpsXYZ gpsXYZ = new GpsXYZ(Double.valueOf(position[0]), Double.valueOf(position[1]));
                Location location = GpsReceiver.getInstance().getLastLocationConverse(gpsXYZ);

                BDGeocoderResult bdGeocoderResult = BDGeocoder.find(location);
                if (bdGeocoderResult == null) {
                    return "";
                }
                FindResult result = bdGeocoderResult.result;
                mThread.isStop = true;
                return result.formatted_address;
            }

            @Override
            protected void onPostExecute(String s) {
                String address = "";
                if (BaseClassUtil.isNullOrEmptyString(s)) {
                    mThread.isStop = true;
                    address = context.getString(R.string.text_not_located);
                } else {
                    userInfo.point.address = s;
                    address = s;
                }
                mTvAddr.setText(address);
            }
        };
        mAsyncTask.execute(userInfo.point.Position);
    }

    private class LocatingThread extends Thread {
        boolean isStop = false;

        @Override
        public void run() {
            super.run();
            while (!isStop) {
                try {
                    Message msg = Message.obtain();
                    msg.arg1 = count++;
                    handler.sendMessage(msg);
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
