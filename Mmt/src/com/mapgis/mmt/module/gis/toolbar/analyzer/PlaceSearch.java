package com.mapgis.mmt.module.gis.toolbar.analyzer;

public abstract class PlaceSearch {
	public static Object SEARCH_RESULT;

	public PlaceSearch(String place) {
	}

	public abstract Object search(int page);
}
