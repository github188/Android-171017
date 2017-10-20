package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.MapViewExtentChangeListener;
import com.mapgis.mmt.module.gis.MmtMapView;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.geometry.Dot;

/**
 * 只选点
 */
public class SelectPointMapMenu extends BaseMapMenu {
    public SelectPointMapMenu(MapGISFrame mapGISFrame, Context context, String loc) {
        super(mapGISFrame);

        try {
            this.context = context;

            if (!BaseClassUtil.isNullOrEmptyString(loc) && loc.contains(","))
                this.loc = new Dot(Double.valueOf(loc.split(",")[0]), Double.valueOf(loc.split(",")[1]));
            Intent intent = new Intent(context, MapGISFrame.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            context.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("地图选点");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity(false);
            }
        });

        return view;
    }

    Context context;
    Dot loc;

    ImageView view1;
    ImageView view2;
    View viewBar;

    protected int viewX;
    protected int viewY;

    @Override
    public boolean onBackPressed() {
        backActivity(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected() {
        try {

            ((MmtMapView) this.mapView).setExtentChangeListener(new MapViewExtentChangeListener() {

                @Override
                public void ExtentChanging() {
                    startAni();
                }

                @Override
                public void ExtentChanged() {
                    endAni();
                }
            });

            mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

            int mapCenterX = (mapView.getRight() - mapView.getLeft()) / 2;
            int mapCenterY = (mapView.getBottom() - mapView.getTop()) / 2;

            view1 = new ImageView(mapGISFrame);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);

            viewX = mapCenterX - DimenTool.dip2px(mapGISFrame, 5);
            viewY = mapCenterY - DimenTool.dip2px(mapGISFrame, 1);
            params.setMargins(viewX, viewY, 0, 0);

            view1.setLayoutParams(params);

            view1.setImageResource(R.drawable.icon_select_shadow);

            mapView.addView(view1);

            view2 = new ImageView(mapGISFrame);

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(-2, -2);

            params2.setMargins(mapCenterX - DimenTool.dip2px(mapGISFrame, 10), mapCenterY - DimenTool.dip2px(mapGISFrame, 30), 0, 0);

            view2.setLayoutParams(params2);

            view2.setImageResource(R.drawable.icon_select_point);

            mapView.addView(view2);

            if (loc != null && (loc.x != 0 && loc.y != 0)) {
                mapView.panToCenter(loc, false);
            } else {
                mapView.panToCenter(GpsReceiver.getInstance().getLastLocalLocation().convertToPoint(), false);
            }

            viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);

            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            viewBar.setLayoutParams(params1);

            viewBar.findViewById(R.id.tvOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backActivity(true);
                }
            });

            mapView.addView(viewBar);

            handler = new SelectPointHandle(view2, viewBar, mapGISFrame, "");

            MyApplication.getInstance().submitExecutorService(findAddr);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    protected void backActivity(boolean isOk) {
        try {
            Intent intent = ((Activity) context).getIntent();

            if (isOk) {
                intent.putExtra("loc", mapView.getCenterPoint().toString());

                intent.putExtra("addr", handler.isValid ? handler.address : "");
                intent.putStringArrayListExtra("names", handler.names);
            } else {
                intent.putExtra("loc", loc == null ? "" : loc.toString());
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
            isExit = true;
        }
    }

    protected void queryAddress() {
        FindResult result = new FindResult();
        result.formatted_address = "定位中...";

        handler.obtainMessage(2, result).sendToTarget();

        result = new FindResult();
        result.formatted_address = "地图上的位置";

        Dot dot = mapView.getCenterPoint();

        Location location = GpsReceiver.getInstance().getLastLocationConverse(new GpsXYZ(dot.x, dot.y));

        if (location != null) {
            FindResult findResult = BDGeocoder.find(location).result;

            if (findResult != null)
                result = findResult;
        }
        if (TextUtils.isEmpty(result.formatted_address)) {
            return;
        }
        handler.obtainMessage(2, result).sendToTarget();
    }

    protected boolean isExit = false;
    Runnable findAddr = new Runnable() {
        @Override
        public void run() {

            while (!isExit) {
                try {
                    queryAddress();
                    synchronized (this) {
                        wait();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    };

    protected SelectPointHandle handler;
    protected boolean isRun = false;

    protected void startAni() {
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

    protected void endAni() {
        if (!isRun)
            return;

        if (view2 != null) {
            TranslateAnimation animation = new TranslateAnimation(0, 0, -50, 0);

            animation.setDuration(300);

            handler.obtainMessage(0, animation).sendToTarget();

            notifySatrtAddressQuery();
        }

        isRun = false;
    }

    private int notifyAddressCount = 0;
    private Handler waitAddrHandler = new Handler();

    protected void notifySatrtAddressQuery() {
        notifyAddressCount++;
        //针停留1000ms后才开始查询
        waitAddrHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (notifyAddressCount > 1) {
                    notifyAddressCount--;
                    return;
                }
                notifyAddressCount = 0;

                synchronized (findAddr) {
                    findAddr.notifyAll();
                }
            }
        }, 1000);

    }

}
