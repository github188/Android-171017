package com.mapgis.mmt.module.gis.toolbar.pointoper;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.place.FindResult;
import com.mapgis.mmt.module.gis.place.Poi;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2016/6/22.
 */
public class SelectPointHandle extends Handler {
    private MapGISFrame mapGISFrame;
    private View view;
    private View viewBar;
    protected String address;
    protected ArrayList<String> names;
    protected boolean isValid = false;
    private String layerName;

    protected SelectPointHandle(View view, View viewBar, MapGISFrame mapGISFrame, String layerName) {
        this.view = view;
        this.viewBar = viewBar;
        this.mapGISFrame = mapGISFrame;
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
                case 0:
                case 1:
                    view.startAnimation((Animation) msg.obj);
                    break;
                case 2:
                    FindResult result = (FindResult) msg.obj;

                    address = result.formatted_address;
                    isValid = result.addressComponent != null;

                    if (isValid)
                        address = result.addressComponent.district + result.addressComponent.street
                                + result.addressComponent.street_number;

                    ((TextView) viewBar.findViewById(R.id.tvAddr1)).setText(address);

                    this.names = new ArrayList<>();

                    if (result.pois != null && result.pois.size() > 0) {
                        viewBar.findViewById(R.id.tvAddr2).setVisibility(View.VISIBLE);
                        String name = result.pois.get(0).name;

                        ((TextView) viewBar.findViewById(R.id.tvAddr2)).setText("在" + name + "附近");

                        for (Poi poi : result.pois) {
                            this.names.add(poi.name);
                        }
                    } else
                        viewBar.findViewById(R.id.tvAddr2).setVisibility(View.GONE);

                    break;
                case 3: {
                    if (TextUtils.isEmpty(layerName)) {
                        break;
                    }
                    Graphic graphic = (Graphic) msg.obj;
                    if (graphic != null) {
                        MapView mapView = mapGISFrame.getMapView();
                        mapView.getAnnotationLayer().removeAllAnnotations();
                        Dot centerDot = graphic.getCenterPoint();
                        MmtAnnotation annotation = new MmtAnnotation(graphic, layerName, graphic.getAttributeValue("编号"), centerDot, null);
                        mapView.getAnnotationLayer().addAnnotation(annotation);

                        view.setVisibility(View.GONE);

                        annotation.showAnnotationView();
                        //mapView.panToCenter(centerDot, true);
                    }
                }
                break;
                case 4: {
                    mapGISFrame.getMapView().getAnnotationLayer().removeAllAnnotations();
                    view.setVisibility(View.VISIBLE);
                }
                break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}