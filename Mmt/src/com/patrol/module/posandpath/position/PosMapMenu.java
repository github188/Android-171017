package com.patrol.module.posandpath.position;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

/**
 * User: zhoukang
 * Date: 2016-03-22
 * Time: 17:18
 * <p/>
 * trunk:
 */
public class PosMapMenu extends BaseMapMenu {

    private Context context;
    private String name;
    private Dot dot;

    private String locStr;
    private String nearByStr;

    public PosMapMenu(MapGISFrame mapGISFrame, Context context, String name, Dot dot) {
        super(mapGISFrame);
        this.context = context;
        this.name = name;
        this.dot = dot;
    }

    View viewBar;

    @Override
    public boolean onOptionsItemSelected() {
        try {
            if (this.dot == null)
                return false;

            mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

            mapView.getAnnotationLayer().removeAllAnnotations();

            Bitmap bitmap = BitmapFactory.decodeResource(
                    mapGISFrame.getResources(), R.drawable.icon_select_point);

            Annotation annotation = new Annotation(name, null, this.dot,
                    bitmap);

            mapView.getAnnotationLayer().addAnnotation(annotation);

            mapView.panToCenter(this.dot, true);
            mapView.refresh();

            viewBar = mapGISFrame.getLayoutInflater().inflate(
                    R.layout.map_select_point_bar, null);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                    -1, -2);

            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            viewBar.setLayoutParams(params1);

            mapView.addView(viewBar);

            TextView tvOk = (TextView) viewBar.findViewById(R.id.tvOk);
            tvOk.setVisibility(View.GONE);

            ImageView imageView2 = (ImageView) viewBar.findViewById(R.id.imageView2);
            imageView2.setVisibility(View.GONE);

            handler = new MyHandle(viewBar);
            MyApplication.getInstance().submitExecutorService(findAddr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("地图定位");

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity(false);
            }
        });

        return view;
    }

    Runnable findAddr = new Runnable() {
        @Override
        public void run() {
            FindResult result = new FindResult();
            result.formatted_address = "定位中...";

            handler.obtainMessage(2, result).sendToTarget();

            result = new FindResult();
            result.formatted_address = "地图上的位置";

            try {
                Location location = GpsReceiver.getInstance()
                        .getLastLocationConverse(new GpsXYZ(dot.x, dot.y));
                BDGeocoderResult r;

                if (location != null
                        && ((r = BDGeocoder.find(location)) != null)
                        && r.result != null) {
                    result = r.result;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            handler.obtainMessage(2, result).sendToTarget();
        }
    };

    MyHandle handler;

    static class MyHandle extends Handler {
        private View viewBar;

        protected MyHandle(View viewBar) {
            this.viewBar = viewBar;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what != 2)
                    return;

                FindResult result = (FindResult) msg.obj;

                String address = result.formatted_address;
                boolean isValid = result.addressComponent != null;

                if (isValid)
                    address = result.addressComponent.district
                            + result.addressComponent.street
                            + result.addressComponent.street_number;

                ((TextView) viewBar.findViewById(R.id.tvAddr1))
                        .setText(address);

                if (result.pois != null && result.pois.size() > 0) {
                    viewBar.findViewById(R.id.tvAddr2).setVisibility(
                            View.VISIBLE);
                    String name = result.pois.get(0).name;

                    ((TextView) viewBar.findViewById(R.id.tvAddr2)).setText("在"
                            + name + "附近");
                } else
                    viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void backActivity(boolean showDetail) {
        try {
            Intent intent = ((Activity) context).getIntent();

            intent.setClass(mapGISFrame, context.getClass());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            mapGISFrame.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(mapGISFrame);

            if (!showDetail) {
                mapView.removeView(viewBar);

                mapGISFrame.resetMenuFunction();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onBackPressed() {
        backActivity(false);

        return true;
    }
}
