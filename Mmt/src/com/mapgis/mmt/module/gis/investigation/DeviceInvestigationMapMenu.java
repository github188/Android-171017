package com.mapgis.mmt.module.gis.investigation;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.android.graphic.GraphicPolylin;
import com.zondy.mapgis.android.graphic.GraphicType;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 设备普查
 * 
 * @author meikai
 */
public class DeviceInvestigationMapMenu extends BaseMapMenu {

	static int SUCCESS = 0;
	static int NORESULT = 1;
	static int EXCEPTION = 2;

	public static final int RESULT_DEVICE_TYPE_SELECTED = 100;
	protected InvestigationAnnotationListener annotationListener;
	protected Annotation annotation = null;

	private ArrayList<String> visibleVectorLayerNames;
	MapLayer layer;
	Rect queryeEnvelope;
	Dot clickPoint;
	Graphic graphic;
	GraphicPolylin graLine;
	List<GraphicPoint> lineDevicePointArr;

	public DeviceInvestigationMapMenu(MapGISFrame mapGISFrame) {
		super(mapGISFrame);
	}

	@Override
	public boolean onOptionsItemSelected() {
		if (mapView == null || mapView.getMap() == null) {
			mapGISFrame.stopMenuFunction();
			return false;
		}

		Intent intent = new Intent(mapGISFrame, DeviceTypeSelect.class);
		mapGISFrame.startActivityForResult(intent, 0);

		return true;
	}

	@Override
	public boolean onActivityResult(int resultCode, Intent intent) {
		if (intent == null) {
			return true;
		}
		switch (resultCode) {
		case RESULT_DEVICE_TYPE_SELECTED:
			String deviceTypeStr = intent.getStringExtra("deviceType");
			annotationListener = new InvestigationAnnotationListener();
			mapView.setAnnotationListener(annotationListener);
			if (deviceTypeStr.equals("属性编辑")) {
				mapView.setTapListener(clickSearchListener);
			}
			if (deviceTypeStr.equals("点设备普查")) {
				mapView.setTapListener(pointDeviceListener);
			}
			if (deviceTypeStr.equals("线设备普查")) {
				annotationListener.setDeviceType(InvestigationAnnotationListener.DeviceType.LineDevice);

				// 取消 上一点 的 点击 事件
				mapGISFrame.findViewById(R.id.baseActionBarOtherImageView).setVisibility(View.VISIBLE);
				mapGISFrame.findViewById(R.id.baseActionBarOtherImageView).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (graLine != null && graLine.getPointCount() > 0) {
							// 删除 最后 一条 线
							graLine.removePoint(graLine.getPointCount() - 1);
							// 删除 最后一个点
							GraphicPoint delPoint = lineDevicePointArr.get(lineDevicePointArr.size() - 1);
							mapView.getGraphicLayer().removeGraphic(delPoint);
							lineDevicePointArr.remove(delPoint);

							if (graLine.getPoints() != null) {
								annotationListener.setClickDotList(Arrays.asList(graLine.getPoints()));
								// 刷新 气球 的 位置
								if (annotation == null) {
									annotation = new Annotation("选择", null, graLine.getPoint(graLine.getPointCount() - 1), null);
									mapView.getAnnotationLayer().addAnnotation(annotation);
									annotation.showAnnotationView();
								} else {
									mapView.getAnnotationLayer().removeAllAnnotations();
									annotation.dispose();
									annotation = null;
									annotation = new Annotation("选择", null, graLine.getPoint(graLine.getPointCount() - 1), null);
									mapView.getAnnotationLayer().addAnnotation(annotation);
									annotation.showAnnotationView();
								}
							} else {
								mapView.getAnnotationLayer().removeAllAnnotations();
								annotation.dispose();
								annotation = null;
							}
						}
						mapView.refresh();
					}
				});

