package com.mapgis.mmt.module.gis.toolbar.analyzer.baidu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.place.BDPlaceSearchResult;
import com.mapgis.mmt.module.gis.place.SingleSearchResult;
import com.mapgis.mmt.module.gis.toolbar.analyzer.AddressSearchTask;
import com.mapgis.mmt.module.gis.toolbar.analyzer.PlaceSearch;

import java.util.List;

public class BDSearchResultFragment extends Fragment {
	private ListView listView;

	private LinearLayout bottomLayout;

	private TextView nextTextView;
	private TextView preTextView;

	private String where;
	private int page;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.bd_search_result, container, false);

		listView = (ListView) view.findViewById(R.id.BDResultListView);

		bottomLayout = (LinearLayout) view.findViewById(R.id.BDResultLayout);

		bottomLayout.setVisibility(View.GONE);

		preTextView = (TextView) view.findViewById(R.id.BDResultPreView);
		nextTextView = (TextView) view.findViewById(R.id.BDResultNextView);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((BaseActivity) getActivity()).getBaseLeftImageView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra("page", page));
				AppManager.finishActivity();
				MyApplication.getInstance().finishActivityAnimation(getActivity());
			}
		});

		page = getActivity().getIntent().getIntExtra("page", 0);

		where = getActivity().getIntent().getStringExtra("where");

		listView.setAdapter(new ResultAdapter(getActivity(), (((BDPlaceSearchResult) PlaceSearch.SEARCH_RESULT)).results));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				BDAddressSearchResultCallback callback = new BDAddressSearchResultCallback((SingleSearchResult) arg0
						.getItemAtPosition(arg2));
				MyApplication.getInstance().sendToBaseMapHandle(callback);

				getActivity().finish();
			}
		});

		listView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (bottomLayout.getVisibility() == View.GONE) {
					bottomLayout.setVisibility(View.VISIBLE);
					handler.sendEmptyMessageDelayed(0, 2000);
				}
				return false;
			}
		});

		preTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				page--;

				if (page < 0) {
					page = 0;
				}

				resetAnimation();

				doSearch();
			}
		});

		nextTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				page++;

				resetAnimation();

				doSearch();
			}
		});
	}

	/** 开始查询 */
	private void doSearch() {
		((BaseActivity) getActivity()).getBaseTextView().setText("<" + where + ">" + "  第" + (page + 1) + "页");
		new AddressSearchTask(getActivity(), where, page).executeOnExecutor(MyApplication.executorService);
	}

	/** 重置动画效果 */
	private void resetAnimation() {
		handler.removeMessages(0);

		if (bottomLayout.getAnimation() != null) {
			bottomLayout.getAnimation().cancel();
			bottomLayout.getAnimation().reset();
		}

		handler.sendEmptyMessageDelayed(0, 2000);
	}

	/** 重置listview的适配器 */
	public void setAdapter() {
		listView.setAdapter(new ResultAdapter(getActivity(), (((BDPlaceSearchResult) PlaceSearch.SEARCH_RESULT)).results));
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Animation animation = new AlphaAnimation(1, 0);

			animation.setDuration(1500);

			bottomLayout.startAnimation(animation);

			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					bottomLayout.setVisibility(View.GONE);
				}
			});

		}
	};

	class ResultAdapter extends BaseAdapter {
		private final List<SingleSearchResult> results;

		private final Context context;

		public ResultAdapter(Context context, List<SingleSearchResult> results) {
			this.results = results;
			this.context = context;
		}

		@Override
		public int getCount() {
			return results.size();
		}

		@Override
		public Object getItem(int position) {
			return results.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = new LinearLayout(getActivity());
			convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			((LinearLayout) convertView).setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(DimenTool.dip2px(getActivity(), 20), DimenTool.dip2px(getActivity(), 3),
					DimenTool.dip2px(getActivity(), 3), DimenTool.dip2px(getActivity(), 3));

			LinearLayout layout = new LinearLayout(context);
			layout.setLayoutParams(layoutParams);
			layout.setOrientation(LinearLayout.VERTICAL);

			TextView name = new TextView(context);
			name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			name.setMinWidth(DimenTool.dip2px(context, 40));
			name.setTextAppearance(context, R.style.default_text_large);
			name.setText(results.get(position).name);

			TextView address = new TextView(context);
			address.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			address.setTextAppearance(context, R.style.default_text_medium_1);
			address.setText(results.get(position).address);

			layout.addView(name);
			layout.addView(address);

			((LinearLayout) convertView).addView(layout);

			return convertView;
		}
	}
}
