package com.repair.zhoushan.module.devicecare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.R;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;

public class ShowAllTaskLocCallback extends BaseMapCallback {

    private BaseActivity activity;
    private ArrayList<ScheduleTask> taskList;
    private boolean isDoneList;

    public ShowAllTaskLocCallback(BaseActivity activity, ArrayList<ScheduleTask> taskList, boolean isDoneList) {
        this.activity = activity;
        this.taskList = taskList;
        this.isDoneList = isDoneList;
    }

    @Override
    public boolean handleMessage(Message msg) {

        BaseMapMenu menu = new ShowAllTaskLocMapMenu(mapGISFrame, activity, taskList);

        if (taskList == null) {
            return false;
        }

        mapGISFrame.getFragment().menu = menu;
        return menu.onOptionsItemSelected();

    }

    final class ShowAllTaskLocMapMenu extends BaseMapMenu {

        private BaseActivity activity;
        private ArrayList<ScheduleTask> taskList;

        private Rect rect = new Rect();
        private View viewBar;

        public ShowAllTaskLocMapMenu(MapGISFrame mapGISFrame, BaseActivity activity, ArrayList<ScheduleTask> taskList) {
            super(mapGISFrame);

            this.activity = activity;
            this.taskList = taskList;

            boolean hasUsefulLoc = false;
            Dot dot;
            for (ScheduleTask scheduleTask : taskList) {
                if (!TextUtils.isEmpty(scheduleTask.Position)) {

                    try {
                        String[] xy = scheduleTask.Position.split(",");

                        if (xy != null && xy.length == 2) {
                            dot = new Dot(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
                            scheduleTask.mDot = dot;

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
//            tvOk.setText("详情");
//            tvAddr1.setText("");
//            tvAddr2.setText("");
//            tvOk.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (null != tvOk.getTag()) {
//                        Intent intent = new Intent(activity, TaskDetailActivity.class);
//                        intent.putExtra("ListItemEntity", (ScheduleTask) tvOk.getTag());
//                        intent.putExtra("ComeFrom", TaskDetailActivity.Source.FromMap);
//                        activity.startActivityForResult(intent, DeviceCareListFragment.TASK_DETAIL_REQUEST_CODE);
//                    }
//                }
//            });

            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();
            // 显示地图界面
            mapGISFrame.showMainFragment(true);

            Bitmap bitmap = BitmapFactory.decodeResource(
                    mapGISFrame.getResources(), R.drawable.default_generalsearch_poi_markpoint); // map_patrol_unarrived

            ScheduleTask scheduleTask;
            for (int i = 0, length = taskList.size(); i < length; i++) {

                scheduleTask = taskList.get(i);
                if (scheduleTask.mDot == null) {
                    continue;
                }

                MmtAnnotation annotation = new MmtAnnotation("", scheduleTask.TaskCode, scheduleTask.BizName, scheduleTask.mDot, bitmap);
                annotation.attrMap.put("ListIndex", String.valueOf(i));
                mapView.getAnnotationLayer().addAnnotation(annotation);
            }

            mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {

//                @Override
//                public void mapViewClickAnnotation(MapView mapview, Annotation annotation) {
//                    super.mapViewClickAnnotation(mapview, annotation);
//                    ScheduleTask st = taskList.get(Integer.parseInt(((MmtAnnotation) annotation).attrMap.get("ListIndex")));
//                    tvOk.setTag(st);
//                    tvAddr1.setText(st.TaskCode);
//                    tvAddr2.setText(st.EquipmentType + " : " + st.BizName);
//                }

                @Override
                public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {

                    Annotation annotation = annotationview.getAnnotation();
                    if (!MmtAnnotation.class.isInstance(annotation)) {
                        return;
                    }

                    MmtAnnotation mmtAnnotation = (MmtAnnotation) annotationview.getAnnotation();
                    ScheduleTask st = taskList.get(Integer.parseInt(mmtAnnotation.attrMap.get("ListIndex")));

                    Intent intent = new Intent(activity, TaskDetailActivity.class);
                    intent.putExtra("ListItemEntity", st);
                    intent.putExtra("ComeFrom", TaskDetailActivity.Source.FromMap);
                    if (isDoneList) {
                        intent.putExtra("IsDone", true);
                    }
                    activity.startActivityForResult(intent, DeviceCareListFragment.TASK_DETAIL_REQUEST_CODE);
                }
            });

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
