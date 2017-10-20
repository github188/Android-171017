package com.repair.shaoxin.water.repairtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.List;

public class ShowAllTaskOnMapCallback extends BaseMapCallback {

    private BaseActivity activity;
    private List<RepairTaskItemEntity> taskList;

    public ShowAllTaskOnMapCallback(BaseActivity activity, List<RepairTaskItemEntity> taskList) {
        this.activity = activity;
        this.taskList = taskList;
    }

    @Override
    public boolean handleMessage(Message msg) {

        BaseMapMenu menu = new ShowAllTaskOnMapCallback.ShowAllTaskLocMapMenu(mapGISFrame, activity, taskList);

        if (taskList == null) {
            return false;
        }

        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();

    }

    final class ShowAllTaskLocMapMenu extends BaseMapMenu {

        private BaseActivity activity;
        private List<RepairTaskItemEntity> taskList;

        private Rect rect = new Rect();
        private View viewBar;

        ShowAllTaskLocMapMenu(MapGISFrame mapGISFrame, BaseActivity activity, List<RepairTaskItemEntity> taskList) {
            super(mapGISFrame);

            this.activity = activity;
            this.taskList = taskList;

            boolean hasUsefulLoc = false;
            Dot dot;
            for (RepairTaskItemEntity task : taskList) {
                if (!TextUtils.isEmpty(task.patrolPosition)) {

                    try {
                        String[] xy = task.patrolPosition.split(",");

                        if (xy.length == 2) {
                            dot = new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
                            task.mDot = dot;

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
                    } catch (Exception e) {
                        e.printStackTrace();
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

//            viewBar = mapGISFrame.getLayoutInflater().inflate(R.layout.map_select_point_bar, null);
//            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(-1, -2);
//            params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            viewBar.setLayoutParams(params1);
//            mapView.addView(viewBar);
//
//            final TextView tvOk = (TextView) viewBar.findViewById(R.id.tvOk);
//            final TextView tvAddr1 = (TextView) viewBar.findViewById(R.id.tvAddr1);
//            final TextView tvAddr2 = (TextView) viewBar.findViewById(R.id.tvAddr2);
//            tvOk.setVisibility(View.GONE);
//            tvAddr1.setText("");
//            tvAddr2.setText("");

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();
            // 显示地图界面
            mapGISFrame.showMainFragment(true);

            Bitmap bitmap = BitmapFactory.decodeResource(
                    mapGISFrame.getResources(), R.drawable.ic_pin_red); // map_patrol_unarrived

            RepairTaskItemEntity task;
            for (int i = 0, length = taskList.size(); i < length; i++) {

                task = taskList.get(i);
                if (task.mDot == null) {
                    continue;
                }

                MmtAnnotation annotation = new MmtAnnotation("", task.no, task.eventSource, task.mDot, bitmap);
                annotation.attrMap.put("ListIndex", String.valueOf(i));
                mapView.getAnnotationLayer().addAnnotation(annotation);
            }


            // 跳转到制定外接矩形,并保留一定的空间间隙
            if (rect != null) {
                double offset = (rect.xMax - rect.xMin) / 4;
                mapView.zoomToRange(new Rect(rect.xMin - offset, rect.yMin - offset, rect.xMax + offset, rect.yMax + offset), true);
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
                    // mapView.removeView(viewBar);
                    mapGISFrame.resetMenuFunction();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
