package com.repair.zhoushan.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

/**
 * Base class for showing a locatable data set on map.
 */
public abstract class ShowAllLocatableCallback<T extends Locatable> extends BaseMapCallback {

    protected final BaseActivity activity;
    private final ArrayList<T> dataList;

    private final int markDrawableResId[] = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc,
            R.drawable.icon_markd, R.drawable.icon_marke, R.drawable.icon_markf, R.drawable.icon_markg,
            R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    public ShowAllLocatableCallback(BaseActivity activity, ArrayList<T> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (dataList == null || dataList.size() == 0) {
            return false;
        }
        BaseMapMenu menu = new ShowAllLocatableCallback<T>.ShowAllTaskLocMapMenu(mapGISFrame, activity, dataList);
        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();
    }

    private final class ShowAllTaskLocMapMenu extends BaseMapMenu {

        private final BaseActivity activity;
        private final ArrayList<T> taskList;

        private final Rect rect = new Rect();

        ShowAllTaskLocMapMenu(MapGISFrame mapGISFrame, BaseActivity activity, ArrayList<T> taskList) {
            super(mapGISFrame);

            this.activity = activity;
            this.taskList = taskList;

            boolean hasUsefulLoc = false;
            Dot dot;
            for (T item : taskList) {
                dot = item.getLocationDot();
                if (dot != null) {
                    if (!hasUsefulLoc) {
                        hasUsefulLoc = true;
                        rect.xMin = rect.xMax = dot.getX();
                        rect.yMin = rect.yMax = dot.getY();
                    }

                    // calc rect
                    if (dot.getX() < rect.xMin) {
                        rect.xMin = dot.getX();
                    } else if (dot.getX() > rect.xMax) {
                        rect.xMax = dot.getX();
                    }
                    if (dot.getY() < rect.yMin) {
                        rect.yMin = dot.getY();
                    } else if (dot.getY() > rect.yMax) {
                        rect.yMax = dot.getY();
                    }
                }
            }

            if (!hasUsefulLoc) {
                taskList = null;
            } else {
                Intent intent = new Intent(activity, MapGISFrame.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
            }
        }

        @Override
        public boolean onOptionsItemSelected() {

            mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.GONE);

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();
            // 显示地图界面
            mapGISFrame.showMainFragment(true);

            Bitmap bitmap = null;
            // 点数等于1或大于10个，使用同一个Tip图标；小于10个，使用带序号的Tip图标.
            boolean usageSequenceIcon = true;
            if (taskList.size() == 1 || taskList.size() > 10) {
                usageSequenceIcon = false;
                bitmap = BitmapFactory.decodeResource(
                        mapGISFrame.getResources(), R.drawable.default_generalsearch_poi_markpoint);
            }

            T dataItem;
            Dot dot;
            ArrayList<MmtAnnotation> annotationList = new ArrayList<>();
            for (int i = 0, length = taskList.size(); i < length; i++) {

                dataItem = taskList.get(i);
                dot = dataItem.getLocationDot();
                if (dot == null) {
                    continue;
                }

                if (usageSequenceIcon) {
                    bitmap = BitmapFactory.decodeResource(
                            mapGISFrame.getResources(), markDrawableResId[annotationList.size()]);
                }
                MmtAnnotation annotation = new MmtAnnotation("", dataItem.getAnnotationTitle(),
                        dataItem.getAnnotationDesc(), dot, bitmap);
                annotation.attrMap.put("ListIndex", String.valueOf(i));
                annotation.showAnnotationView();
                annotationList.add(annotation);
            }

            if (annotationList.size() == 1) {
                mapView.getAnnotationLayer().addAnnotation(annotationList.get(0));
                annotationList.get(0).showAnnotationView();
            } else {
                for (MmtAnnotation annotation : annotationList) {
                    mapView.getAnnotationLayer().addAnnotation(annotation);
                }
            }

            mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {

                @Override
                public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {

                    MmtAnnotation mmtAnnotation = (MmtAnnotation) annotationview.getAnnotation();
                    T st = taskList.get(Integer.parseInt(mmtAnnotation.attrMap.get("ListIndex")));

                    onAnnotationViewClick(st);
                }
            });

            // 跳转到制定外接矩形,并保留一定的空间间隙
            if (rect != null) {
                double offset = (rect.xMax - rect.xMin) / 4;
                mapView.zoomToRange(new Rect(rect.xMin - offset, rect.yMin - offset,
                        rect.xMax + offset, rect.yMax + offset), true);
            }
            mapView.refresh();

            return false;
        }

        @Override
        public View initTitleView() {

            View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.main_actionbar, null);
            ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText("任务定位");
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

        private void backActivity(boolean showDetail) {

            try {
                Intent intent = activity.getIntent();

                intent.setClass(mapGISFrame, activity.getClass());
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                mapGISFrame.startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(mapGISFrame);

                if (!showDetail) {
                    mapGISFrame.resetMenuFunction();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Handle map annotation view click event.
     * @param dataItem The data associated with the annotation.
     */
    protected abstract void onAnnotationViewClick(T dataItem);

//    private Dot mDot;
//
//    @Override
//    public Dot getLocationDot() {
//        if (mDot != null) {
//            return mDot;
//        }
//
//        if (!TextUtils.isEmpty(Position)) {
//            String[] xy = Position.split(",");
//            if (xy.length == 2) {
//                mDot = new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
//            }
//        }
//
//        return mDot;
//    }
//
//    @Override
//    public String getAnnotationTitle() {
//        return null;
//    }
//
//    @Override
//    public String getAnnotationDesc() {
//        return null;
//    }
}
