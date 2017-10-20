package com.mapgis.mmt.module.gis.toolbar.analyzer.local;

import java.util.ArrayList;

public class LocalPlaceSearchResult {

	public ArrayList<LocalPlaceSearchResultItem> dataList = new ArrayList<LocalPlaceSearchResult.LocalPlaceSearchResultItem>();

	public class LocalPlaceSearchResultItem {

		public String addressName;

		public double loc_x;

		public double loc_y;

		public ArrayList<Attrbuite> attrbuiteList = new ArrayList<LocalPlaceSearchResult.Attrbuite>();
	}

	public class Attrbuite {
		public String key;
		public String value;
	}
}
