package com.mapgis.mmt.module.gis.toolbar.analyzer.local;

import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult.Attrbuite;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult.LocalPlaceSearchResultItem;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.List;

/**
 * 本地db文件 地名搜索
 * 
 * @author meikai 配置例值 { "Key": "NativePlaceSearch", "Value":
 *         "地名点|地名|LOC_COORD_X|LOC_COORD_Y|15", "Label": "本地地名搜索",
 *         "Description": "参数1|参数2|参数3|参数4|参数5", "ExtraInfo":
 *         "参数1表示地址图层名，参数2表示包含地址的字段名，参数3表示横坐标字段名，参数4表示纵坐标字段名，参数5表示分页查询时单页的数量（5个参数都不可缺少）"
 *         },
 */
public class LocalPlaceSearch extends PlaceSearch {

	MapView mapView;
	String placeName;
	String layerName;
	String fieldNameAddress;
	String fieldNameX;
	String fieldNameY;
	int page;
	int pageCount;

	public LocalPlaceSearch(MapView mapView, String placeName, String layerName, String fieldNameAddress, String fieldNameX,
			String fieldNameY, int page, int pageCount) {
		super(placeName);
		this.mapView = mapView;
		this.placeName = placeName;
		this.layerName = layerName;
		this.fieldNameAddress = fieldNameAddress;
		this.fieldNameX = fieldNameX;
		this.fieldNameY = fieldNameY;
		this.page = page;
		this.pageCount = pageCount;
	}

	@Override
	public Object search(int page) {

		List<Graphic> graList = null;
		LocalPlaceSearchResult result = new LocalPlaceSearchResult();

		LayerEnum layerEnum = mapView.getMap().getLayerEnum();
		layerEnum.moveToFirst();
		MapLayer mapLayer;
		while ((mapLayer = layerEnum.next()) != null) {
			if (!(mapLayer instanceof VectorLayer) || !mapLayer.getName().equals(layerName)) {
				continue;
			}

			FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) mapLayer, fieldNameAddress + " like '%" + placeName
					+ "%'", null, FeatureQuery.SPATIAL_REL_OVERLAP, true, true, "", this.pageCount);

			if (featurePagedResult != null && featurePagedResult.getTotalFeatureCount() > 0) {
				graList = Convert.fromFeaturesToGraphics(featurePagedResult.getPage(page + 1), mapLayer.getName()); // featurePagedResult.getPage
																													// 的参数是
																													// 从
																													// 1
																													// 开始的
				break;
			}
		}

		for (int i = 0; i < graList.size(); i++) {
			LocalPlaceSearchResultItem item = result.new LocalPlaceSearchResultItem();
			for (long j = 0; j < graList.get(i).getAttributeNum(); j++) {
				Attrbuite attr = result.new Attrbuite();
				attr.key = graList.get(i).getAttributeName(j);
				attr.value = graList.get(i).getAttributeValue(j);
				item.attrbuiteList.add(attr);

				if (attr.key.equals(fieldNameX)) {
					item.loc_x = Double.parseDouble(attr.value);
				}
				if (attr.key.equals(fieldNameY)) {
					item.loc_y = Double.parseDouble(attr.value);
				}
				if (attr.key.equals(fieldNameAddress)) {
					item.addressName = attr.value;
				}

			}
			result.dataList.add(item);
		}

		return result;
	}

}
