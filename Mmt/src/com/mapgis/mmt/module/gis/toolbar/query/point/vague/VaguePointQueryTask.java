package com.mapgis.mmt.module.gis.toolbar.query.point.vague;

import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class VaguePointQueryTask implements Callable<Integer> {

	private onSearchListener onSearchListener;
	private ArrayList<String> visibleVectorLayerNames;
	private MapView mapView;
	private MapLayer layer;
	private ArrayList<Graphic> resultGraphicList;
	private int graphicCount;
	private Rect searchExtent;
	
	public interface onSearchListener{
		void onUpdate(String layerName, ArrayList<Graphic> graphicList, int currentCount);
		void onUpdate2(String layerName, FeaturePagedResult featurePagedResult, int currentCount);
		void onComplete(int totalCount) ;
	}
	
	public VaguePointQueryTask(MapView mapView , onSearchListener listener , Rect searchExtent) {
		super();
		this.mapView = mapView;
		this.onSearchListener = listener; 
		this.graphicCount = 0 ;
		this.searchExtent = searchExtent ;
	}

	@Override
	public Integer call() throws Exception {
		int result = 0 ;
		try{
			result = searchTargetGeomLayer( GeomType.GeomPnt );
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			onSearchListener.onComplete( result );
		}
		return result;
	}
	
	/**
	 * 搜索指定类型的图层
	 * @param targetGeomType
	 * @return
	 */
	private int searchTargetGeomLayer(GeomType targetGeomType) {

		visibleVectorLayerNames = GisQueryUtil.getVisibleVectorLayerNames(mapView);
		

		LayerEnum layerEnum = mapView.getMap().getLayerEnum();
		layerEnum.moveToFirst();
		while((layer = layerEnum.next())!=null){
			if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(targetGeomType)) {
				continue;
			}
		
//		for (int i = 0; i < mapView.getMap().getLayerCount(); i++) {
//			layer = mapView.getMap().getLayer(i);
//
//			if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(targetGeomType)) {
//				continue;
//			}
			
//			searchExtent.setXMin(searchExtent.xMin - mapView.getResolution(mapView.getZoom()) * 10);
//			searchExtent.setYMin(searchExtent.yMin - mapView.getResolution(mapView.getZoom()) * 10);
//			searchExtent.setXMax(searchExtent.xMin + mapView.getResolution(mapView.getZoom()) * 10);
//			searchExtent.setYMax(searchExtent.yMin + mapView.getResolution(mapView.getZoom()) * 10);

			// 存储要素查询结果
			FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "",
					new FeatureQuery.QueryBound(searchExtent), FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", 	50);

			if (featurePagedResult.getTotalFeatureCount() > 0) {
				resultGraphicList = new ArrayList<Graphic>();
				resultGraphicList.addAll(Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName() ));
				graphicCount += resultGraphicList.size();
				onSearchListener.onUpdate( layer.getName() , resultGraphicList , graphicCount );
			}
		}

		return graphicCount ; 
	}

}
