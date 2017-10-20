package com.mapgis.mmt.module.gis.toolbar.online.query.nearby;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.config.LayerConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailActivity;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineFeature;

import java.util.HashMap;

public class OnlineNearbyQueryResultActivity extends BaseActivity {
	private Parcelable[] onlineFeatures;
	private String field;

	private int clickPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		onlineFeatures = getIntent().getParcelableArrayExtra("data");
		field = LayerConfig.getInstance().getConfigInfo(getIntent().getStringExtra("layerName")).HighlightField;

		// 防止数据过多时,有短暂的黑屏出现
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName(this.getClass().getName());

				OnlineNearbyQueryResultFragment fragment = new OnlineNearbyQueryResultFragment();
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.baseFragment, fragment);
				ft.show(fragment);
				ft.commit();
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		switch (arg1) {
		case ResultCode.RESULT_PIPE_LOCATE:
			locateToRefreash();
			break;
		}
	}

	private void locateToRefreash() {
		Intent intent = new Intent();

		intent.putExtra("position", clickPosition);

		setResult(ResultCode.RESULT_PIPE_LOCATE, intent);

		AppManager.finishActivity(this);
		MyApplication.getInstance().finishActivityAnimation(this);
	}

	@Override
	public void onBackPressed() {
		AppManager.finishActivity(this);
	}

	/** 属性查询结果，实体片段 */
	class OnlineNearbyQueryResultFragment extends Fragment {

		private ListView listView;
		private MyAdapter adapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			listView = new ListView(getActivity());
			listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			listView.setOnItemClickListener(onItemClickListener);

			if (onlineFeatures != null) {
				adapter = new MyAdapter();
				listView.setAdapter(adapter);
			}

			setCustomView(initMyTitleView());

			return listView;
		}

		/**
		 * 自定义标题视图
		 * 
		 * @return
		 */
		private View initMyTitleView() {
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.header_bar_plan_name, null);

			((TextView) view.findViewById(R.id.tvPlanName)).setText("附近查询结果列表");
			((TextView) view.findViewById(R.id.tvTaskState)).setText(getActivity().getIntent().getStringExtra("whereInfo"));

			// 返回按钮
			view.findViewById(R.id.tvPlanBack).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AppManager.finishActivity(getActivity());
				}
			});

			((ImageView) view.findViewById(R.id.ivPlanDetail)).setImageResource(R.drawable.login_setting);
			view.findViewById(R.id.ivPlanDetail).setVisibility(View.VISIBLE);
			view.findViewById(R.id.ivPlanDetail).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					setResult(ResultCode.RESULT_WHERE_SELECTED);
					AppManager.finishActivity(getActivity());
				}
			});

			return view;
		}

		/** ListView每一项的点击事件 */
		OnItemClickListener onItemClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				clickPosition = arg2;

				Intent intent = new Intent(OnlineNearbyQueryResultActivity.this, PipeDetailActivity.class);

				OnlineFeature onlineFeature = (OnlineFeature) onlineFeatures[clickPosition];

				intent.putExtra("graphicMap", onlineFeature.attributes);
                intent.putExtra("graphicMapStr",new Gson().toJson(onlineFeature.attributes));
				startActivityForResult(intent, 0);
			}
		};

		public class MyAdapter extends BaseAdapter {
			@Override
			public int getCount() {
				return onlineFeatures.length;
			}

			@Override
			public Object getItem(int position) {
				return onlineFeatures[position];
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = LayoutInflater.from(getActivity()).inflate(R.layout.spatial_search_item, null);
					holder = new ViewHolder();
					holder.item_loc = (ImageView) convertView.findViewById(R.id.ItemImage);
					holder.primaryListTitle = (TextView) convertView.findViewById(R.id.ItemTitle);
					holder.secondListTile = (TextView) convertView.findViewById(R.id.ItemText);
					holder.thirdListTile = (TextView) convertView.findViewById(R.id.ItemDistance);
					holder.item_detail = (ImageView) convertView.findViewById(R.id.BtnDefault);
					holder.position = position;
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				final OnlineFeature feature = (OnlineFeature) onlineFeatures[position];

				holder.item_loc.setImageResource(R.drawable.icon_mark_normal);

				holder.item_loc.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						clickPosition = position;
						locateToRefreash();
					}
				});

				holder.primaryListTitle.setText(BaseClassUtil.isNullOrEmptyString(field) ? "" : feature.attributes.get(field));
				holder.secondListTile.setText(showInfo(feature.attributes));
				holder.secondListTile.setVisibility(View.VISIBLE);

				return convertView;
			}

			class ViewHolder {
				public ImageView item_loc;
				public TextView primaryListTitle;
				public TextView secondListTile;
				public TextView thirdListTile;
				public ImageView item_detail;
				public int position;
			}
		}

		private String showInfo(HashMap<String, String> map) {
			StringBuilder builder = new StringBuilder();
			for (String key : map.keySet()) {
				builder.append(key).append(":").append(map.get(key)).append(";");
			}
			return builder.toString();
		}
	}
}
