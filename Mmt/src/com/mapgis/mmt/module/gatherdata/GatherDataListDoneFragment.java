package com.mapgis.mmt.module.gatherdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gatherdata.operate.GatherDataOperate;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.login.UserBean;

import java.util.ArrayList;
import java.util.List;

public class GatherDataListDoneFragment extends Fragment implements
		OnClickListener {
	private final List<GatherProjectBean> projectBeans = new ArrayList<GatherProjectBean>();
	private GatherProjectBean bean = null;
	private final MapGISFrame mapGISFrame;

	// 数据显示列表
	private PullToRefreshListView dataListView;

	private MyAdapter adapter;

	private int startIndex = 1;
	private int endIndex = 5;
	private final int pageSize = 5;

	public GatherDataListDoneFragment(MapGISFrame mapGISFrame) {
		this.mapGISFrame = mapGISFrame;

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				DimenTool.dip2px(mapGISFrame, 30), DimenTool.dip2px(
						mapGISFrame, 30));
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		ImageView targetView = new ImageView(mapGISFrame);
		targetView.setLayoutParams(layoutParams);
		targetView.setImageResource(R.drawable.mapview_gather_point);
		targetView.setTag("MapViewScreenView");

		mapGISFrame.getMapView().addView(targetView);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		RelativeLayout relativeLayout = new RelativeLayout(getActivity());
		relativeLayout.setBackgroundResource(R.color.white);
		relativeLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// edit by WL - 20150525 新建按钮放置到此处
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.setMargins(DimenTool.dip2px(getActivity(), 10), 0,
				DimenTool.dip2px(getActivity(), 10),
				DimenTool.dip2px(getActivity(), 10));

		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setLayoutParams(params);
		linearLayout.setBackgroundResource(R.drawable.mapview_bottombar_bg);
		linearLayout.setId(linearLayout.hashCode());
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setMinimumHeight(DimenTool.dip2px(getActivity(), 50));

		linearLayout.addView(new BottomUnitView(mapGISFrame, "新建", this));
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(DimenTool.dip2px(getActivity(), 12), 0,
				DimenTool.dip2px(getActivity(), 12), 0);
		layoutParams.addRule(RelativeLayout.ABOVE, linearLayout.getId());

		dataListView = new PullToRefreshListView(getActivity());
		dataListView.setLayoutParams(layoutParams);
		dataListView.getRefreshableView().setDivider(
				getResources().getDrawable(R.color.default_no_bg));
		dataListView.getRefreshableView().setDividerHeight(
				DimenTool.dip2px(getActivity(), 12));
		dataListView.getRefreshableView().setSelector(R.drawable.item_focus_bg);
		dataListView.setFocusable(false);
		dataListView.setMode(Mode.BOTH);

		relativeLayout.addView(linearLayout);
		relativeLayout.addView(dataListView);

		return relativeLayout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		adapter = new MyAdapter(getActivity(), projectBeans);
		dataListView.setAdapter(adapter);

		dataListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新");
		dataListView.getLoadingLayoutProxy(true, false).setRefreshingLabel(
				"正在刷新");
		dataListView.getLoadingLayoutProxy(true, false)
				.setReleaseLabel("放开以刷新");
		// 上拉加载更多时的提示文本设置
		dataListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
		dataListView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
				"正在加载...");
		dataListView.getLoadingLayoutProxy(false, true)
				.setReleaseLabel("放开以加载");
		dataListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				String label = DateUtils.formatDateTime(getActivity()
						.getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				dataListView.getLoadingLayoutProxy(true, false)
						.setLastUpdatedLabel(label);
				dataListView.getLoadingLayoutProxy(false, true)
						.setLastUpdatedLabel(label);

				startIndex = 1;
				endIndex = 5;

				new GetDoneDataTask()
						.executeOnExecutor(MyApplication.executorService);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				String label = DateUtils.formatDateTime(getActivity()
						.getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
				dataListView.getLoadingLayoutProxy(false, true)
						.setLastUpdatedLabel(label);

				new GetDoneDataTask()
						.executeOnExecutor(MyApplication.executorService);

			}

		});

		dataListView.setRefreshing(false);

		dataListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				try {
					bean = (GatherProjectBean) arg0.getItemAtPosition(arg2);
					if (bean == null) {
						Toast.makeText(getActivity(), "该工程无管网数据",
								Toast.LENGTH_SHORT).show();
						return;
					}
					new GetGetElementDataTask().execute("" + bean.ID);
				} catch (Exception ex) {
					Toast.makeText(getActivity(), "该工程无管网数据和地图不匹配或无地图",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	class MyAdapter extends BaseAdapter {
		private final List<GatherProjectBean> projectBeans;
		private final LayoutInflater inflater;

		public MyAdapter(Context context, List<GatherProjectBean> projectBeans) {
			this.projectBeans = projectBeans;
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return projectBeans.size();
		}

		@Override
		public Object getItem(int position) {
			return projectBeans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.acquisition_historylist_item_view, null);
				viewHolder = new ViewHolder();
				viewHolder.index = (TextView) convertView
						.findViewById(R.id.acquisitionListItemIndex);
				viewHolder.acquisitionListItemName = (TextView) convertView
						.findViewById(R.id.acquisitionListItemName);
				viewHolder.acquisitionListItemCaseNO = (TextView) convertView
						.findViewById(R.id.acquisitionListItemCaseNO);
				viewHolder.acquisitionListItemReporter = (TextView) convertView
						.findViewById(R.id.acquisitionListItemReporter);

				viewHolder.acquisitionListItemStatus = (TextView) convertView
						.findViewById(R.id.acquisitionListItemStatus);
				viewHolder.acquisitionListItemReportDate = (TextView) convertView
						.findViewById(R.id.acquisitionListItemReportDate);
				viewHolder.acquisitionListItemType = (TextView) convertView
						.findViewById(R.id.acquisitionListItemType);
				viewHolder.acquisitionListItemAddress = (TextView) convertView
						.findViewById(R.id.acquisitionListItemAddress);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			final GatherProjectBean entity = (GatherProjectBean) getItem(position);

			viewHolder.index.setText((position + 1) + ".");
			viewHolder.acquisitionListItemName.setText(entity.EventCode);
			viewHolder.acquisitionListItemCaseNO.setText(entity.Describte);
			viewHolder.acquisitionListItemReporter.setText(entity.Repoter);
			viewHolder.acquisitionListItemReportDate.setText(entity.ReportTime
					.replace('T', ' '));
			viewHolder.acquisitionListItemType.setText(entity.Type);
			viewHolder.acquisitionListItemStatus.setText(entity.Status);
			viewHolder.acquisitionListItemAddress.setText(entity.address);

			return convertView;
		}

		class ViewHolder {
			public TextView index;
			public TextView acquisitionListItemName;
			public TextView acquisitionListItemCaseNO;
			public TextView acquisitionListItemReporter;
			public TextView acquisitionListItemStatus;
			public TextView acquisitionListItemReportDate;
			public TextView acquisitionListItemType;
			public TextView acquisitionListItemAddress;
		}
	}

	class GetGetElementDataTask extends
			AsyncTask<String, Void, List<GatherElementBean>> {
		private ProgressDialog loadingDialog;

		@Override
		protected void onPreExecute() {

			loadingDialog = MmtProgressDialog.getLoadingProgressDialog(
					getActivity(), "正在加载，请稍候");
			loadingDialog.show();
		}

		@Override
		protected List<GatherElementBean> doInBackground(String... params) {
			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/GetProjectElement";
			String resultStr = NetUtil.executeHttpGet(url, "ID", params[0]);

			if (BaseClassUtil.isNullOrEmptyString(resultStr))
				return null;

			ResultData<String> resultDatatemp = new Gson().fromJson(resultStr,
					new TypeToken<ResultData<String>>() {
					}.getType());
			ArrayList<GatherElementBean> list = new Gson().fromJson(
					resultDatatemp.DataList.get(0),
					new TypeToken<ArrayList<GatherElementBean>>() {
					}.getType());

			return list;
		}

		@Override
		protected void onPostExecute(List<GatherElementBean> result) {
			dataListView.onRefreshComplete();
			loadingDialog.dismiss();

			if (result == null) {
				Toast.makeText(getActivity(), "获取服务失败，网络异常或者服务不存在",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result.size() <= 0) {
				Toast.makeText(getActivity(), "该工程无管网数据", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			bean.elementBeans.clear();
			bean.elementBeans.addAll(result);
			// 在地图上显示图形

			try {
				MapGISFrame mapGISFrame = (MapGISFrame) getActivity();

				mapGISFrame.getMapView().getGraphicLayer().removeAllGraphics();
				mapGISFrame.getMapView().findViewWithTag("MapViewScreenView")
						.setVisibility(View.GONE);

				bean.initGatherDataElementDot();

				GatherDataOperate operate = new GatherDataOperate(mapGISFrame,
						bean, true);
				operate.setToolBarNoneOperate();
				operate.showGatherDataMap();
				mapGISFrame.getMapView().refresh();
			} catch (Exception ex) {
				Toast.makeText(getActivity(), "该工程无管网数据和地图不匹配或无地图",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	class GetDoneDataTask extends
			AsyncTask<Void, Void, ResultData<GatherProjectBean>> {

		@Override
		protected void onPreExecute() {
			((BaseActivity) getActivity()).setBaseProgressBarVisibility(true);
		}

		@Override
		protected ResultData<GatherProjectBean> doInBackground(Void... params) {
			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/FetchGatherHistoryList";
			String resultStr = NetUtil.executeHttpGet(url, "UserID",
					MyApplication.getInstance().getUserId() + "", "startIndex",
					"" + startIndex, "endIndex", "" + endIndex);

			if (BaseClassUtil.isNullOrEmptyString(resultStr))
				return null;

			ResultData<String> resultDatatemp = new Gson().fromJson(resultStr,
					new TypeToken<ResultData<String>>() {
					}.getType());
			ArrayList<GatherProjectBean> list = new Gson().fromJson(
					resultDatatemp.DataList.get(0),
					new TypeToken<ArrayList<GatherProjectBean>>() {
					}.getType());
			ResultData<GatherProjectBean> resultData = new ResultData<GatherProjectBean>();

			resultData.DataList = list;
			resultData.ResultCode = resultDatatemp.ResultCode;
			resultData.ResultMessage = resultDatatemp.ResultMessage;

			return resultData;
		}

		@Override
		protected void onPostExecute(ResultData<GatherProjectBean> result) {
			dataListView.onRefreshComplete();
			((BaseActivity) getActivity()).setBaseProgressBarVisibility(false);

			if (result == null) {
				Toast.makeText(getActivity(), "获取服务失败，网络异常或者服务不存在",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (result.ResultCode < 0) {
				Toast.makeText(getActivity(), result.ResultMessage,
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (result.DataList.size() <= 0) {
				Toast.makeText(getActivity(), "没有更多数据", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			// startIndex=1,说明是刷新或数据为0，
			if (startIndex == 1) {
				projectBeans.clear();
			}
			projectBeans.addAll(result.DataList);

			startIndex = projectBeans.size() + 1;
			endIndex = startIndex + pageSize - 1;

			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {

		// 点击新建就开始获取地理位置，减少提交耗时,后来改成了点描点时取
		// new BDGeocoderResultTask().execute("");

		String[] functions = new String[] { "单点采集", "单线采集" };

		ListDialogFragment fragment = new ListDialogFragment("新建任务", functions);

		fragment.setListItemClickListener(new OnListItemClickListener() {
			@Override
			public void onListItemClick(int arg2, String value) {

				GatherProjectBean projectBean = new GatherProjectBean();
				projectBean.Name = BaseClassUtil.getSystemTime("yyMMdd-HHmmss");
				projectBean.Repoter = MyApplication.getInstance()
						.getConfigValue("UserBean", UserBean.class).TrueName;
				projectBean.Status = "未提交";
				projectBean.ReportTime = BaseClassUtil.getSystemTime();

				mapGISFrame.setBottomBarClear();

				if (value.equals("片区采集")) {
					projectBean.Type = "片区采集";
				} else if (value.equals("单点采集")) {
					projectBean.Type = "单点采集";
				} else if (value.equals("单线采集")) {
					projectBean.Type = "单线采集";
				}

				// if (createCaseTask != null &&
				// createCaseTask.getStatus() == Status.RUNNING) {
				// mapGISFrame.showToast("正在创建案件，请不要重复操作");
				// return;
				// }
				mapGISFrame.getMapView().findViewWithTag("MapViewScreenView")
						.setVisibility(View.VISIBLE);

				new GatherDataOperate(mapGISFrame, projectBean)
						.showGatherDataMap();

				// new
				// CreateCaseTask(projectBean).executeOnExecutor(MyApplication.executorService);

			}
		});

		fragment.show(mapGISFrame.getSupportFragmentManager(), "");
	}

	// class BDGeocoderResultTask extends AsyncTask<String, String,
	// BDGeocoderResult> {
	//
	// @Override
	// protected BDGeocoderResult doInBackground(String... params) {
	// Location location = GpsReceiver.getInstance().getLastLocation();
	// return BDGeocoder.find(location);
	// }
	//
	// @Override
	// protected void onPostExecute(BDGeocoderResult bdResult) {
	//
	// try {
	// SharedPreferences preferences =
	// MyApplication.getInstance().getSystemSharedPreferences();
	// SharedPreferences.Editor editor = preferences.edit();
	//
	// String address="无法获取地址";
	// if (bdResult == null && getActivity() != null) {
	// Toast.makeText(getActivity(), "请检测GPS状态或者网络状况",
	// Toast.LENGTH_SHORT).show();
	// MyApplication.getInstance().putConfigValue("CurrentAddress", address);
	// editor.putString("currentAddress", address);
	// editor.commit();
	// return;
	// }
	//
	// address= bdResult.result.addressComponent.district +
	// bdResult.result.addressComponent.street
	// + bdResult.result.addressComponent.street_number;
	// if(BaseClassUtil.isNullOrEmptyString(address)){
	// address="无法获取地址";
	// }
	//
	// editor.putString("currentAddress", address);
	// editor.commit();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	// }
}
