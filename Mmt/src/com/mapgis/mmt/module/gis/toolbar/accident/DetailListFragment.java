package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailFragment;
import com.zondy.mapgis.android.annotation.Annotation;

public class DetailListFragment extends PipeDetailFragment {

    // 重写管件详情里面定位操作
    private ImageView btnLocate;

    // 重写返回按钮的事件
    private ImageView btnBack;

    @Override
    protected void afterViewCreated(View view) {

        btnLocate = (ImageView) view.findViewById(R.id.detail_loc_btn);
        btnBack = (ImageView) view.findViewById(R.id.detail_revert_btn);

        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_DETAIL_ACTIVITY | PipeAccidentMenu.state;

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAnnotationImg();

                Intent intent = new Intent(getActivity(), MapGISFrame.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(intent);
                MyApplication.getInstance().startActivityAnimation(getActivity());
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity baseActivity = (BaseActivity) getActivity();
                baseActivity.onBackPressed();
            }
        });
    }

    @Override
    public boolean onBackPressed() {

        if (!getArguments().getBoolean("list", false)
                && (PipeAccidentMenu.state & PipeAccidentMenu.STATE_SHOW_LIST_ACTIVITY) != 0) {
            Intent intent = new Intent(getActivity(), MapGISFrame.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            getActivity().finish();
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        PipeAccidentMenu.state = ~PipeAccidentMenu.STATE_SHOW_DETAIL_ACTIVITY & PipeAccidentMenu.state;
        super.onDestroy();
    }

    /**
     * 更改要定位的按钮的图标
     */
    private void changeAnnotationImg() {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                // 1，遍历地图上已经显示的结果
                // 找到要定位的点的Annotation对象,更改地图上对应位置的图标，并展开标注
                for (Annotation annotation : mapView.getAnnotationLayer().getAllAnnotations()) {
                    String id = annotation.getTitle();
                    if (id != null && id.equals(infos.get("ElemID"))) {
                        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.icon_gcoding);
                        annotation.setImage(bm);
                        annotation.showAnnotationView();
                        mapView.panToCenter(annotation.getPoint(), true);
                        mapView.refresh();

                        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_LOCATED_RESULT | PipeAccidentMenu.state;
                        PipeAccidentMenu.state = PipeAccidentMenu.STATE_SHOW_HANDLE_RESULT | PipeAccidentMenu.state;
                        return true;
                    }
                }
//
//                // 2，如果没有，则自己绘制出来
//                Annotation annotation = (listChilds.get(groupPosition).features)[childPosition].createAnnotation(
//                        infos.get("ElemID"),
//                        BitmapFactory.decodeResource(mapGISFrame.getResources(), R.drawable.icon_gcoding));
//                mapView.getAnnotationLayer().addAnnotation(annotation);
//                // 展开标注视图
//                annotation.showAnnotationView();
//                mapView.panToCenter(annotation.getPoint(), true);
//                mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
//                    @Override
//                    public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
//                        Annotation annotation = annotationview.getAnnotation();
//                        if (annotation instanceof AccidentAnnotation) {
//                            Intent intent = new Intent(mapGISFrame, DetailListActivity.class);
//
//                            intent.putExtra("layerName", getIntent().getStringExtra("layerName"));
//                            intent.putExtra("graphicMap", getIntent().getStringExtra("graphicMap"));
//                            intent.putExtra("graphicMapStr", getIntent().getStringExtra("graphicMapStr"));
//                            intent.putExtra("unvisiable_detail_fragment", true);
//                            mapGISFrame.startActivity(intent);
//
//                            MyApplication.getInstance().startActivityAnimation(DetailListActivity.this);
//                        }
//                    }
//                });
                return true;
            }
        });
    }
}
