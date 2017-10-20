package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.entity.eventbustype.DeviceSelectEvent;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.geometry.Dot;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by liuyunfan on 2016/6/22.
 */
public class DeviceSelectPointMapMenu extends SelectPointMapMenu {

    Graphic graphic;
    long zoomNum = MyApplication.getInstance().getConfigValue("MyPlanDetailLevel", 6);

    public DeviceSelectPointMapMenu(MapGISFrame mapGISFrame, Context context, String loc) {
        super(mapGISFrame, context, loc);
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("设备选择");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity(false);
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected() {

        boolean result = super.onOptionsItemSelected();

        MyApplication.getInstance().submitExecutorService(pointQuery);

        handler.obtainMessage(4).sendToTarget();
        notifySatrtPointQuery();

        return result;
    }

    @Override
    protected void backActivity(boolean isOk) {

        DeviceSelectEvent deviceSelectEvent = new DeviceSelectEvent();

        try {

            exitPointQuery();

            Intent intent = ((Activity) context).getIntent();

            deviceSelectEvent.loc = mapView.getCenterPoint().toString();
            deviceSelectEvent.addr = handler.isValid ? handler.address : "";
            deviceSelectEvent.names = handler.names;


            if (graphic != null) {

                deviceSelectEvent.layerName = handler.getLayerName();

                String filed = "编号";
                String filedVal = "";
                if (TextUtils.isEmpty(filedVal = graphic.getAttributeValue(filed))) {
                    filed = "GUID";
                }
                if (TextUtils.isEmpty(filedVal = graphic.getAttributeValue(filed))) {
                    filed = "编号";
                }
                deviceSelectEvent.filed = filed;
                deviceSelectEvent.filedVal = filedVal;
                deviceSelectEvent.patrolNo = "-1";
            }

            intent.setClass(mapGISFrame, context.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            mapGISFrame.startActivity(intent);

            mapView.removeView(view1);
            mapView.removeView(view2);
            mapView.removeView(viewBar);

            ((MmtMapView) mapView).setExtentChangeListener(null);

            mapGISFrame.resetMenuFunction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (isOk) {
                EventBus.getDefault().post(deviceSelectEvent);
            }
        }
    }

    Handler waitHandler = new Handler();
    int notifyCount = 0;

    void notifySatrtPointQuery() {
        notifyCount++;
        //针停留1000ms后才开始查询
        waitHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (notifyCount > 1) {
                    notifyCount--;
                    return;
                }
                notifyCount = 0;

                if (mapView.getZoom() < zoomNum) {
                    return;
                }
                synchronized (pointQuery) {
                    pointQuery.notifyAll();
                }
            }
        }, 1000);

    }

    void notifyStopPointQuery() {
        handler.obtainMessage(4).sendToTarget();

    }

    void exitPointQuery() {
        isExit = true;
        handler.obtainMessage(4).sendToTarget();
    }

    @Override
    protected void startAni() {
        notifyStopPointQuery();

        if (isRun)
            return;

        isRun = true;

        if (view2 != null) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -50);

            animation.setDuration(300);
            animation.setFillAfter(true);

            handler.obtainMessage(1, animation).sendToTarget();
        }
    }


    @Override
    protected void endAni() {

        if (!isRun)
            return;
        notifySatrtPointQuery();

        if (view2 != null) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, -50, 0);

            animation.setDuration(300);

            handler.obtainMessage(0, animation).sendToTarget();

            notifySatrtAddressQuery();
        }

        isRun = false;
    }

    final Runnable pointQuery = new Runnable() {
        @Override
        public void run() {
            try {
                while (!isExit) {

                    synchronized (this) {
                        wait();
                        Dot dot = null;
                        if (viewX > 0 && viewY > 0) {
                            dot = mapView.viewPointToMapPoint(new PointF(viewX, viewY));
                        }
                        if (dot == null) {
                            dot = mapView.getCenterPoint();
                        }
                        graphic = GisQueryUtil.pointQueryForSingleV2(mapView, dot);
                        if (graphic != null) {

                            mapView.setAnnotationListener(new MmtAnnotationListenerDialog());
                            handler.setLayerName(graphic.getAttributeValue("$图层名称$"));
                            handler.obtainMessage(3, graphic).sendToTarget();
                        }

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

}
