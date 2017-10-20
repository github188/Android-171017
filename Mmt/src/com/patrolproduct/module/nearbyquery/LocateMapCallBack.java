package com.patrolproduct.module.nearbyquery;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.pointoper.ShowPointMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Created by Comclay on 2016/11/14.
 * 定位
 */

public class LocateMapCallBack extends ShowMapPointCallback {
    protected Graphic graphic;
    protected BaseMapMenu mMenu;
    protected String titleAttName;
    protected String textAttName;
    protected ArrayList<Graphic> graphicList;

    public LocateMapCallBack(Graphic graphic, Context context, String title, String text, int pos) {
        super();
        this.context = context;
        this.graphic = graphic;
        this.title = title;
        this.text = text;
        this.pos = pos;
    }

    public LocateMapCallBack(Context context, String loc, String title, String text, int pos) {
        super(context, loc, title, text, pos);
    }

    public LocateMapCallBack(ArrayList<Graphic> graphicList, Context context, String titleAttName, String textAttName, int pos) {
        super();
        this.graphicList = graphicList;
        this.context = context;
        this.titleAttName = titleAttName;
        this.textAttName = textAttName;
        this.pos = pos;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (graphicList == null) {
            mMenu = new LocatedMapMenu(mapGISFrame, context, graphic, title, text, pos);
        } else {
            mMenu = new LocatedMapMenu(mapGISFrame, context, graphicList, titleAttName, textAttName, pos);
        }

        mapGISFrame.getFragment().menu = mMenu;

        return mMenu.onOptionsItemSelected();
    }

    class LocatedMapMenu extends ShowPointMapMenu {
        protected Graphic graphic;
        protected String titleAttName;
        protected String textAttName;
        protected ArrayList<Graphic> graphicList;

        public LocatedMapMenu(MapGISFrame mapGISFrame, Context context, ArrayList<Graphic> graphicList, String titleAttName, String textAttName, int pos) {
            super(mapGISFrame);
            this.graphicList = graphicList;
            this.context = context;
            this.titleAttName = titleAttName;
            this.textAttName = textAttName;
            this.pos = pos;
        }

        public LocatedMapMenu(MapGISFrame mapGISFrame, Context context, Graphic graphic, String title, String text, int pos) {
            super(mapGISFrame);
            this.graphic = graphic;
            this.context = context;
            this.title = title;
            this.text = text;
            this.pos = pos;
        }

        public LocatedMapMenu(MapGISFrame mapGISFrame, Context context, String loc, String title, String text, int pos) {
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
                } else {
                    Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        protected void backActivity(boolean showDetail) {
            try {
                mapGISFrame.onCustomBack();
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
        public boolean onOptionsItemSelected() {
            try {
                if (graphicList != null) {
                    if (!onLocatedAll()) return false;
                } else {
                    if (!onLocated()) return false;
                }
                onGuided();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        private boolean onLocatedAll() {
            if (graphicList == null || graphicList.size() == 0) return false;
            mapGISFrame.findViewById(com.mapgis.mmt.R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);
            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            mapView.setShowUserLocation(true);

            for (Graphic graphic : graphicList) {
                LinkedHashMap<String,String> mapData = QueryUtils.graphicToMap(graphic);
                title = mapData.get(titleAttName);
                text = mapData.get(textAttName);
                showGraphic(graphic);
            }
            mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                @Override
                public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                    Annotation annotation = annotationview.getAnnotation();
                    if (annotation instanceof MmtAnnotation) {
                        QueryUtils.enterDetailActivity(mapGISFrame
                                , QueryUtils.graphicToMap(((MmtAnnotation) annotation).graphic), false);
                    }
                }
            });

            mapView.refresh();
            return true;
        }

        @Override
        protected boolean onLocated() {
            if (graphic == null) {
                return false;
            }
            mapGISFrame.findViewById(com.mapgis.mmt.R.id.layoutMapToolbar).setVisibility(View.INVISIBLE);
            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();

            mapView.setShowUserLocation(true);

            showGraphic(graphic);

            mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
                @Override
                public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                    Annotation annotation = annotationview.getAnnotation();
                    if (annotation instanceof MmtAnnotation) {
                        QueryUtils.enterDetailActivity(mapGISFrame, QueryUtils.graphicToMap(graphic), false);
                    }
                }
            });

            mapView.refresh();
            return true;
        }

        private void showGraphic(Graphic graphic) {
            Dot dot = graphic.getCenterPoint();

            MmtAnnotation mmtAnnotation = new MmtAnnotation(
                    graphic
                    , title
                    , text
                    , dot
                    , BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_select_point));
            mmtAnnotation.setCanShowAnnotationView(true);
            mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);

            if (graphic instanceof GraphicPolylin) {
                // 管段
                graphic.setColor(Color.RED);
                ((GraphicPolylin) graphic).setLineWidth(5);
                mapView.getGraphicLayer().addGraphic(graphic);
            }
        }

        @Override
        protected void onGuided() {
        }
    }
}
