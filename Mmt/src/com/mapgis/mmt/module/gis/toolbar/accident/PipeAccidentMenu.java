package com.mapgis.mmt.module.gis.toolbar.accident;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentFeature;
import com.zondy.mapgis.geometry.Dot;

import java.util.List;

/**
 * 爆管分析
 *
 * @author Administrator
 */
public class PipeAccidentMenu extends BaseMapMenu {
    /**
     * 环形进度滚动条
     */
    private ProgressBar progressBar;

    /**
     * 查询结果详情按钮，点击查看详情
     */
    private ImageView queryResultListImg;

    /**
     * 单击地图开始爆管分析监听器
     */
    private final AccidentPointSearchListener listener;

    public Dot tagMapDot;

    public PipeAccidentMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
        // listener = new AccidentPointSearchListener(handler, mapGISFrame);
        listener = new AccidentPointSearchListener(this, mapGISFrame);
    }

    @Override
    public boolean onOptionsItemSelected() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }

        // 还没有开始选点
        state = STATE_NOT_SELECT_POINT;

        mapView.setTapListener(listener);
        Toast.makeText(mapGISFrame, "在地图上点击设备点,对该设备进行爆管分析", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        return false;
    }

    @Override
    public View initTitleView() {
        View view = LayoutInflater.from(mapGISFrame).inflate(R.layout.pipe_accident_titlebar, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // 退出功能按钮
        view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapGISFrame.resetMenuFunction();
                mapGISFrame.onBackPressed();
            }
        });

        ((TextView) view.findViewById(R.id.tvPlanName)).setText("爆管分析");
        ((Button) view.findViewById(R.id.tvTaskState)).setText("①阀门 ②管段 ③水表 ④接水点 ⑤用户");
        ((Button) view.findViewById(R.id.tvTaskState)).setTextAppearance(mapGISFrame, R.style.default_text_small_purity);
        ((Button) view.findViewById(R.id.tvTaskState)).setTextColor(Color.WHITE);
        view.findViewById(R.id.tvTaskState).setAlpha(0.87f);
        progressBar = (ProgressBar) view.findViewById(R.id.searchProgressBar);
        queryResultListImg = (ImageView) view.findViewById(R.id.ivPlanDetail);

        // 隐藏底部地图的工具栏
        mapGISFrame.findViewById(R.id.layoutMapToolbar).setVisibility(View.GONE);

        return view;
    }

    /**
     * 显示滚动进度条，或者显示详情按钮
     *
     * @param isVisiable 是否显示进度条，true则显示进度条，否则显示详情按钮
     */
    public void showProgressBar(boolean isVisiable) {
        queryResultListImg.setVisibility(isVisiable ? View.INVISIBLE : View.VISIBLE);
        progressBar.setVisibility(isVisiable ? View.VISIBLE : View.GONE);
    }

    /**
     * 详情按钮控件
     *
     * @return 返回详情按钮
     */
    public ImageView getQueryResultListImg() {
        return queryResultListImg;
    }

    private AccidentQueryTask queryTask;

    /**
     * 开始进行爆管分析查询
     */
    public void startQuery() {
        queryTask = new AccidentQueryTask(mapGISFrame, this, null);
        queryTask.executeOnExecutor(MyApplication.executorService, tagMapDot);
    }

    /**
     * 开始进行爆管分析查询
     *
     * @param features 需要二次爆管分析的设备
     */
    public void startTwiceQuery(List<AccidentFeature> features) {
        new AccidentQueryTask(mapGISFrame, this, features).executeOnExecutor(MyApplication.executorService, tagMapDot);
    }

    /**
     * 进入到保管分析界面但是没有选取点或者选取的点无效
     */
    public static final int STATE_NOT_SELECT_POINT          = 1;
    public static final int STATE_SHOW_RESULT               = 1 << 1;
    public static final int STATE_SHOW_HANDLE_RESULT        = 1 << 2;
    public static final int STATE_SHOW_DETAIL_ACTIVITY      = 1 << 3;
    public static final int STATE_SHOW_LIST_ACTIVITY        = 1 << 4;
    public static final int STATE_SHOW_LOCATED_RESULT       = 1 << 5;
    public static int state = 0;

    /**
     * @return true 表示返回事件由自己处理
     * false 表示返回事件由MapGisFrame处理
     */
    @Override
    public boolean onBackPressed() {
        // 第一种情况，从详情页跳转到爆管分析界面
        if ((state & (STATE_SHOW_DETAIL_ACTIVITY | STATE_SHOW_LIST_ACTIVITY)) > 0) {
            List<Activity> list = AppManager.activityList;

            Intent intent = new Intent();
            for (Context context : list) {
                if (context instanceof PipeDetailActivity) {
                    intent.setClass(mapGISFrame, PipeDetailActivity.class);
                    intent.putExtra("FragmentClass", DetailListFragment.class);
//                    return false;
                }
                if (context instanceof AccidentCheckActivity && (state & STATE_SHOW_LIST_ACTIVITY) > 0) {
                    intent.setClass(mapGISFrame, AccidentCheckActivity.class);
//                    return false;
                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            mapGISFrame.startActivity(intent);
            return true;
        }

//        if ((state & STATE_SHOW_LOCATED_RESULT) == STATE_SHOW_LOCATED_RESULT){
//            queryTask.showResultOnMap();
//            state -= STATE_SHOW_LOCATED_RESULT;
//            return true;
//        }

        // 第二种情况，将页面的数据恢复到最初的状态
        if ((state & (STATE_SHOW_HANDLE_RESULT | STATE_SHOW_LOCATED_RESULT)) > 0) {
            state = state & ~STATE_SHOW_HANDLE_RESULT;

            // 只计算标注的数量
            int allCount = queryTask.getAccidentResult().getAllAnnotationCount()
                    - queryTask.getAccidentResult().line.totalRcdNum;
            int showCount = mapView.getAnnotationLayer().getAnnotationCount();
            if (showCount < allCount || (state & STATE_SHOW_LOCATED_RESULT) > 0) {
                state = state & ~STATE_SHOW_LOCATED_RESULT;
                queryTask.showResultOnMap();
                return true;
            }
        }

        /*
         * 在三种情况是已经退出了详情界面和爆管分析结果界面
         * ，现在要清除本次爆管分析结果，让用户重新选择点进行分析
         */
        if ((state & STATE_SHOW_RESULT) == STATE_SHOW_RESULT) {
            mapView.getAnnotationLayer().removeAllAnnotations();
            mapView.getGraphicLayer().removeAllGraphics();
            mapView.setTapListener(listener);
            mapView.refresh();
//            mapGISFrame.showToast("请重新选取位置进行爆管分析");
            state = STATE_NOT_SELECT_POINT;
            return true;
        }

		/*
         * 用户退出爆管分析功能，恢复到地图界面的原始状态
		 *
		 */
//		if (state == STATE_NOT_SELECT_POINT){
        state = 0;
        return super.onBackPressed();
//		}
//		state = 0;
//		return false;
    }
}
