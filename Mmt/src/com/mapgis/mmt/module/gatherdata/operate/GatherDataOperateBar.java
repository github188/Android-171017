package com.mapgis.mmt.module.gatherdata.operate;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.CacheUtils;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gatherdata.GatherDataLayer;
import com.mapgis.mmt.module.gatherdata.GatherDataUtils;
import com.mapgis.mmt.module.gatherdata.GatherElementBean;
import com.mapgis.mmt.module.gatherdata.GatherProjectBean;
import com.mapgis.mmt.module.gatherdata.operate.bar.GatherDataForAreaBar;
import com.mapgis.mmt.module.gatherdata.operate.bar.GatherDataForLineBar;
import com.mapgis.mmt.module.gatherdata.operate.bar.GatherDataForPointBar;
import com.mapgis.mmt.module.gatherdata.report.AttrEditDialogFragment;
import com.mapgis.mmt.module.gatherdata.report.AttrEditDialogFragment.OnButtonClickListener;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.graphic.GraphicPolygon;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.android.mapview.MapView.MapViewTapListener;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.Rect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 底部工具栏<br>
 * edit by WL - 20150525：采集后的数据，作为一次事件上报。
 * 
 */
public abstract class GatherDataOperateBar extends LinearLayout implements
		MapViewTapListener {
	protected MapGISFrame mapGISFrame;

	protected GatherProjectBean projectBean;

	protected MapView mapView;

	/** 提交的点击事件监听 */
	private OnReportClickListener onReportClickListener;

	/** 上次填写设备图层缓存标识 */
	public static final String GATHER_DATA_LAST_LAYER = "GATHER_DATA_LAST_LAYER";

	/** 上次 <strong>点</strong> 图层选取的图层标识 */
	public static final String GATHER_DATA_LAST_LINE_LAYER = "GATHER_DATA_LAST_LINE_LAYER";

	/** 上次 <strong>线</strong> 图层选取的图层标识 */
	public static final String GATHER_DATA_LAST_POINT_LAYER = "GATHER_DATA_LAST_POINT_LAYER";

	public static final String GATHER_DATA_ATTR = "GATHER_DATA_ATTR";

	public static GatherDataOperateBar getOperateBar(MapGISFrame mapGISFrame,
			GatherProjectBean projectBean) {
		if (projectBean.Type.equals("单点采集")) {
			return new GatherDataForPointBar(mapGISFrame, projectBean);
		} else if (projectBean.Type.equals("单线采集")) {
			return new GatherDataForLineBar(mapGISFrame, projectBean);
		} else {
			return new GatherDataForAreaBar(mapGISFrame, projectBean);
		}
	}

	public GatherDataOperateBar(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public GatherDataOperateBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GatherDataOperateBar(MapGISFrame mapGISFrame,
			GatherProjectBean projectBean) {
		this(mapGISFrame.getApplicationContext(), null);

		this.mapGISFrame = mapGISFrame;
		this.mapView = mapGISFrame.getMapView();
		this.projectBean = projectBean;

		registTagListener();

		initSelf();
	}

	/** 初始化自身，并赋予布局参数 */
	private void initSelf() {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		setMinimumHeight(DimenTool.dip2px(mapGISFrame, 50));
		setOrientation(LinearLayout.HORIZONTAL);
	}

	/** 初始化自身控件 */
	protected abstract void initToolBar();

	/** 重置功能 */
	public void resetProject() {

		if (projectBean == null || projectBean.elementBeans == null)
			return;

		projectBean.addressList.clear();
		projectBean.address = "";
		projectBean.dot = null;

		int size = projectBean.elementBeans.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				// deletePoint 内部会删除elementBean 所以这里每次都取第0个
				deletePoint(projectBean.elementBeans.get(0));
			}
		}
		mapView.getGraphicLayer().removeAllGraphics();
		mapView.refresh();

		initToolBar();
	}

	/**
	 * 点击属性对话框中移图按钮的相应事件
	 */
	public void onMoveButtonClick(GatherElementBean elementBean) {
		elementBean.canMove = true;
	}

	/** 注册地图点击事件：点击Graphic弹出属性编辑框 */
	public void registTagListener() {
		mapView.setTapListener(this);
	}

	/**
	 * 获取标靶View的中心点
	 * 
	 * @return 中心点坐标
	 */
	protected PointF getTargetViewCenterPoint() {
		float x = mapView.getWidth() / 2;
		float y = mapView.getHeight() / 2;

		return new PointF(x, y);
	}

	/**
	 * 在标靶中心点上添加Graphic, 添加背景图和标注
	 */
	protected GatherElementBean addElementBean() {
		PointF pointF = getTargetViewCenterPoint();

		// 获取绘制标注点的地图坐标
		Dot mapDot = mapView.viewPointToMapPoint(pointF);

		// 创建标注对象
		projectBean.LastSN = projectBean.LastSN + 1;

		String index = projectBean.LastSN + "";

		GatherElementBean elementBean = new GatherElementBean(index, mapDot,
				projectBean.ID);
		elementBean.GeomType = "点";
		elementBean.canMove = false;

		mapView.getGraphicLayer().addGraphic(elementBean.createGraphicPoint());
		mapView.getGraphicLayer().addGraphic(elementBean.createGraphicText());

		mapView.refresh();

		// 将标注加到缓存中
		projectBean.elementBeans.add(elementBean);

		return elementBean;
	}

	/**
	 * 移动Graphic
	 */
	protected GatherElementBean movePoint(GatherElementBean bean) {
		if (bean == null) {
			return null;
		}

		PointF pointF = getTargetViewCenterPoint();

		mapView.getGraphicLayer().removeAllGraphics();

		// 获取绘制标注点的地图坐标
		bean.mapDot = mapView.viewPointToMapPoint(pointF);

		bean.NewGeo = Convert.FormatDouble(bean.mapDot.x) + ","
				+ Convert.FormatDouble(bean.mapDot.y);

		mapView.getGraphicLayer().addGraphic(bean.createGraphicPoint());
		mapView.getGraphicLayer().addGraphic(bean.createGraphicText());

		mapView.refresh();

		bean.canMove = false;

		return bean;
	}

	/**
	 * 移动Graphic到指定点
	 */
	protected GatherElementBean moveToPoint(GatherElementBean bean,
			Dot targetDot) {
		if (bean == null) {
			return null;
		}

		mapView.getGraphicLayer().removeAllGraphics();

		// 获取绘制标注点的地图坐标
		bean.mapDot = targetDot;

		bean.NewGeo = Convert.FormatDouble(bean.mapDot.x) + ","
				+ Convert.FormatDouble(bean.mapDot.y);

		mapView.getGraphicLayer().addGraphic(bean.createGraphicPoint());
		mapView.getGraphicLayer().addGraphic(bean.createGraphicText());

		mapView.refresh();

		bean.canMove = false;

		return bean;
	}

	/**
	 * 删除指定点
	 * 
	 * @param mark
	 *            要删除的标注
	 */
	protected void deletePoint(GatherElementBean elementBean) {

		if (elementBean == null
				|| !projectBean.elementBeans.contains(elementBean)) {
			return;
		}
		// 删除关联的点和线
		projectBean.elementBeans.remove(elementBean);

		// 重新获取最大编号
		int maxSN = 0;

		for (GatherElementBean bean : projectBean.elementBeans) {
			if (bean.GeomType.equals("点")) {
				maxSN = Math.max(maxSN, Integer.valueOf(bean.ElemSN));
			}
		}

		projectBean.LastSN = maxSN;
	}

	/**
	 * 获取采集反馈项信息，并弹出可编辑对话框
	 * 
	 * @param type
	 *            类型：点，线
	 * @param isAdd
	 *            是否是新增点，用来判断取消事件是否删除改点
	 */
	public void executeAttr(GatherElementBean elementBean, boolean isAdd) {
		if (elementBean == null) {
			return;
		}

		// 优选读取缓存的反馈项，未读取到，则从网络获取
		String attrStr = CacheUtils.getInstance(mapGISFrame).get(
				GATHER_DATA_ATTR);

		if (BaseClassUtil.isNullOrEmptyString(attrStr)) {
			new GetGatherDataLayerTask(elementBean, isAdd).execute();
		} else {
			List<GatherDataLayer> layers = new Gson().fromJson(attrStr,
					new TypeToken<List<GatherDataLayer>>() {
					}.getType());
			showAttrEditFragment(elementBean, layers, isAdd);
		}
	}

	public void executeAttr(GatherElementBean elementBean, boolean isAdd,
			Dot dot) {
		if (elementBean == null) {
			return;
		}

		// 优选读取缓存的反馈项，未读取到，则从网络获取
		String attrStr = CacheUtils.getInstance(mapGISFrame).get(
				GATHER_DATA_ATTR);

		if (BaseClassUtil.isNullOrEmptyString(attrStr)) {
			new GetGatherDataLayerTask(elementBean, isAdd).execute();
		} else {
			List<GatherDataLayer> layers = new Gson().fromJson(attrStr,
					new TypeToken<List<GatherDataLayer>>() {
					}.getType());
			showAttrEditFragment(elementBean, layers, isAdd);
		}
	}

	/**
	 * 显示属性编辑框
	 * 
	 * @param elementBean
	 * @param attrs
	 *            属性列表
	 * @param isAdd
	 *            是否是新增点，用来判断取消事件是否删除改点
	 */
	private void showAttrEditFragment(final GatherElementBean elementBean,
			List<GatherDataLayer> layers, final boolean isAdd) {

		List<GatherDataLayer> pointLayers = new ArrayList<GatherDataLayer>();
		List<GatherDataLayer> lineLayers = new ArrayList<GatherDataLayer>();

		for (GatherDataLayer layer : layers) {
			if (layer.Type.equals("线类型")) {
				lineLayers.add(layer);
			} else if (layer.Type.equals("点类型")) {
				pointLayers.add(layer);
			}
		}

		final AttrEditDialogFragment fragment = new AttrEditDialogFragment(
				"图形<" + elementBean.ElemSN + ">的属性编辑", elementBean,
				elementBean.GeomType.equals("点") ? pointLayers : lineLayers,
				projectBean);

		fragment.setCancelable(false);
		fragment.setCanEdit(!elementBean.isCaptureDot);

		fragment.setOnButtonClickListener(new OnButtonClickListener() {

			@Override
			public void onMoveClick() {
				mapGISFrame.showToast("对准新位置后，点击<描点>将点移动到指定位置");
				onMoveButtonClick(elementBean);
				
				//点移动也保存位置
				int size = projectBean.elementBeans.size();
				if (size == 1) {
					// 记录工程的坐标,用第一个点的位置作为工程的位置
					projectBean.dot = projectBean.elementBeans.get(0).mapDot;
					projectBean.address = projectBean.elementBeans.get(0).address;
				}
			}

			@Override
			public void onCancelClick() {
				if (isAdd) {
					deletePoint(elementBean);
					initToolBar();
					projectBean.showOnMap(mapView);
				}
			}

			@Override
			public void onOkClick() {

				if (elementBean.GeomType.equals("线")) {
					return;
				}

				int size = projectBean.elementBeans.size();
				if (size == 1) {
					// 记录工程的坐标,用第一个点的位置作为工程的位置
					projectBean.dot = projectBean.elementBeans.get(0).mapDot;
					projectBean.address = projectBean.elementBeans.get(0).address;
					return;
				}
				int dotSize = 0;
				int lineSize = 0;
				for (int i = 0; i < size; i++) {
					if (projectBean.elementBeans.get(i).GeomType.equals("点")) {
						dotSize++;
					}
					if (projectBean.elementBeans.get(i).GeomType.equals("线")) {
						lineSize++;
					}
				}
				// 两点一线，且可以确定这两点连接该线。单线已连接，不用划线。
				if (dotSize == 2 && lineSize == 1) {
					return;
				}
				// 需要划线
				if (dotSize - lineSize == 2) {
					// 有多个点，需要连接该点和前一个点
					GatherElementBean startBean = projectBean.elementBeans
							.get(size - 2);
					GatherElementBean newBean = projectBean.elementBeans
							.get(size - 1);

					GatherElementBean lineBean = new GatherElementBean(
							startBean.ElemSN + "-" + newBean.ElemSN,
							startBean.ID + "," + newBean.ID, newBean.ProjectID);
					projectBean.elementBeans.add(lineBean);

					projectBean.showOnMap(mapView);
					// 直接连点 没有专门描线的按钮，这里点击取消也不能取消该线，当然了，当为线时根本没有取消按钮
					executeAttr(lineBean, false);
				}
			}
		});

		fragment.show(mapGISFrame.getSupportFragmentManager(), "");
	}

	/** 将地图移动到project所在地 */
	public void zoomToProject() {
		GraphicPolygon polygon = new GraphicPolygon();

		if (projectBean != null && projectBean.elementBeans != null
				&& projectBean.elementBeans.size() > 0) {

			for (GatherElementBean bean : projectBean.elementBeans) {

				if (bean.mapDot != null && bean.GeomType.equals("点")) {
					polygon.appendPoint(bean.mapDot);
				}
			}

			Rect rect = polygon.getBoundingRect();

			rect = new Rect(rect.xMin - 50, rect.yMin - 50, rect.xMax + 50,
					rect.yMax + 50);

			if (rect != null) {
				mapView.zoomToRange(rect, true);
			}
		}
	}

	/**
	 * 点击查询，查询结束后
	 * 
	 * @param attrMap
	 *            设备的属性信息
	 */
	protected void onCaptureFinished(LinkedHashMap<String, String> attrMap) {
		if (attrMap == null || attrMap.size() == 0) {
			mapGISFrame.showToast("捕获失败,未查询到设备信息");
			return;
		} else {
			mapGISFrame.showToast("捕获成功");
		}
	}

	/**
	 * 上报信息
	 * 
	 * @param isUpdate
	 *            是否是修改操作 。true修改数据，则无论何种情况，都不需要弹出承办人；false新增数据
	 */
	public void reportGatherResult() {
		if (projectBean == null || projectBean.elementBeans == null
				|| projectBean.elementBeans.size() == 0) {
			mapGISFrame.showToast("没有数据");
			return;
		}

		new ReportData().executeOnExecutor(MyApplication.executorService);

	}

	@Override
	public void mapViewTap(PointF arg0) {

		int isVisible = mapGISFrame.getMapView()
				.findViewWithTag("MapViewScreenView").getVisibility();
		if (isVisible == View.GONE) {
			return;
		}

		MapView mapView = mapGISFrame.getMapView();

		// 从后往前遍历，即先遍历点，在遍历线
		for (int i = mapView.getGraphicLayer().getGraphicCount() - 1; i >= 0; i--) {

			Graphic graphic = mapView.getGraphicLayer().getGraphic(i);

			if (mapView.graphicHitTest(graphic, arg0.x, arg0.y)) {

				String SN = graphic.getAttributeValue("SN");

				if (BaseClassUtil.isNullOrEmptyString(SN)) {
					continue;
				}

				for (GatherElementBean bean : projectBean.elementBeans) {
					if (bean.ElemSN.equals(SN)) {
						executeAttr(bean, false);
						return;
					}
				}
			}
		}
	}

	/** 提交的点击事件监听 */
	public void setOnReportClickListener(
			OnReportClickListener onReportClickListener) {
		this.onReportClickListener = onReportClickListener;
	}

	/**
	 * 作点击查询，查询当前点的空间属性信息
	 * 
	 */
	public class PointQueryTask extends
			AsyncTask<Void, Void, LinkedHashMap<String, String>> {

		@Override
		protected LinkedHashMap<String, String> doInBackground(Void... params) {
			PointF pointF = getTargetViewCenterPoint();

			// 获取绘制标注点的地图坐标
			Dot mapDot = mapView.viewPointToMapPoint(pointF);

			Graphic graphic = GatherDataUtils.searchTargetGeomLayer(mapView,
					mapDot);

			if (graphic == null) {
				return null;
			}

			LinkedHashMap<String, String> attrMap = new LinkedHashMap<String, String>();

			for (int i = 0; i < graphic.getAttributeNum(); i++) {
				String key = graphic.getAttributeName(i);
				String value = graphic.getAttributeValue(i);
				attrMap.put(key, value);
			}

			String layerName = attrMap.get("$图层名称$");

			attrMap = GatherDataUtils.sortByDbColumn(attrMap);

			attrMap.put("$坐标$",
					graphic.getCenterPoint().x + ","
							+ graphic.getCenterPoint().y);
			attrMap.put("$图层名称$", layerName);

			return attrMap;
		}

		@Override
		protected void onPostExecute(LinkedHashMap<String, String> attrMap) {
			onCaptureFinished(attrMap);
		}
	}

	/**
	 * 获取补漏所需要填写的属性信息
	 * 
	 */
	protected class GetGatherDataLayerTask extends
			AsyncTask<Void, Void, String> {

		private ProgressDialog progressDialog;

		private final GatherElementBean elementBean;
		private final boolean isAdd;

		public GetGatherDataLayerTask(GatherElementBean elementBean,
				boolean isAdd) {
			this.elementBean = elementBean;
			this.isAdd = isAdd;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = MmtProgressDialog.getLoadingProgressDialog(
					mapGISFrame, "正在加载数据...");
			progressDialog.show();
		}

        @Override
		protected String doInBackground(Void... params) {

			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/GetGatherDataLayer";

			String resultStr = NetUtil.executeHttpGet(url);

			if (BaseClassUtil.isNullOrEmptyString(resultStr)) {
				return null;
			}

			return resultStr;
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.cancel();

			if (BaseClassUtil.isNullOrEmptyString(result)) {
				mapGISFrame.showToast("获取信息失败，检查是否服务需要更新");
				return;
			}

			ResultData<GatherDataLayer> entity = new Gson().fromJson(result,
					new TypeToken<ResultData<GatherDataLayer>>() {
					}.getType());

			if (entity.ResultCode < 0) {
				mapGISFrame.showToast(entity.ResultMessage);
				return;
			}

			CacheUtils.getInstance(mapGISFrame).put(
					GATHER_DATA_ATTR,
					new Gson().toJson(entity.DataList,
							new TypeToken<ArrayList<GatherDataLayer>>() {
							}.getType()));

			showAttrEditFragment(elementBean, entity.DataList, isAdd);
		}
    }

	/**
	 * 先选择移交人员，再上报填写的数据信息
	 * 
	 */
	protected class ReportData extends AsyncTask<Void, Void, Long> {
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = MmtProgressDialog.getLoadingProgressDialog(
					mapGISFrame, "正在保存数据...");
			progressDialog.show();
		}

        @Override
		protected Long doInBackground(Void... params) {
			/*
			 * 获取地址信息
			 */
//			String address = MyApplication.getInstance()
//					.getSystemSharedPreferences()
//					.getString("currentAddress", "地址获取中");
//
//			if (address.equals("无法获取地址")) {
//				address = "";
//			}
//			if (address.equals("地址获取中")) {
//				mapGISFrame.showToast("正在查询地址信息，请稍候提交");
//				return (long) -100;
//			}
//			/*
//			 * 获取坐标信息
//			 */
//			GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
//			String position = xyz.getX() + "," + xyz.getY();

			String address=projectBean.address;
			String position =projectBean.dot.x+","+projectBean.dot.y; 
			/*
			 * 简单的描述信息
			 */
			List<String> descs = new ArrayList<String>();
			for (GatherElementBean bean : projectBean.elementBeans) {
				descs.add(bean.Oper + "-" + bean.LayerName);
			}

			String desc = BaseClassUtil.listToString(descs);

			

			projectBean.caluteRange();

			String data = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create()
					.toJson(projectBean);

			List<String> absoluteFilePaths = new ArrayList<String>();
			List<String> relativeFilePaths = new ArrayList<String>();

			for (GatherElementBean elementBean : projectBean.elementBeans) {

				if (BaseClassUtil.isNullOrEmptyString(elementBean.Photo))
					continue;

				absoluteFilePaths.add(elementBean.photosPaths);
				relativeFilePaths.add(elementBean.Photo);
			}
            String relativePaths=BaseClassUtil.listToString(relativeFilePaths);
            
			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/GatherDataReport?UserID="
					+ MyApplication.getInstance().getUserId() + "&Position="
					+ position + "&Desc=" + desc + "&Addr=" + address+"&imgUrls="+relativePaths;
			
			ReportInBackEntity entity = new ReportInBackEntity(data,
					MyApplication.getInstance().getUserId(),
					ReportInBackEntity.REPORTING, url, projectBean.ID + "",
					"数据采集", BaseClassUtil.listToString(absoluteFilePaths),
					relativePaths);

			return entity.insert();

		}

		@Override
		protected void onPostExecute(Long result) {
			progressDialog.cancel();
			if (result == -100) {
				return;
			}
			if (result > 0) {
				mapView.getGraphicLayer().removeAllGraphics();
				mapView.refresh();

				mapGISFrame.showToast("保存成功");

				if (onReportClickListener != null) {
					onReportClickListener.OnClick();
				}
				// 提交成功后重置，方便采集下一个
				resetProject();
			} else {
				mapGISFrame.showToast("信息保存失败");
			}
		}
    }

	public interface OnReportClickListener {
		void OnClick();
	}
}
