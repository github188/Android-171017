package com.maintainproduct.module.maintenance.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;

import net.tsz.afinal.core.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/** 维修养护工单列表 */
public class MaintenanceHistoryListActivity extends BaseActivity {

	protected MaintenanceHistoryFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("已办工单");

		fragment = new MaintenanceHistoryFragment();
		addFragment(fragment);
	}

	class MaintenanceHistoryFragment extends Fragment {
		private final List<FetchDoneEntity> dataList = new ArrayList<FetchDoneEntity>();
		private final Myadapter adapter = new Myadapter(MaintenanceHistoryListActivity.this, dataList);
		private PullToRefreshListView listView;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.pull_list, container, false);

			listView = (PullToRefreshListView) view.findViewById(R.id.order_form_list);
			// 给listview添加刷新监听器
			listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
				@Override
				public void onRefresh(PullToRefreshBase<ListView> refreshView) {
					String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
							| DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

					// 更新下拉面板
					refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

					// 执行更新任务,结束后刷新界面
					new FetchDoneCaseListTask(listView).executeOnExecutor(MyApplication.executorService);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					FetchDoneEntity entity = (FetchDoneEntity) arg0.getItemAtPosition(arg2);
					Intent intent = new Intent(getActivity(), MaintenanceHistoryDetailActivity.class);
					intent.putExtra("id", entity.ID + "");
					intent.putExtra("FlowName", entity.FlowName);
					intent.putExtra("CaseNo", entity.CASENO);
					startActivity(intent);
					MyApplication.getInstance().startActivityAnimation(getActivity());
				}
			});

			ListView actualListView = listView.getRefreshableView();

			registerForContextMenu(actualListView);

			actualListView.setAdapter(adapter);

			listView.setRefreshing(false);

			return view;
		}

		public void setDataList(List<List<FetchDoneEntity>> datalist) {
			this.dataList.clear();
			for (List<FetchDoneEntity> data : datalist) {
				this.dataList.addAll(data);
			}
			adapter.notifyDataSetChanged();
		}
	}

	class Myadapter extends BaseAdapter {
		private final List<FetchDoneEntity> entities;
		private final LayoutInflater inflater;

		public Myadapter(Context context, List<FetchDoneEntity> entities) {
			this.entities = entities;
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return entities.size();
		}

		@Override
		public Object getItem(int position) {
			return entities.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.maintenance_history_item, parent, false);
				holder = new ViewHolder();
				holder.maintenanceHisTime = (TextView) convertView.findViewById(R.id.maintenanceHisTime);
				holder.maintenanceHisAddr = (TextView) convertView.findViewById(R.id.maintenanceHisAddr);
				holder.maintenanceHisSource = (TextView) convertView.findViewById(R.id.maintenanceHisSource);
				holder.maintenanceHisDesc = (TextView) convertView.findViewById(R.id.maintenanceHisDesc);
				holder.maintenanceHisCaseno = (TextView) convertView.findViewById(R.id.maintenanceHisCaseno);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			FetchDoneEntity entity = (FetchDoneEntity) getItem(position);

			if (BaseClassUtil.isNullOrEmptyString(entity.REPORTTIME)) {
				holder.maintenanceHisTime.setVisibility(View.GONE);
			} else {
				holder.maintenanceHisTime.setText(entity.REPORTTIME.replace("T", " "));
			}

			String text = "";
			if (!BaseClassUtil.isNullOrEmptyString(entity.EVENTSOURCE)) {
				text += entity.EVENTSOURCE;
			}
			if (!BaseClassUtil.isNullOrEmptyString(entity.EVENTTYPE)) {
				text = text + (text.length() == 0 ? entity.EVENTTYPE : " " + entity.EVENTTYPE);
			}
			if (BaseClassUtil.isNullOrEmptyString(text)) {
				holder.maintenanceHisSource.setVisibility(View.GONE);
			} else {
				holder.maintenanceHisSource.setText(text);
			}

			if (BaseClassUtil.isNullOrEmptyString(entity.ADDRESS)) {
				holder.maintenanceHisAddr.setVisibility(View.GONE);
			} else {
				holder.maintenanceHisAddr.setText(entity.ADDRESS);
			}

			if (BaseClassUtil.isNullOrEmptyString(entity.DESCRIPTION)) {
				holder.maintenanceHisDesc.setVisibility(View.GONE);
			} else {
				holder.maintenanceHisDesc.setText(entity.DESCRIPTION);
			}

			if (BaseClassUtil.isNullOrEmptyString(entity.CASENO)) {
				holder.maintenanceHisCaseno.setVisibility(View.GONE);
			} else {
				holder.maintenanceHisCaseno.setText(entity.CASENO);
			}

			return convertView;
		}

		class ViewHolder {
			public TextView maintenanceHisTime;
			public TextView maintenanceHisAddr;
			public TextView maintenanceHisSource;
			public TextView maintenanceHisDesc;
			public TextView maintenanceHisCaseno;
		}
	}

	class FetchDoneCaseListTask extends AsyncTask<Void, Void, String> {
		private final PullToRefreshListView listView;

		public FetchDoneCaseListTask(PullToRefreshListView listView) {
			this.listView = listView;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/RepairStandardRest.svc/FetchDoneCaseList";
			String resultStr = NetUtil.executeHttpGet(url, "userID", MyApplication.getInstance().getUserId() + "");
			return resultStr;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result == null || BaseClassUtil.isNullOrEmptyString(result)) {
					showToast("未查询到已办工单");
					return;
				}

				String jsonStr = result.replace("[\"[", "[[").replace("]\"]", "]]");
				jsonStr = jsonStr.replace("\\", "");

				FetchDoneResult resultData = new Gson().fromJson(jsonStr, FetchDoneResult.class);

				showToast(resultData.ResultMessage);

				if (resultData.DataList != null) {
					fragment.setDataList(resultData.DataList);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				listView.onRefreshComplete();
			}
		}
	}

	class FetchDoneResult {
		public int ResultCode;
		public String ResultMessage;

		public List<List<FetchDoneEntity>> DataList;

		public int CurrentPage;
		public int Total;
	}

	class FetchDoneEntity {
		public int ID;
		public String EVENTSOURCE;// 巡线上报
		public String DISPOSEDEPARTMENT;// 管网部
		public int EVENTID;// 2
		public String CASENO;// QX-2014-0000016
		public String URGENCYDEGREE;// 紧急事件
		public String ACCIDENTLEVEL;// 三级
		public String EVENTTYPE;// 井盖丢失
		public String REPORTERTEL;// 18612344321
		public String REPORTTIME;// 2014-08-28T00:00:00
		public String DESCRIPTION;
		public String ADDRESS;
		public String POSITION;

		@SerializedName("流程名称")
		public String FlowName;
	}
}
