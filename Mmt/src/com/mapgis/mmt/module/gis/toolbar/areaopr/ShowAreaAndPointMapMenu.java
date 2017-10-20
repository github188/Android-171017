package com.mapgis.mmt.module.gis.toolbar.areaopr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.BDGeocoder;
import com.mapgis.mmt.module.gis.place.BDGeocoderResult;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.pointoper.ShowPointMapMenu;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.NavigationTimerCallback;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

public class ShowAreaAndPointMapMenu extends BaseMapMenu {

    private Context context;

    private String areaStr;
    private Dot coorDot;

    private boolean isCoorValid = false;
    private boolean isAreaValid = false;

    private String title;
    private String text;
    private int pos;

    /**
     *  0 : 正常显示不考虑到位反馈状态
     *  1 : 未到位
     *  2 : 已到位
     *  3 : 已反馈
     */
    private int state;

    /**
     * @param areaStr 区域
     * @param coorStr 坐标
     * @param state 到位反馈状态
     */
    public ShowAreaAndPointMapMenu(MapGISFrame mapGISFrame, Context context, String areaStr,
                                   String coorStr, int state, String title, String text, int pos) {
        super(mapGISFrame);
        this.context = context;
        this.title = title;
        this.text = text;
        this.pos = pos;
        this.state = state;

        if (!TextUtils.isEmpty(coorStr) && coorStr.contains(",")) {
            try {
                String[] dotStrs = coorStr.split(",");
                this.coorDot = new Dot(Double.valueOf(dotStrs[0]), Double.valueOf(dotStrs[1]));
                this.destXYZ = new GpsXYZ(coorDot.getX(), coorDot.getY());
                isCoorValid = true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(areaStr) && areaStr.contains("rings")) {
            this.areaStr = areaStr;
            isAreaValid = true;
            if (destXYZ == null) {
                Dot dot = AreaOprUtil.getFirstDotOfArea(areaStr);
                if (dot != null) {
                    destXYZ = new GpsXYZ(dot.getX(), dot.getY());
                }
            }
        }

        if (!isCoorValid && !isAreaValid) {
            MyApplication.getInstance().showMessageWithHandle("无效的坐标区域");
        } else {
            Intent intent = new Intent(context, MapGISFrame.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        }
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

            intent.removeExtra("area");

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

    private View viewBar;
    private String areaPaintTip;
    private ShowPointMapMenu.MyHandle handler;

    @Override
    public boolean onOptionsItemSelected() {

        if (isCoorValid || isAreaValid) {
            AreaOprUtil.clearArea(mapView); // Clear map.
        }

        try {
            if (isCoorValid) {
                mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

                int bitmapSrc = R.drawable.icon_select_point;
                if (state == 1) {
                    bitmapSrc = R.drawable.map_patrol_unarrived;
                }  else if (state == 2) {
                    bitmapSrc = R.drawable.map_patrol_arrived;
                } else if (state == 3) {
                    bitmapSrc = R.drawable.map_patrol_feedbacked;
                }

                Bitmap bitmap = BitmapFactory.decodeResource(mapGISFrame.getResources(), bitmapSrc);
                Annotation annotation = new Annotation(title, text, this.coorDot, bitmap);
                mapView.getAnnotationLayer().addAnnotation(annotation);
                mapView.panToCenter(this.coorDot, true);
                mapView.refresh();

                viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);
                RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);
                params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                viewBar.setLayoutParams(params1);

                TextView tvOther = (TextView) viewBar.findViewById(R.id.tvOther);
                tvOther.setVisibility(View.VISIBLE);
                tvOther.setText("导航");
                tvOther.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            navigate();
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

                handler = new ShowPointMapMenu.MyHandle(mapGISFrame, viewBar, "");
                MyApplication.getInstance().submitExecutorService(findAddr);
            }

            if (isAreaValid) {

                areaPaintTip = AreaOprUtil.painArea(mapView, areaStr);

                if (mapView == null || mapView.getMap() == null) {
                    mapGISFrame.stopMenuFunction();
                    return false;
                }

                if (!isCoorValid) {
                    mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);

                    viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);
                    viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);
                    viewBar.findViewById(R.id.tvOk).setVisibility(View.INVISIBLE);

                    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);
                    params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    viewBar.setLayoutParams(params1);

                    TextView tipView = (TextView) viewBar.findViewById(R.id.tvAddr1);
                    tipView.setText(TextUtils.isEmpty(areaPaintTip) ? AreaOprUtil.areatip : areaPaintTip);

                    mapView.addView(viewBar);
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private NavigationTimerCallback navigationTimerCallback;
    private GpsXYZ destXYZ;

    /**
     * 导航方式，两种：1.“NavigationMode”配为1，地图内导航；
     * <p>2.“NavigationMode”不配或配为0，调用百度导航； </p>
     */
    private void navigate() {

        boolean innerNaviMode = (MyApplication.getInstance().getConfigValue("NavigationMode", 0) == 1);
        if (innerNaviMode) {
            if (navigationTimerCallback == null) {
                GpsXYZ startXYZ = GpsReceiver.getInstance().getLastLocalLocation();
                navigationTimerCallback = new NavigationTimerCallback(startXYZ, destXYZ, true);
                MyApplication.getInstance().sendToBaseMapHandle(navigationTimerCallback);
            }
        } else {
            GisUtil.callOuterNavigationApp(mapGISFrame, coorDot);
        }
    }

    /**
     * Schedule for navigation.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (navigationTimerCallback == null) {
            return;
        }

        if (intent.hasExtra("xy")) {
            GpsXYZ xy = intent.getParcelableExtra("xy");
            navigationTimerCallback.updateLocations(xy, destXYZ, false);
        }
    }

    private Runnable findAddr = new Runnable() {
        @Override
        public void run() {

            FindResult result = new FindResult();
            result.formatted_address = "定位中...";
            handler.obtainMessage(2, result).sendToTarget();

            try {
                Location location = GpsReceiver.getInstance().getLastLocationConverse(new GpsXYZ(coorDot.x, coorDot.y));
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

            handler.obtainMessage(2, result).sendToTarget();
        }
    };
}
