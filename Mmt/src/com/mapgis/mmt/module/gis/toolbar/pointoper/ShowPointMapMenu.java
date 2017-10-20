package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.place.SingleSearchResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.LocatorGeocodeResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.NavigationTimerCallback;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicStippleLine;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

public class ShowPointMapMenu extends BaseMapMenu {
    protected Context context;
    protected Dot loc;
    protected String title;
    protected String text;
    protected int pos;
    //是否是坐标
    protected boolean isxy = false;
    protected String locStr;
    protected String nearByStr;

    protected String tipStr = "无效位置";

    public ShowPointMapMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
    }

    public ShowPointMapMenu(MapGISFrame mapGISFrame, Context context, String loc, String title,
                            String text, int pos) {
        super(mapGISFrame);

        this.title = title;
        this.text = text;
        this.pos = pos;
        this.locStr = loc;
        try {
            this.context = context;

            if (!BaseClassUtil.isNullOrEmptyString(loc)) {
                if (isxy = loc.contains(",")) {
                    this.loc = new Dot(Double.valueOf(loc.split(",")[0]),
                            Double.valueOf(loc.split(",")[1]));
                } else {
                    addressLoc(loc);
                }

                Intent intent = new Intent(context, mapGISFrame.getClass());

                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                context.startActivity(intent);
            } else {
                Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 地址转坐标
     *
     * @param loc
     */
    public void addressLoc(final String loc) {
        new MmtBaseTask<Void, Void, Object>(context) {
            @Override
            protected Object doInBackground(Void... params) {
                return BDGeocoder.locFromAddressUtil(mapView, loc, 1);
            }

            @Override
            protected void onSuccess(Object result) {
                if (result == null) {
                    return;
                }
                try {
                    if (result instanceof BDPlaceSearchResult) {// 百度结果
                        BDPlaceSearchResult result1s = (BDPlaceSearchResult) result;
                        if (result1s != null && result1s.results != null && result1s.results.size() > 0) {
                            SingleSearchResult singleSearchResult = result1s.results.get(0);
                            nearByStr = singleSearchResult.address;
                            GpsXYZ xyz = singleSearchResult.getXyz();
                            ShowPointMapMenu.this.loc = new Dot(xyz.getX(), xyz.getY());
                            onOptionsItemSelected();
                        } else {
                            Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
                        }

                    } else if (result instanceof LocatorGeocodeResult) {// GIS服务器结果
                        LocatorGeocodeResult locatorGeocodeResult = (LocatorGeocodeResult) result;
                        if (locatorGeocodeResult != null && locatorGeocodeResult.candidates != null && locatorGeocodeResult.candidates.length > 0) {
                            LocatorGeocodeResult.Candidate candidate = locatorGeocodeResult.candidates[0];
                            nearByStr = candidate.address;
                            ShowPointMapMenu.this.loc = candidate.toDot();
                            onOptionsItemSelected();
                        } else {
                            Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
                        }

                    } else if (result instanceof LocalPlaceSearchResult) { // 本地db文件地名搜索结果
                        LocalPlaceSearchResult localPlaceSearchResult = (LocalPlaceSearchResult) result;
                        if (localPlaceSearchResult != null && localPlaceSearchResult.dataList != null && localPlaceSearchResult.dataList.size() > 0) { // 查询到结果时才重绘地图
                            LocalPlaceSearchResult.LocalPlaceSearchResultItem localPlaceSearchResultItem = localPlaceSearchResult.dataList.get(0);

                            nearByStr = localPlaceSearchResultItem.addressName;
                            ShowPointMapMenu.this.loc = new Dot(localPlaceSearchResultItem.loc_x, localPlaceSearchResultItem.loc_y);
                            onOptionsItemSelected();
                        } else {
                            Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    MyApplication.getInstance().showMessageWithHandle(tipStr);
                }
            }
        }.executeOnExecutor(MyApplication.executorService);
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

    @Override
    public boolean onBackPressed() {
        backActivity(false);

        return true;
    }

    protected View viewBar;

    @Override
    public boolean onOptionsItemSelected() {
        try {
            if (!onLocated()) return false;
            onGuided();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 定位
     * 位置无效时，返回false;
     * 否则返回true
     */
    protected boolean onLocated() {
        if (this.loc == null)
            return false;

        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

        mapView.getAnnotationLayer().removeAllAnnotations();

        Bitmap bitmap = BitmapFactory.decodeResource(
                mapGISFrame.getResources(), R.drawable.icon_select_point);

        Annotation annotation = new Annotation(title, text, this.loc,
                bitmap);

        mapView.getAnnotationLayer().addAnnotation(annotation);

        if (MyApplication.getInstance().getConfigValue("showDirectionLine", 0) > 0) {
            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            if (xyz.isUsefull()) {
                GraphicStippleLine directionLine = new GraphicStippleLine(xyz.convertToPoint(), this.loc);

                directionLine.setColor(Color.RED);
                directionLine.setLineWidth(10);

                mapView.getGraphicLayer().addGraphic(directionLine);
            }
        }

        mapView.panToCenter(this.loc, true);
        mapView.refresh();

        return true;
    }

    private NavigationTimerCallback navigationTimerCallback;

    /**
     * 底部导航栏
     */
    protected void onGuided() {
        viewBar = mapGISFrame.getLayoutInflater().inflate(
                R.layout.map_select_point_bar, null);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                -1, -2);

        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        viewBar.setLayoutParams(params1);

        TextView tvOther = (TextView) viewBar.findViewById(R.id.tvOther);
        tvOther.setVisibility(View.VISIBLE);
        tvOther.setText("导航");
        tvOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    boolean innerNaviMode = (MyApplication.getInstance().getConfigValue("NavigationMode", 0) == 1);
                    if (innerNaviMode) {
                        if (navigationTimerCallback == null) {
                            GpsXYZ startXYZ = GpsReceiver.getInstance().getLastLocalLocation();
                            GpsXYZ destXYZ = new GpsXYZ(loc.getX(), loc.getY());
                            navigationTimerCallback = new NavigationTimerCallback(startXYZ, destXYZ, true);
                            MyApplication.getInstance().sendToBaseMapHandle(navigationTimerCallback);
                        }
                    } else {
                        GisUtil.callOuterNavigationApp(mapGISFrame, loc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        TextView tvOk = (TextView) viewBar.findViewById(R.id.tvOk);
        if (this.pos >= 0) {
            tvOk.setText("详情");
            tvOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backActivity(true);
                }
            });
        } else {
            tvOk.setVisibility(View.GONE);
        }

        mapView.addView(viewBar);

        handler = new MyHandle(mapGISFrame, viewBar, "");
        if (isxy) {
            MyApplication.getInstance().submitExecutorService(findAddr);
        } else {
            ((TextView) viewBar.findViewById(R.id.tvAddr1))
                    .setText(locStr);
            if (TextUtils.isEmpty(nearByStr)) {
                viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);
            } else {
                ((TextView) viewBar.findViewById(R.id.tvAddr2)).setText("在"
                        + nearByStr + "附近");
            }
        }
    }

    protected void backActivity(boolean showDetail) {
        try {
            Intent intent = ((Activity) context).getIntent();

            if (this.pos >= 0 && !showDetail) {
                intent.putExtra("pos", -2);
            } else {
                intent.putExtra("pos", this.pos);
            }

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
                        .getLastLocationConverse(new GpsXYZ(loc.x, loc.y));
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

    public static class MyHandle extends Handler {
        private View viewBar;
        private MapGISFrame mapGISFrame;
        private String layerName;

        public MyHandle(MapGISFrame mapGISFrame, View viewBar, String layerName) {
            this.mapGISFrame = mapGISFrame;

            this.viewBar = viewBar;

            this.layerName = layerName;
        }

        public void setLayerName(String layerName) {
            this.layerName = layerName;
        }

        public String getLayerName() {
            return layerName;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {

                    case 2: {

                        FindResult result = (FindResult) msg.obj;

                        String address;
                        if (result.addressComponent != null) {
                            address = result.addressComponent.district
                                    + result.addressComponent.street
                                    + result.addressComponent.street_number;
                        } else {
                            address = result.formatted_address;
                        }
                        ((TextView) viewBar.findViewById(R.id.tvAddr1)).setText(address);

                        TextView tvAddr2 = (TextView) viewBar.findViewById(R.id.tvAddr2);
                        if (result.pois != null && result.pois.size() > 0) {
                            tvAddr2.setVisibility(View.VISIBLE);
                            tvAddr2.setText("在" + result.pois.get(0).name + "附近");
                        } else {
                            tvAddr2.setVisibility(View.GONE);
                        }
                        break;
                    }

                    case 3: {
                        if (TextUtils.isEmpty(layerName)) {
                            break;
                        }
                        Graphic graphic = (Graphic) msg.obj;
                        if (graphic != null) {
                            MapView mapView = mapGISFrame.getMapView();
                            mapView.getAnnotationLayer().removeAllAnnotations();
                            MmtAnnotation annotation = new MmtAnnotation(graphic, layerName, graphic.getAttributeValue("编号"), mapView.getCenterPoint(), null);
                            mapView.getAnnotationLayer().addAnnotation(annotation);
                            annotation.showAnnotationView();
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
