package com.mapgis.mmt.module.gis.toolbar.online.query.coord;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.geometry.Dot;

import java.lang.ref.WeakReference;

public class CoordQueryMapMenu extends BaseMapMenu implements CoordInputDialogFragment.OnQueryListener {

    private ShowAddressHandler showAddressHandler;

    public CoordQueryMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        if (TextUtils.isEmpty(MobileConfig.MapConfigInstance.VectorService)) {
            Toast.makeText(mapGISFrame, "未配置地图矢量服务，无法查询", Toast.LENGTH_SHORT).show();
            return false;
        }

        CoordInputDialogFragment dialogFragment = new CoordInputDialogFragment();
        dialogFragment.setOnQueryListener(this);
        dialogFragment.show(mapGISFrame.getSupportFragmentManager(), CoordInputDialogFragment.TAG);
        return true;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("坐标查询");
        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapGISFrame.resetMenuFunction();
            }
        });
        return view;
    }

    @Override
    public void query(final double xCoord, final double yCoord) {

        locate(new Dot(xCoord, yCoord));

        MmtBaseTask<String, Void, OnlineFeature> mmtBaseTask = new MmtBaseTask<String, Void, OnlineFeature>(mapGISFrame) {
            @Override
            protected OnlineFeature doInBackground(String... params) {
                String url = ServerConnectConfig.getInstance().getBaseServerPath() + "/rest/services/MapServer.svc/"
                        + MobileConfig.MapConfigInstance.VectorService + "/identify2?x=" + xCoord + "&y=" + yCoord;

                String result = NetUtil.executeHttpGet(url);
                if (TextUtils.isEmpty(result)) {
                    return null;
                }

                OnlineFeature onlineFeature = null;

                try {
                    onlineFeature = new Gson().fromJson(result, OnlineFeature.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

                return onlineFeature;
            }

            @Override
            protected void onSuccess(OnlineFeature onlineFeature) {
                if (onlineFeature == null) {
                    mapGISFrame.showToast("未查询到信息!");
                    return;
                }

                mapView.getAnnotationLayer().removeAllAnnotations();
                mapView.getGraphicLayer().removeAllGraphics();

                mapView.setAnnotationListener(new MmtAnnotationListener());

                String field = LayerConfig.getInstance().getConfigInfo(onlineFeature.layerName).HighlightField;
                String highlight = TextUtils.isEmpty(field) ? "" : onlineFeature.attributes.get(field);

                if (TextUtils.isEmpty(highlight)) {
                    highlight = "-";
                }

                onlineFeature.showAnnotationOnMap(mapView, highlight, onlineFeature.layerName,
                        BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding))
                        .showAnnotationView();

                GraphicPoint graphic = new GraphicPoint();
                graphic.setColor(Color.RED);
                graphic.setSize(5);
                graphic.setPoint(onlineFeature.geometry.toDot());
                mapView.getGraphicLayer().addGraphic(graphic);
                mapView.getGraphicLayer().addGraphic(onlineFeature.geometry.createGraphicPolylin());

                mapView.refresh();
            }
        };
        mmtBaseTask.setCancellable(false);
        mmtBaseTask.mmtExecute();
    }

    private void locate(final Dot locDot) {

        View bottomView = initBottomBarView(R.layout.map_select_point_bar);

        TextView tvOk = (TextView) bottomView.findViewById(R.id.tvOk);
        tvOk.setVisibility(View.VISIBLE);
        tvOk.setText("导航");
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    GisUtil.callOuterNavigationApp(mapGISFrame, locDot);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (showAddressHandler == null) {
            this.showAddressHandler = new ShowAddressHandler(bottomView);
        } else {
            showAddressHandler.update(bottomView);
        }
        MyApplication.getInstance().submitExecutorService(new FindAddressRunnable(locDot, showAddressHandler));

        Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_lcoding);
        Annotation annotation = new Annotation(locDot.toString(), "", locDot, bitmap);
        mapView.getAnnotationLayer().addAnnotation(annotation);
        mapView.panToCenter(locDot, true);
        mapView.refresh();
    }

    private static class ShowAddressHandler extends Handler {

        static final int RC_FIND_ADDRESS = 1;

        private WeakReference<View> bottomBarView;

        public ShowAddressHandler(View bottomView) {
            this.bottomBarView = new WeakReference<>(bottomView);
        }

        public void update(View bottomView) {
            this.bottomBarView = new WeakReference<>(bottomView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case RC_FIND_ADDRESS: {
                    FindResult result = (FindResult) msg.obj;

                    String address;
                    if (result.addressComponent != null) {
                        address = result.addressComponent.district
                                + result.addressComponent.street
                                + result.addressComponent.street_number;
                    } else {
                        address = result.formatted_address;
                    }

                    View viewBar = bottomBarView.get();
                    if (viewBar != null) {
                        ((TextView) viewBar.findViewById(R.id.tvAddr1)).setText(address);
                        TextView tvAddr2 = (TextView) viewBar.findViewById(R.id.tvAddr2);
                        if (result.pois != null && result.pois.size() > 0) {
                            tvAddr2.setVisibility(View.VISIBLE);
                            tvAddr2.setText("在" + result.pois.get(0).name + "附近");
                        } else {
                            tvAddr2.setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }

    private static class FindAddressRunnable implements Runnable {

        private final Dot coordDot;
        private final Handler handler;

        public FindAddressRunnable(Dot coordDot, Handler handler) {
            this.coordDot = coordDot;
            this.handler = handler;
        }

        @Override
        public void run() {
            FindResult result = new FindResult();
            result.formatted_address = "定位中...";
            handler.obtainMessage(ShowAddressHandler.RC_FIND_ADDRESS, result).sendToTarget();

            try {
                Location location = GpsReceiver.getInstance().getLastLocationConverse(new GpsXYZ(coordDot.x, coordDot.y));
                BDGeocoderResult r;

                if (location != null && ((r = BDGeocoder.find(location)) != null) && r.result != null) {
                    result = r.result;
                } else {
                    result = new FindResult();
                    result.formatted_address = "地图上的位置";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            handler.obtainMessage(ShowAddressHandler.RC_FIND_ADDRESS, result).sendToTarget();
        }
    }
}
