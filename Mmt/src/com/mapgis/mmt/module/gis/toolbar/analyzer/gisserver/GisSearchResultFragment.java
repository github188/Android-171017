package com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.adapter.TreeViewAdapter;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;
import com.mapgis.mmt.module.gis.toolbar.analyzer.gisserver.LocatorGeocodeResult.Candidate;

import java.util.ArrayList;
import java.util.List;

public class GisSearchResultFragment extends Fragment {
	private ExpandableListView expandableListView;

	private GisAddressSearchAdapter adapter;

	private final List<String> parent = new ArrayList<String>();
	private final List<List<LocatorGeocodeResult.Candidate>> child = new ArrayList<List<LocatorGeocodeResult.Candidate>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LinearLayout layout = new LinearLayout(getActivity());
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(Color.WHITE);

		expandableListView = new ExpandableListView(getActivity());
		expandableListView.setLayoutParams(params);

		layout.addView(expandableListView);
		return layout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initDate();

		adapter = new GisAddressSearchAdapter(getActivity(), TreeViewAdapter.PaddingLeft >> 1);
		List<GisAddressSearchAdapter.TreeNode> treeNode = adapter.GetTreeNode();
		for (int i = 0; i < parent.size(); i++) {
			GisAddressSearchAdapter.TreeNode node = new GisAddressSearchAdapter.TreeNode();
			node.parent = parent.get(i);
			for (int ii = 0; ii < child.get(i).size(); ii++) {
				node.childs.add(child.get(i).get(ii));
			}
			treeNode.add(node);
		}
		adapter.UpdateTreeNode(treeNode);

		expandableListView.setAdapter(adapter);

		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				LocatorGeocodeResult.Candidate candidate = child.get(groupPosition).get(childPosition);
				Toast.makeText(getActivity(), candidate.address, Toast.LENGTH_SHORT).show();
				getActivity().finish();

				GisAddressSearchResultCallback callback = new GisAddressSearchResultCallback(candidate);
				MyApplication.getInstance().sendToBaseMapHandle(callback);
				return false;
			}
		});
	}

	private void initDate() {
		if (PlaceSearch.SEARCH_RESULT == null) {
			return;
		}

		List<String> values = new ArrayList<String>();
		for (LocatorGeocodeResult.Candidate candidate : ((LocatorGeocodeResult) PlaceSearch.SEARCH_RESULT).candidates) {
			if (!values.contains(candidate.attributes[0].Value)) {
				values.add(candidate.attributes[0].Value);
				List<Candidate> list = new ArrayList<Candidate>();
				child.add(list);
			}
			int i = values.indexOf(candidate.attributes[0].Value);
			child.get(i).add(candidate);
		}

		for (String value : values) {
			if (value.equals("address")) {
				parent.add("地名");
			} else if (value.equals("positionline")) {
				parent.add("定位线");
			} else {
				parent.add("道路中心线");
			}
		}
	}
}
