package com.mapgis.mmt.module.gis.toolbar.query.point;

import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotation;
import com.mapgis.mmt.module.gis.toolbar.query.MmtAnnotationListener;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicLayer;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicType;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.List;

public class OnPointClickListener {
    protected final MapGISFrame mapGISFrame;
    protected final MapView mapView;

    protected Dot clickDot;
    protected ArrayList<String> visibleVectorLayerNames;

    protected int[] icons = {R.drawable.icon_marka, R.drawable.icon_markb, R.drawable.icon_markc, R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg, R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj};

    protected GraphicLayer pointClickLayer = new GraphicLayer();
    boolean isAnnotationListener = true;

    public OnPointClickListener(MapGISFrame mapGISFrame, MapView mapView, boolean isAnnotationListener) {
        this.mapGISFrame = mapGISFrame;
        this.mapView = mapView;
        this.isAnnotationListener = isAnnotationListener;

        pointClickLayer.setName("pointClickLayer");

        mapView.getGraphicLayers().add(pointClickLayer);
    }

    public OnPointClickListener(MapGISFrame mapGISFrame, MapView mapView) {
        this(mapGISFrame, mapView, true);
    }

    public void onStop() {
        pointClickLayer.removeAllGraphics();
        mapView.getGraphicLayers().remove(pointClickLayer);

        GisUtil.removeAnnotationByType(mapView, MmtAnnotation.POINT_QUERY);
    }

    MmtAnnotation firstAnnotation = null;

    public void onClick(PointF arg0) {
        try {
            firstAnnotation = null;
            resultGraphicCount = 0;

            pointClickLayer.removeAllGraphics();
            GisUtil.removeAnnotationByType(mapView, MmtAnnotation.POINT_QUERY);

            clickDot = mapView.viewPointToMapPoint(arg0);

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

            if (firstAnnotation != null) {
                firstAnnotation.showAnnotationView();
            } else {
                Toast.makeText(this.mapGISFrame, "未捕获到任何结果", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(this.mapGISFrame, "查询异常", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 搜索制定类型的图层
     *
     * @param targetGeomType 目标类型
     * @return 是否命中
     */
    public boolean searchTargetGeomLayer(GeomType targetGeomType) {
        try {
            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            MapLayer layer;

            while ((layer = layerEnum.next()) != null) {
                if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(targetGeomType))
                    continue;

                if (mapView.getZoom() <= 8 && resultGraphicCount > 0)//前八级地图默认只查询一层数据即可，之后再遍历每个图层
                    continue;

                Rect rect = new Rect();// 20像素的缓冲半径的点击查询模拟

                rect.setXMin(clickDot.x - mapView.getResolution(mapView.getZoom()) * 20);
                rect.setYMin(clickDot.y - mapView.getResolution(mapView.getZoom()) * 20);
                rect.setXMax(clickDot.x + mapView.getResolution(mapView.getZoom()) * 20);
                rect.setYMax(clickDot.y + mapView.getResolution(mapView.getZoom()) * 20);


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

    protected int resultGraphicCount = 0;

    public void showQueryResult(String layerName, List<Graphic> graphicList) {
        try {
            if (graphicList == null || graphicList.size() == 0) {
                return;
            }

            if (isAnnotationListener)
                mapView.setAnnotationListener(new MmtAnnotationListener());

            Dot point = null;

            for (Graphic graphic : graphicList) {
                if (graphic.getGraphicType().equals(GraphicType.PointType)) {
                    point = ((GraphicPoint) graphic).getPoint();
                } else if (graphic.getGraphicType().equals(GraphicType.PolylinType)) {
                    Dot[] point2ds = ((GraphicPolylin) graphic).getPoints();

                    point = getIntersectionInView(point2ds[0], point2ds[1]);
                }

                if (point == null) {
                    point = clickDot;
                }

                pointClickLayer.addGraphic(graphic);

                String highlight = LayerConfig.getInstance().getConfigInfo(layerName).HighlightField;

                if (!BaseClassUtil.isNullOrEmptyString(highlight)) {// 设置高亮字段则显示高亮字段
                    highlight = graphic.getAttributeValue(highlight);
                }

                MmtAnnotation mmtAnnotation = new MmtAnnotation(graphic, layerName, highlight, point, BitmapFactory.decodeResource(
                        mapGISFrame.getResources(), resultGraphicCount >= icons.length ? R.drawable.icon_mark_pt
                                : icons[resultGraphicCount]));

                mmtAnnotation.Type = MmtAnnotation.POINT_QUERY;

                if (firstAnnotation == null)
                    firstAnnotation = mmtAnnotation;

                resultGraphicCount++;

                mapView.getAnnotationLayer().addAnnotation(mmtAnnotation);
            }

            mapView.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断两条线是否相交 a 线段1起点坐标 b 线段1终点坐标 c 线段2起点坐标 d 线段2终点坐标 intersection 相交点坐标
     * reutrn 是否相交: 0 : 两线平行 -1 : 不平行且未相交 1 : 两线相交
     */
    protected static Dot getIntersection(Dot a, Dot b, Dot c, Dot d) {
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

    protected Dot getIntersectionInView(Dot start2d, Dot end2d) {
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

            for (Dot point : points) {
                if (point != null) {
                    xx += point.getX();
                    yy += point.getY();

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
