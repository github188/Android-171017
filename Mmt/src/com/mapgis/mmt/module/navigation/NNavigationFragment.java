package com.mapgis.mmt.module.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapgis.mmt.R;

import java.util.ArrayList;

public class NNavigationFragment extends Fragment {

	NavigationActivity activity;
	ArrayList<NavigationItem> productFunctions;
	// NNavigationView navigationView;
	NavigationItemView[] navigationItemViewArr;
	int[] idsArr = { R.id.nav_itemView_01, R.id.nav_itemView_02, R.id.nav_itemView_03, R.id.nav_itemView_04,
			R.id.nav_itemView_05, R.id.nav_itemView_06, R.id.nav_itemView_07, R.id.nav_itemView_08, R.id.nav_itemView_09 };

	public NNavigationFragment(NavigationActivity activity, ArrayList<NavigationItem> productFunctions) {
		this.activity = activity;
		this.productFunctions = productFunctions;
		navigationItemViewArr = new NavigationItemView[9];
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.navigation_view, null);
		for (int i = 0; i < productFunctions.size(); i++) {
			navigationItemViewArr[i] = (NavigationItemView) view.findViewById(idsArr[i]);
			navigationItemViewArr[i].setItem(productFunctions.get(i));
			navigationItemViewArr[i].activity = this.activity;
		}
		return view;
	}

	// public NNavigationView getNavigationView() {
	// return navigationView;
	// }

}