				mapView.setTapListener(lineDeviceListener);
			}
			break;
		default:

			break;
		}

		return true;
	}

	/** 点设备 的 地图点击事件 */
	MapViewTapListener clickSearchListener = new MapViewTapListener() {
		@Override
		public void mapViewTap(PointF arg0) {
			try {
				mapView.getGraphicLayer().removeAllGraphics();
				mapView.getAnnotationLayer().removeAllAnnotations();

				clickPoint = mapView.viewPointToMapPoint(arg0);

				visibleVectorLayerNames = GisQueryUtil.getVisibleVectorLayerNames(mapView);

				// 点击查询的优先顺序为：点 > 线 > 区
				boolean isSuccess = searchTargetGeomLayer(GeomType.GeomPnt);

				if (!isSuccess) {
					isSuccess = searchTargetGeomLayer(GeomType.GeomLin);
				}

				if (!isSuccess) {
					isSuccess = searchTargetGeomLayer(GeomType.GeomReg);
				}

				processResult(isSuccess ? SUCCESS : NORESULT);
			} catch (Exception e) {
				e.printStackTrace();

				processResult(EXCEPTION);
			}
		}
	};

	/** 点设备 的 地图点击事件 */
	MapViewTapListener pointDeviceListener = new MapViewTapListener() {
		@Override
		public void mapViewTap(PointF arg0) {
			mapView.getGraphicLayer().removeAllGraphics();
			mapView.getAnnotationLayer().removeAllAnnotations();

			clickPoint = mapView.viewPointToMapPoint(arg0);

			annotationListener.setClickDot(clickPoint);

			GraphicPoint graPoint = new GraphicPoint(clickPoint, 5);
			graPoint.setColor(Color.RED);
			mapView.getGraphicLayer().addGraphic(graPoint);

			if (annotation == null) {
				annotation = new Annotation("选择", null, clickPoint, null);
				mapView.getAnnotationLayer().addAnnotation(annotation);
				annotation.showAnnotationView();
			} else {
				mapView.getAnnotationLayer().removeAllAnnotations();
				annotation.dispose();
				annotation = null;
				annotation = new Annotation("选择", null, clickPoint, null);
				mapView.getAnnotationLayer().addAnnotation(annotation);
				annotation.showAnnotationView();
			}
			mapView.refresh();
		}
	};

	/** 线设备 的 地图点击事件 */
	MapViewTapListener lineDeviceListener = new MapViewTapListener() {
		@Override
		public void mapViewTap(PointF arg0) {
			clickPoint = mapView.viewPointToMapPoint(arg0);

			if (lineDevicePointArr == null) {
				lineDevicePointArr = new LinkedList<GraphicPoint>();
			}

			GraphicPoint graPoint = new GraphicPoint(clickPoint, 5);
			graPoint.setColor(Color.RED);
			lineDevicePointArr.add(graPoint);
			mapView.getGraphicLayer().addGraphic(lineDevicePointArr.get(lineDevicePointArr.size() - 1));

			if (graLine == null) {
				graLine = new GraphicPolylin();
				graLine.setLineWidth(1);
				graLine.setColor(Color.RED);
				mapView.getGraphicLayer().addGraphic(graLine);
			}
			graLine.appendPoint(clickPoint);

			annotationListener.setClickDotList(Arrays.asList(graLine.getPoints()));

			if (annotation == null) {
				annotation = new Annotation("选择", null, clickPoint, null);
				mapView.getAnnotationLayer().addAnnotation(annotation);
				annotation.showAnnotationView();
			} else {
				mapView.getAnnotationLayer().removeAllAnnotations();
				annotation.dispose();
				annotation = null;
				annotation = new Annotation("选择", null, clickPoint, null);
				mapView.getAnnotationLayer().addAnnotation(annotation);
				annotation.showAnnotationView();
			}
			mapView.refresh();

		}
	};

	/**
	 * 搜索制定类型的图层
	 * 
	 * @param targetGeomType
	 * @return
	 */
	boolean searchTargetGeomLayer(GeomType targetGeomType) {
		LayerEnum layerEnum = mapView.getMap().getLayerEnum();
		layerEnum.moveToFirst();
		MapLayer layer;
		while ((layer = layerEnum.next()) != null) {

			if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(targetGeomType)) {
				continue;
			}

			Rect rect = null;

			if (queryeEnvelope == null) {// 20像素的缓冲半径的点击查询模拟
				rect = new Rect();

				rect.setXMin(clickPoint.x - mapView.getResolution(mapView.getZoom()) * 20);
				rect.setYMin(clickPoint.y - mapView.getResolution(mapView.getZoom()) * 20);
				rect.setXMax(clickPoint.x + mapView.getResolution(mapView.getZoom()) * 20);
				rect.setYMax(clickPoint.y + mapView.getResolution(mapView.getZoom()) * 20);
			} else {// 矩形查询
				rect = queryeEnvelope;
			}

			// 存储要素查询结果
			FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(rect),
					FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", 1);

			if (featurePagedResult.getTotalFeatureCount() > 0) {
				graphic = Convert.fromFeatureToGraphic(featurePagedResult.getPage(1).get(0), layer.getName());
				graphic.setAttributeValue("<图层名称>", layer.getName());
				return true;
			}
		}
		return false;
	}

	public void processResult(int result) {
		try {
			// this.mapGISFrame.setSupportProgressBarIndeterminateVisibility(false);

			if (result == SUCCESS) {
				mapView.setAnnotationListener(new AttributeEditAnnotationListener().setGraphic(graphic));

				Dot point = null;

				if (graphic.getGraphicType().equals(GraphicType.PointType)) {
					point = ((GraphicPoint) graphic).getPoint();
				} else if (graphic.getGraphicType().equals(GraphicType.PolylinType)) {
					Dot[] point2ds = ((GraphicPolylin) graphic).getPoints();

					point = getIntersectionInView(point2ds[0], point2ds[1]);

					if (point == null) {
						point = clickPoint;
					}
				} else if (graphic.getGraphicType().equals(GraphicType.PolygonType)) {
					point = clickPoint;
				}

				mapView.getGraphicLayer().addGraphic(graphic);

				String field = LayerConfig.getInstance().getConfigInfo(layer.getName()).HighlightField;

				String highlight = BaseClassUtil.isNullOrEmptyString(field) ? graphic.getAttributeValue("$图层名称$") : graphic
						.getAttributeValue(field);

				Annotation annotation = new Annotation(layer.getName(), highlight, point, null);

				mapView.getAnnotationLayer().addAnnotation(annotation);

				annotation.showAnnotationView();

			} else if (result == NORESULT) {
				mapView.getGraphicLayer().removeAllGraphics();

				GraphicPoint gra = new GraphicPoint(clickPoint, 5);
				gra.setColor(Color.RED);
				mapView.getGraphicLayer().addGraphic(gra);

				Toast.makeText(this.mapGISFrame, "此位置无设备", Toast.LENGTH_SHORT).show();
				// Intent intent = new Intent(mapGISFrame,
				// DeviceTypeSelect.class);
				// mapGISFrame.startActivityForResult(intent, 0);

			} else {
				Toast.makeText(this.mapGISFrame, "查询异常，详情查询日志文件", Toast.LENGTH_SHORT).show();
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

	@Override
	public View initTitleView() {
		// TODO Auto-generated method stub
		return null;
	}
}
