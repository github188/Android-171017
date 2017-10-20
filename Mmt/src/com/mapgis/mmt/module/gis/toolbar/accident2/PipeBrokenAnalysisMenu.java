package com.mapgis.mmt.module.gis.toolbar.accident2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.AssistAnnotation;
import com.mapgis.mmt.entity.DefaultMapViewAnnotationListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.module.gis.toolbar.accident2.entity.FeatureMetaGroup;
import com.mapgis.mmt.module.gis.toolbar.accident2.presenter.PipeBrokenAnalysisPresenter;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.BrokenUtil;
import com.mapgis.mmt.module.gis.toolbar.accident2.util.IconFactory;
import com.mapgis.mmt.module.gis.toolbar.accident2.view.IPipeBrokenAnalysisView;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.annotation.AnnotationView;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 供水爆管分析菜单
 */
public class PipeBrokenAnalysisMenu extends BaseMapMenu implements View.OnClickListener
        , MapView.MapViewTapListener, IPipeBrokenAnalysisView {
    private static final String TAG = "PipeBrokenAnalysisMenu";

    static final int CODE_RESULT_ACTIVITY = 748;
    static final String INVALIDATE_VALVE = "invalidate_valve";
    public static final String PARAM_ANALYSIS_RESULT = "analysis_result";

    private Dot mBrokenPoint;
    private String mInvalidateValve;
    private Rect mRect;
    private Rect mShotRect;
    private View mImgBack;
    private View mBottomBarView;
    private ProgressBar mProgressBar;
    private ImageView mIvResult;
    private ViewGroup mFirstView;
    private ViewGroup mSecondView;

    private PipeBrokenAnalysisPresenter mPresenter;
    private Handler mHandler;
    private IconFactory mIconFactory;
    private boolean isBack = true;

    public PipeBrokenAnalysisMenu(MapGISFrame mapGISFrame) {
        super(mapGISFrame);
        this.mHandler = new Handler();
        this.mIconFactory = IconFactory.create(super.mapGISFrame.getResources());
        this.mPresenter = new PipeBrokenAnalysisPresenter(this);
        this.mBottomBarView = initBottomBarView(R.layout.pipe_broken_bottom_view);
        this.mFirstView = (ViewGroup) mBottomBarView.findViewById(R.id.ll_before_broken);
        this.mSecondView = (ViewGroup) mBottomBarView.findViewById(R.id.ll_after_broken);

        this.mBottomBarView.findViewById(R.id.tv_live_repair).setOnClickListener(this);
        this.mBottomBarView.findViewById(R.id.cb_show_area).setOnClickListener(this);
    }

    public Dot getBrokenPoint() {
        return mBrokenPoint;
    }

    public void setBrokenPoint(Dot brokenPoint) {
        this.mBrokenPoint = brokenPoint;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect rect) {
        this.mRect = rect;
    }

    @Override
    public boolean onOptionsItemSelected() {
        return isLoadMapView();
    }

    @Override
    public View initTitleView() {
        LayoutInflater layoutInflater = mapGISFrame.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.pipe_accident_titlebar, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvTitle = (TextView) view.findViewById(R.id.tvPlanName);
        tvTitle.setText(R.string.title_broken_analysis_menu);
        mProgressBar = (ProgressBar) view.findViewById(R.id.searchProgressBar);
        mIvResult = (ImageView) view.findViewById(R.id.ivPlanDetail);
        mImgBack = view.findViewById(R.id.tvPlanBack);
        view.findViewById(R.id.tvTaskState).setVisibility(View.GONE);

        hidenIvAndProgress();
        this.mIvResult.setOnClickListener(this);
        this.mImgBack.setOnClickListener(this);
        this.mapView.setTapListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == this.mImgBack.getId()) {
            mapGISFrame.onBackPressed();
        } else if (id == this.mIvResult.getId()) {
            enterResultActivity();
        } else if (id == R.id.tv_live_repair) {
            prepShot();
        } else if (id == R.id.cb_show_area) {

        }
    }

    // 绘制截图去域
    private GraphicPolygon mShotPolygon;
    private PointF mStartPoint;
    private PointF mEndPoint;
    private Dot[] dots;

    /**
     * 准备截图
     * 思路：在截图之前抓取一份当前窗口的地图
     */
    private void prepShot() {
        mapGISFrame.showToast("请在地图上选择你要截取的部分！");
        if (mShotPolygon != null) {
            mapView.getGraphicLayer().removeGraphic(mShotPolygon);
            mapView.refresh();
        }

        hidenSecondView();
        this.mapView.setTouchListener(new MapView.MapViewTouchListener() {
            @Override
            public boolean mapViewTouch(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartPoint = new PointF(motionEvent.getX(), motionEvent.getY());
                        dots = new Dot[5];
                        dots[0] = mapView.viewPointToMapPoint(mStartPoint);
                        dots[4] = dots[0];
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        mEndPoint = new PointF(motionEvent.getX(), motionEvent.getY());
                        Dot temp = mapView.viewPointToMapPoint(mEndPoint);
                        dots[1] = new Dot(temp.getX(), dots[0].getY());
                        dots[2] = temp;
                        dots[3] = new Dot(dots[0].getX(), temp.getY());
                        if (mShotPolygon != null) {
                            mapView.getGraphicLayer().removeGraphic(mShotPolygon);
                        }
                        mShotPolygon = new GraphicPolygon(dots);
                        mShotPolygon.setBorderlineColor(Color.RED);
                        mShotPolygon.setBorderlineWidth(1);
                        mShotPolygon.setColor(Color.argb(80, 0, 0, 255));
//                        mShotPolygon.setColor(Color.TRANSPARENT);
                        mapView.getGraphicLayer().addGraphic(mShotPolygon);
                        mapView.refresh();
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            mapView.setTouchListener(null);
                            showSecondView();
                            // 开始截取屏幕
                            shotMap();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /*开始截取地图上选择的矩形区域
    * */
    private void shotMap() {
        if (dots == null) {
            return;
        }
        mapView.getGraphicLayer().removeGraphic(mShotPolygon);
//        final int color = mapView.getBackGroundColor();
//        mapView.setBackgroundColor(Color.WHITE);
        mapView.refresh();
        captureMap(/*color*/);
    }

    private void captureMap(/*int color*/) {
        PointF point = new PointF(Math.min(mStartPoint.x, mEndPoint.x), Math.min(mStartPoint.y, mEndPoint.y));
        float width = Math.abs(mEndPoint.x - mStartPoint.x);
        float height = Math.abs(mEndPoint.y - mStartPoint.y);
        try {
            // 确定需要截取的视图区域
            Bitmap bitmap = Bitmap.createBitmap((int) Math.floor(width) - 2, (int) Math.floor(height) - 2, Bitmap.Config.ARGB_8888);
            PipeBrokenAnalysisMenu.this.mapView.capture((int) Math.ceil(point.x) + 1, (int) Math.ceil(point.y) + 1, bitmap);
//            PipeBrokenAnalysisMenu.this.mapView.setBackgroundColor(color);

            File shotFile = BrokenUtil.getShotFile();
            if (shotFile.exists()) {
                // 保存图片
                FileOutputStream fos = new FileOutputStream(shotFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                Log.w(TAG, "shotMap: 截取地图成功！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void post(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    @Override
    public void mapViewTap(PointF pointF) {
        Dot brokenPoint = this.mapView.viewPointToMapPoint(pointF);
        if (!isEffective(brokenPoint)) {
            this.mapGISFrame.showToast(mapGISFrame.getString(R.string.noneffective_broken_point));
            return;
        }
        this.mapView.setTapListener(null);
        showProgress();
        hidenBottomView();
        // 绘制爆管点
        drawBrokenPoint(brokenPoint);
        // 开始进行爆管分析
        this.mBrokenPoint = brokenPoint;
        this.mPresenter.startPipeBrokenAnalysis(brokenPoint);
    }

    /**
     * 判断爆管点是否有效
     *
     * @return true爆管点在地图范围内，false不在地图范围内
     */
    public boolean isEffective(Dot dot) {
        if (dot == null) {
            return false;
        }

        Rect range = this.mapView.getMap().getRange();
        return dot.getX() <= range.getXMax() && dot.getX() >= range.getXMin()
                && dot.getY() <= range.getYMax() && dot.getY() >= range.getYMin();
    }

    @Override
    public Rect getMapRange() {
        return this.mapView.getMap().getRange();
    }

    @Override
    public void showBottomView() {
        this.mBottomBarView.setVisibility(View.VISIBLE);
        this.mFirstView.setVisibility(View.VISIBLE);
        this.mSecondView.setVisibility(View.GONE);
    }

    @Override
    public void hidenBottomView() {
        this.mBottomBarView.setVisibility(View.GONE);
        this.mFirstView.setVisibility(View.GONE);
//        this.mSecondView.setVisibility(View.VISIBLE);
    }

    public void hidenSecondView() {
        ViewPropertyAnimator.animate(this.mBottomBarView)
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(800)
                .start();
    }

    public void showSecondView() {
        ViewHelper.setAlpha(this.mBottomBarView, 0f);
        ViewHelper.setScaleX(this.mBottomBarView, 0f);
        ViewHelper.setScaleY(this.mBottomBarView, 0f);
        this.mBottomBarView.setVisibility(View.VISIBLE);
        this.mFirstView.setVisibility(View.GONE);
        this.mSecondView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator.animate(this.mBottomBarView)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .start();
    }

    @Override
    public void resetBrokenFunction() {
        clearMapView();
        this.showBottomView();
        this.hidenIvAndProgress();
        this.mInvalidateValve = "";
        this.mBrokenPoint = null;
        this.mRect = null;
        this.mPresenter = new PipeBrokenAnalysisPresenter(this);
        this.mapView.setTapListener(this);
    }

    @Override
    public void showToast(String msg) {
        this.mapGISFrame.showToast(msg);
    }

    @Override
    public void analysisSuccess(FeatureMetaGroup[] featureMetaArray) {
        this.hidenProgress();
//        this.showSecondView();
        this.hidenBottomView();

//        showResultOnMap(featureMetaArray);
    }

    /**
     * 显示分析结果
     *
     * @param featureMetaArray 爆管分析结果对象
     */
    @Override
    public void showResultOnMap(FeatureMetaGroup[] featureMetaArray) {
        mapView.getAnnotationLayer().removeAllAnnotations();
        for (FeatureMetaGroup featureMetaGroup :
                featureMetaArray) {
            mPresenter.showFeatrueMetaGroup(featureMetaGroup);
        }
    }

    @Override
    public void analysisFailed(String msg) {
        this.showToast(msg);
        // 重新选择爆管点
        this.resetBrokenFunction();
    }

    @Override
    public void drawBrokenPoint(Dot dot) {
        // 将手指点的位置的标注图形显示在地图上
        GraphicPoint tagGraphicPoint = new GraphicPoint();
        tagGraphicPoint.setColor(Color.RED);
        tagGraphicPoint.setSize(10);
        tagGraphicPoint.setPoint(dot);
        mapView.getGraphicLayer().addGraphic(tagGraphicPoint);
        mapView.refresh();
    }

    @Override
    public void showProgress() {
        mIvResult.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidenProgress() {
        mIvResult.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void hidenIvAndProgress() {
        mIvResult.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean isLoadMapView() {
        if (mapView == null || mapView.getMap() == null) {
            mapGISFrame.stopMenuFunction();
            return false;
        }
        return true;
    }

    @Override
    public MapView getMapView() {
        return this.mapView;
    }

    @Override
    public void clearMapView() {
        this.mapView.getAnnotationLayer().removeAllAnnotations();
        this.mapView.getGraphicLayer().removeAllGraphics();
        this.mapView.refresh();
    }

    @Override
    public boolean isCloseValve() {
        return mBottomBarView.findViewById(R.id.cb_close_valve).isSelected();
    }

    @Override
    public boolean isAllowCloseValve() {
        return mBottomBarView.findViewById(R.id.cb_allow_open_valve).isSelected();
    }

    @Override
    public void setAnnotationListener() {
        this.mapView.setAnnotationListener(new DefaultMapViewAnnotationListener() {
            @Override
            public void mapViewClickAnnotationView(MapView mapview, AnnotationView annotationview) {
                Annotation annotation = annotationview.getAnnotation();
                if (annotation instanceof AssistAnnotation) {
                    // 图层名称，和详细属性信息
                    String layerName = ((AssistAnnotation) annotation).getInfo();
                    String attributes = ((AssistAnnotation) annotation).getAttributes();
                    toDetailActivity(layerName, attributes);
                }
            }

            @Override
            public AnnotationView mapViewViewForAnnotation(MapView mapview, Annotation annotation) {
                AnnotationView annotationView = super.mapViewViewForAnnotation(mapview, annotation);
                annotationView.setPanToMapViewCenter(false);
                return annotationView;
            }
        });
    }

    @Override
    public void toDetailActivity(String layerName, String detailData) {
        Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);
        intent.putExtra("FragmentClass", DetailListFragment.class);
        intent.putExtra("layerName", layerName);
        intent.putExtra("graphicMapStr", detailData);
        intent.putExtra("needLoc", false);
        intent.putExtra("unvisiable_detail_fragment", true);
        mapGISFrame.startActivity(intent);
    }

    /**
     * 获取图标
     */
    @Override
    public Bitmap getBitmap(String type) {
        return this.mIconFactory.getBitmap(type);
    }

    @Override
    public void enterResultActivity() {
        Intent intent = new Intent(mapGISFrame, AnalysisResultActivity.class);
        if (AppManager.existActivity(AnalysisResultActivity.class)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        } /*else {
            ProgressDialog loadingProgressDialog = MmtProgressDialog.getLoadingProgressDialog(mapGISFrame, mapGISFrame.getString(R.string.msg_on_loading));
            loadingProgressDialog.show();
            intent.putExtra(PARAM_ANALYSIS_RESULT, this.mPresenter.getAnalysisResult());
            loadingProgressDialog.dismiss();
        }*/
        mapGISFrame.startActivityForResult(intent, CODE_RESULT_ACTIVITY);
        MyApplication.getInstance().startActivityAnimation(mapGISFrame);
    }

    @Override
    public boolean onActivityResult(int resultCode, Intent intent) {
        if (resultCode == CODE_RESULT_ACTIVITY) {
            if (BaseClassUtil.isNullOrEmptyString(this.mInvalidateValve)) {
                this.mInvalidateValve = intent.getStringExtra(INVALIDATE_VALVE);
            } else {
                this.mInvalidateValve += "," + intent.getStringExtra(INVALIDATE_VALVE);
            }

            Log.i(TAG, "设为失效的设备ElemID: " + this.mInvalidateValve);
            clearMapView();
            showProgress();
            this.mPresenter.stopPipeBrokenAnalysis();
            this.mPresenter = new PipeBrokenAnalysisPresenter(this);
            mPresenter.startPipeBrokenAnalysis(mBrokenPoint);
        }
        return true;
    }

    @Override
    public String getInvalidateValve() {
        if (BaseClassUtil.isNullOrEmptyString(this.mInvalidateValve)) {
            return "";
        }
        return this.mInvalidateValve;
    }

    @Override
    public void setBack(boolean isBack) {
        this.isBack = isBack;
    }

    @Override
    public boolean onBackPressed() {
        if (isBack) {
            this.mPresenter.stopPipeBrokenAnalysis();
            this.mPresenter = null;
            AppManager.finishActivity(AnalysisResultActivity.class);
            return super.onBackPressed();
        } else {
            isBack = true;
            enterResultActivity();
            return true;
        }
    }
}
