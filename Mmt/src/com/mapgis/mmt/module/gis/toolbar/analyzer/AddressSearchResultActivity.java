package com.mapgis.mmt.module.gis.toolbar.analyzer;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.baidu.BDSearchResultFragment;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.GisSearchResultFragment;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.local.LocalPlaceSearchResultFragment;

public class AddressSearchResultActivity extends BaseActivity {
	private Fragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("查询结果");

		fragment = new GisSearchResultFragment();

		if (PlaceSearch.SEARCH_RESULT instanceof BDPlaceSearchResult) {// 百度结果

			fragment = new BDSearchResultFragment();
			getBaseTextView().setText( "<" + getIntent().getStringExtra("where") + ">" + "  第" + (getIntent().getIntExtra("page", 0) + 1) + "页");

		}else if(PlaceSearch.SEARCH_RESULT instanceof LocalPlaceSearchResult){  // 本地db文件地名查询结果
			
			fragment = new LocalPlaceSearchResultFragment();
			getBaseTextView().setText( "<" + getIntent().getStringExtra("where") + ">" + "  第" + (getIntent().getIntExtra("page", 0) + 1) + "页");
			
		}else {// GIS服务器结果
			fragment = new GisSearchResultFragment();
		}

		addFragment(fragment);

	}

	public void resetBDFragmentAdapter() {
		BDSearchResultFragment f = (BDSearchResultFragment) fragment;
		f.setAdapter();
	}
	
	public void resetNativeFragmentAdapter( boolean hasNewData, int page ){
		LocalPlaceSearchResultFragment f = (LocalPlaceSearchResultFragment)fragment;
		f.setAdapter( hasNewData, page );
	}
	
}



