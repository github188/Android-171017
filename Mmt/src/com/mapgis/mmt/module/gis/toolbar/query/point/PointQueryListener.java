package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.widget.fragment.SplitListViewFragment;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicType;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PointQueryListener implements MapViewTapListener {
    private final MapGISFrame mapGISFrame;
    private final MapView mapView;

    private Dot clickPoint;
    private ArrayList<String> visibleVectorLayerNames;
    private MapLayer layer;
    private Rect queryeEnvelope;

    private MmtAnnotationListener mmtAnnotationListener;

    static int SUCCESS = 0;
    static int NORESULT = 1;
    static int EXCEPTION = 2;

    int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    private LinkedHashMap<String, List<Graphic>> resultLinkedHashMap;
    private List<String> leftListVData;
    private List<List<String>> rightListVData;
    /**
     * 显示 模糊点击查询结果 的 Fragment
     */
    private SplitListViewFragment splitListViewFragment;
    /**
     * 左右边选中的位置
     */
    int leftSelectedPos = -1, rightSelectedPos = -1;
    private ImageView queryResultListImg;
    private double radiusMeter = 0;

    public PointQueryListener(MapGISFrame mapGISFrame, MapView mapView, ImageView queryResultListImg) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapView;
        this.queryResultListImg = queryResultListImg;
        this.resultLinkedHashMap = new LinkedHashMap<String, List<Graphic>>();
        this.leftListVData = new ArrayList<String>();
        this.rightListVData = new ArrayList<List<String>>();
    }

    public void setQueryeEnvelope(Rect queryeEnvelope) {
        this.queryeEnvelope = queryeEnvelope;
    }

    /**
     * 设置AnnotationView的点击事件
     */
    public void setPointQueryAnnotationListener(MmtAnnotationListener mmtAnnotationListener) {
        this.mmtAnnotationListener = mmtAnnotationListener;
    }

    @Override
    public void mapViewTap(PointF arg0) {
        try {
            if (mmtAnnotationListener != null && mmtAnnotationListener.getHideTapListener()) {
                mmtAnnotationListener.setHideTapListener(false);
                return;
            }

            resultGraphicCount = 0;
            leftListVData.clear();
            rightListVData.clear();

            mapView.getGraphicLayer().removeAllGraphics();
            mapView.getAnnotationLayer().removeAllAnnotations();

            clickPoint = mapView.viewPointToMapPoint(arg0);

            //默认查询半径10像素,以PC上默认的96dpi为基础的
            long radius = MyApplication.getInstance().getConfigValue("PointQueryRadiusPX", 10);
            int dpi = mapView.getResources().getDisplayMetrics().densityDpi;

            //转换为手机实际对应的查询范围，像素为单位
            radius = (long) (radius * (dpi / 96.0));

            //转换为手机实际对应的查询范围，米为单位
            radiusMeter = mapView.getResolution(mapView.getZoom()) * radius;

            //在这里画一个范围的查询框
            painQueryRect(clickPoint, radiusMeter);

            // 获取 经过 mobileconfig.json 的 PointQueryLayerFilter 值 过滤后的 可查 图层列表
            visibleVectorLayerNames = GisQueryUtil.getPointQueryVectorLayerNames(mapView);

            // 点击查询的优先顺序为：点 > 线 > 区
            searchTargetGeomLayer(GeomType.GeomPnt);

            if (resultGraphicCount == 0) {
                searchTargetGeomLayer(GeomType.GeomLin);
            }

            if (resultGraphicCount == 0) {
                searchTargetGeomLayer(GeomType.GeomReg);
            }

            if (resultGraphicCount > 0) {
                // 图层遍历 完成 后 显示 点击查询 结果
                initSplitListViewFragment();

                if (queryResultListImg != null) {
                    queryResultListImg.setVisibility(View.VISIBLE);
                }

                mapView.getAnnotationLayer().getAnnotation(0).showAnnotationView();
            } else {
                Toast.makeText(this.mapGISFrame, "未捕获到任何结果", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this.mapGISFrame, "查询异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void painQueryRect(Dot clickPoint, double radiusMeter) {
        Dot dot1 = new Dot(clickPoint.x - radiusMeter, clickPoint.y - radiusMeter);
        Dot dot2 = new Dot(clickPoint.x + radiusMeter, clickPoint.y - radiusMeter);
        Dot dot3 = new Dot(clickPoint.x + radiusMeter, clickPoint.y + radiusMeter);
        Dot dot4 = new Dot(clickPoint.x - radiusMeter, clickPoint.y + radiusMeter);

        GraphicPolygon graphic = new GraphicPolygon();

        graphic.setColor(Color.argb(125, 255, 0, 0));

        graphic.appendPoint(dot1);
        graphic.appendPoint(dot2);
        graphic.appendPoint(dot3);
        graphic.appendPoint(dot4);

        mapView.getGraphicLayer().addGraphic(graphic);

        mapView.refresh();
    }

    /**
     * 搜索制定类型的图层
     *
     * @param targetGeomType
     * @return
     */
    boolean searchTargetGeomLayer(GeomType targetGeomType) {
        try {
            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            while ((layer = layerEnum.next()) != null) {
                if (!layer.getIsVisible() || !visibleVectorLayerNames.contains(layer.getName())
                        || !layer.GetGeometryType().equals(targetGeomType))
                    continue;

                if (mapView.getZoom() <= 8 && resultGraphicCount > 0)//前八级地图默认只查询一层数据即可，之后再遍历每个图层
                    continue;

                Rect rect;

                if (queryeEnvelope == null) {// 20像素的缓冲半径的点击查询模拟
                    rect = new Rect();

                    rect.setXMin(clickPoint.x - radiusMeter);
                    rect.setYMin(clickPoint.y - radiusMeter);
                    rect.setXMax(clickPoint.x + radiusMeter);
                    rect.setYMax(clickPoint.y + radiusMeter);
                } else {// 矩形查询
                    rect = queryeEnvelope;
                }

                // 存储要素查询结果
                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(rect),
                        FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", mapView.getZoom() <= 8 ? 1 : 8);

                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    List<Graphic> graphicList = Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName());

                    showQueryResult(layer.getName(), graphicList);
                }
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    public void showResultListFragment() {
        splitListViewFragment.show(mapGISFrame.getSupportFragmentManager(), "");
    }

    private void initSplitListViewFragment() {
        splitListViewFragment = new SplitListViewFragment("查询结果", "图层", "设备信息", leftListVData, rightListVData);
        splitListViewFragment.setRightListItemSingleLine(true);
        splitListViewFragment.setCancelable(false);
        splitListViewFragment.setLeftLayoutWeight(4);
        splitListViewFragment.setRightLayoutWeight(6);
        splitListViewFragment.setSplitListViewPositiveClick(new SplitListViewFragment.SplitListViewPositiveClick() {
            @Override
            public void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos) {
                leftSelectedPos = leftPos;
                rightSelectedPos = rightPos;

                // String layerName = leftListValue.substring(0,
                // leftListValue.indexOf("("));
                Graphic itemClickGraphic = resultLinkedHashMap.get(leftListValue).get(rightPos);
                HashMap<String, String> graphicMap = new LinkedHashMap<String, String>();
                for (int m = 0; m < itemClickGraphic.getAttributeNum(); m++) {
                    graphicMap.put(itemClickGraphic.getAttributeName(m), itemClickGraphic.getAttributeValue(m));
                }

                Intent intent = new Intent(mapGISFrame, PipeDetailActivity.class);

                intent.putExtra("layerName", leftListValue);
                intent.putExtra("isSetResult", true);
                intent.putExtra("graphic", itemClickGraphic);
                intent.putExtra("graphicMap", graphicMap);
                intent.putExtra("graphicMapStr", new Gson().toJson(graphicMap));
                intent.putExtra("needLoc", false);

                mapGISFrame.startActivityForResult(intent, 0);
            }
        });
    }

    private int resultGraphicCount = 0;

    public void showQueryResult(String layerName, List<Graphic> graphicList) {
        try {
            if (graphicList == null || graphicList.size() == 0) {
                return;
            }

            if (mmtAnnotationListener == null) {
                mapView.setAnnotationListener(new MmtAnnotationListener());
            } else {
                mapView.setAnnotationListener(mmtAnnotationListener);
            }

            // resultGraphicCount += graphicList.size();
            Dot point = null;
            ArrayList<String> stringArrList = new ArrayList<String>();

            for (Graphic graphic : graphicList) {
                if (graphic.getGraphicType().equals(GraphicType.PointType)) {
                    point = ((GraphicPoint) graphic).getPoint();
                } else if (graphic.getGraphicType().equals(GraphicType.PolylinType)) {
                    Dot[] point2ds = ((GraphicPolylin) graphic).getPoints();

                    point = getIntersectionInView(point2ds[0], point2ds[1]);
                }

                if (point == null) {
                    point = clickPoint;
                }

                mapView.getGraphicLayer().addGraphic(graphic);

                String highlight = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

                if (BaseClassUtil.isNullOrEmptyString(highlight)) {// 未设置高亮字段则显示前3个属性
                    stringArrList.add(getGraphicInfo(graphic));
                } else {// 设置高亮字段则显示高亮字段
                    stringArrList.add(highlight + ":" + graphic.getAttributeValue(highlight));

                    highlight = graphic.getAttributeValue(highlight);
                }

                MmtAnnotation mmtAnnotation = new MmtAnnotation(graphic, layerName, highlight, point, BitmapFactory.decodeResource(
                        mapGISFrame.getResources(), resultGraphicCount >= icons.length ? R.drawable.icon_mark_pt
                                : icons[resultGraphicCount]));

                resultGraphicCount++;

                mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);
            }

            mapView.refresh();

            resultLinkedHashMap.put(layerName, graphicList);
            leftListVData.add(layerName);
            rightListVData.add(stringArrList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getGraphicInfo(Graphic graphic) {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            String key = graphic.getAttributeName(i);
            String value = graphic.getAttributeValue(i);

            strBuilder.append(key + ":" + value + ";");
        }

        return strBuilder.toString();
    }

    /**
     * 判断两条线是否相交 a 线段1起点坐标 b 线段1终点坐标 c 线段2起点坐标 d 线段2终点坐标 intersection 相交点坐标
     * reutrn 是否相交: 0 : 两线平行 -1 : 不平行且未相交 1 : 两线相交
     */
    private static Dot getIntersection(Dot a, Dot b, Dot c, Dot d) {
        Dot intersection = new Dot(0, 0);

        if (Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX()) + Math.abs(d.getY() - c.getY()) + Math.abs(d.getX() - c.getX()) == 0) {
            if ((c.getX() - a.getX()) + (c.getY() - a.getY()) == 0) {
                System.out.println("ABCD是同一个点！");
            } else {
                System.out.println("AB是一个点，CD是一个点，且AC不同！");
            }

            return null;
        }

        if (Math.abs(b.getY() - a.getY()) + Math.abs(b.getX() - a.getX()) == 0) {
            if ((a.getX() - d.getX()) * (c.getY() - d.getY()) - (a.getY() - d.getY()) * (c.getX() - d.getX()) == 0) {
                System.out.println("A、B是一个点，且在CD线段上！");
            } else {
                System.out.println("A、B是一个点，且不在CD线段上！");
            }

            return null;
        }

        if (Math.abs(d.getY() - c.getY()) + Math.abs(d.getX() - c.getX()) == 0) {
            if ((d.getX() - b.getX()) * (a.getY() - b.getY()) - (d.getY() - b.getY()) * (a.getX() - b.getX()) == 0) {
                System.out.println("C、D是一个点，且在AB线段上！");
            } else {
                System.out.println("C、D是一个点，且不在AB线段上！");
            }

            return null;
        }

        if ((b.getY() - a.getY()) * (c.getX() - d.getX()) - (b.getX() - a.getX()) * (c.getY() - d.getY()) == 0) {
            System.out.println("线段平行，无交点！");

            return null;
        }

        intersection.setX(((b.getX() - a.getX()) * (c.getX() - d.getX()) * (c.getY() - a.getY()) - c.getX() * (b.getX() - a.getX())
                * (c.getY() - d.getY()) + a.getX() * (b.getY() - a.getY()) * (c.getX() - d.getX()))
                / ((b.getY() - a.getY()) * (c.getX() - d.getX()) - (b.getX() - a.getX()) * (c.getY() - d.getY())));

        intersection.setY(((b.getY() - a.getY()) * (c.getY() - d.getY()) * (c.getX() - a.getX()) - c.getY() * (b.getY() - a.getY())
                * (c.getX() - d.getX()) + a.getY() * (b.getX() - a.getX()) * (c.getY() - d.getY()))
                / ((b.getX() - a.getX()) * (c.getY() - d.getY()) - (b.getY() - a.getY()) * (c.getX() - d.getX())));

        if ((intersection.getX() - a.getX()) * (intersection.getX() - b.getX()) <= 0
                && (intersection.getX() - c.getX()) * (intersection.getX() - d.getX()) <= 0
                && (intersection.getY() - a.getY()) * (intersection.getY() - b.getY()) <= 0
                && (intersection.getY() - c.getY()) * (intersection.getY() - d.getY()) <= 0) {

            System.out.println("线段相交于点(" + intersection.getX() + "," + intersection.getY() + ")！");

            return intersection; // '相交
        } else {
            System.out.println("线段相交于虚交点(" + intersection.getX() + "," + intersection.getY() + ")！");

            return null; // '相交但不在线段上
        }
    }

    Dot getIntersectionInView(Dot start2d, Dot end2d) {
        try {
            int ratio = 10000;

            Dot start = new Dot(start2d.x * ratio, start2d.y * ratio), end = new Dot(end2d.x * ratio, end2d.y * ratio);

            double x = 0, y = 0, count = 0;
            Rect envelope = mapView.getMap().getRange();

            double xmin = envelope.getXMin() * ratio, ymin = envelope.getYMin() * ratio, xmax = envelope.getXMax() * ratio, ymax = envelope
                    .getYMax() * ratio;

            double distanceX = Math.abs(xmax - xmin) * 0.05, distanceY = Math.abs(ymax - ymin) * 0.05;

            xmin += distanceX;
            xmax -= distanceX;
            ymin += distanceY;
            ymax -= distanceY;

            if (start.x > xmin && start.x < xmax && start.y > ymin && start.y < ymax) {
                x += start.getX();
                y += start.getY();

                count++;
            }

            if (end.x > xmin && end.x < xmax && end.y > ymin && end.y < ymax) {
                x += end.getX();
                y += end.getY();

                count++;
            }

            if (count > 1) {
                return new Dot(x / (2 * ratio), y / (2 * ratio));
            }

            Dot[] points = new Dot[4];

            points[0] = getIntersection(start, end, new Dot(xmin, ymin), new Dot(xmin, ymax));
            points[1] = getIntersection(start, end, new Dot(xmin, ymin), new Dot(xmax, ymin));
            points[2] = getIntersection(start, end, new Dot(xmax, ymin), new Dot(xmax, ymax));
            points[3] = getIntersection(start, end, new Dot(xmin, ymax), new Dot(xmax, ymax));

            int count2 = 0;
            double xx = 0, yy = 0;
            for (int i = 0; i < points.length; i++) {
                if (points[i] != null) {
                    xx += points[i].getX();
                    yy += points[i].getY();

                    count2++;
                }
            }

            if (count + count2 < 2) {
                return null;
            }

            if (count2 > 1) {
                x = xx;
                y = yy;
            } else {
                x += xx;
                y += yy;
            }

            if (x == 0 || y == 0) {
                return null;
            }

            return new Dot(x / (2 * ratio), y / (2 * ratio));
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
