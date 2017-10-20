package com.mapgis.mmt.module.gps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.module.gps.entity.GpsTraceBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpsHistoryActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String title = getIntent().getStringExtra("filter");

		getBaseTextView().setText(title);

		GpsHistoryFrament frament = new GpsHistoryFrament();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.baseFragment, frament);
		ft.commit();
	}
}

class GpsHistoryFrament extends Fragment {
	private int start = 0;
	private final int size = 5;
	private BaseAdapter adapter;

	private int queryCount;

	final List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private PullToRefreshListView mPullRefreshListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mPullRefreshListView = new PullToRefreshListView(getActivity());
		mPullRefreshListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity().getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// loadMoreDate();// 加载更多数据

				new GetGpsTask().executeOnExecutor(MyApplication.executorService);
			}
		});

		ListView actualListView = mPullRefreshListView.getRefreshableView();

		registerForContextMenu(actualListView);

		adapter = new SimpleAdapter(getActivity(), data, R.layout.simple_list_item_1, new String[] { "gpsFullInfo" },
				new int[] { R.id.text1 });
		actualListView.setAdapter(adapter);

		mPullRefreshListView.setRefreshing(false);

		return mPullRefreshListView;
	}

	private void getDataFromDB() {
		SQLiteQueryParameters para = new SQLiteQueryParameters(null, null, getActivity().getIntent().getStringExtra("filter")
				.equals("历史记录") ? "userId=" + MyApplication.getInstance().getUserId() : "isSuccess=0 and userId="
				+ MyApplication.getInstance().getUserId(), null, null, null, "id desc", start + "," + size);

		ArrayList<GpsTraceBean> beans = DatabaseHelper.getInstance().query(GpsTraceBean.class, para);

		for (GpsTraceBean gpsTraceBean : beans) {
			Map<String, String> map = new HashMap<String, String>();

			map.put("gpsFullInfo", gpsTraceBean.getFullString());

			data.add(map);
		}

	}

	class GetGpsTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			getDataFromDB();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			start += 5;
			adapter.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();

			// 当无更多数据时, 关闭刷新功能
			if (queryCount == data.size()) {
				mPullRefreshListView.setMode(Mode.DISABLED);
				Toast.makeText(getActivity(), "数据已经加载完毕!", Toast.LENGTH_SHORT).show();
			}

			queryCount = data.size();
		}
	}

}
