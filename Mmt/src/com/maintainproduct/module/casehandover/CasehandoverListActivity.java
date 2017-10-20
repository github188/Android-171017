package com.maintainproduct.module.casehandover;

import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;

import java.util.ArrayList;

public class CasehandoverListActivity extends BaseActivity {
	private final ArrayList<MaintainSimpleInfo> taskList = new ArrayList<MaintainSimpleInfo>();

	private CasehandoverListFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("工单列表");

		fragment = new CasehandoverListFragment();
		addFragment(fragment);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	/** 获取维修养护工单列表服务 */
	// /////////////////////////////////////////////////////////////////////////////////////
	class GetCasehandoverListTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {

			String url = ServerConnectConfig.getInstance().getBaseServerPath()
					+ "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/PatrolREST.svc/GetDoingCase";

			String json = NetUtil.executeHttpGet(url, "userID", MyApplication.getInstance().getUserId() + "", "planid",
					MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).PatrolPlanID + "");

			return json;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				ResultData<MaintainSimpleInfo> data = new Gson().fromJson(result,
						new TypeToken<ResultData<MaintainSimpleInfo>>() {
						}.getType());

				if (data != null) {
					Toast.makeText(CasehandoverListActivity.this, data.ResultMessage, Toast.LENGTH_SHORT).show();
					if (data.DataList != null) {
						taskList.clear();
						taskList.addAll(data.DataList);
					}
				}

				fragment.notifyDataSetChanged();

			} catch (JsonSyntaxException e) {

				showErrorMsg(result);

				e.printStackTrace();
			} finally {
				fragment.onRefreshComplete();
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	/** 维修养护工单Fragment */
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	class CasehandoverListFragment extends Fragment {
		private PullToRefreshListView listView;
		private CasehandoverAdapter adapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.pull_list, container, false);

			listView = (PullToRefreshListView) view.findViewById(R.id.order_form_list);
			// 给listview添加刷新监听器
			listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
				@Override
				public void onRefresh(PullToRefreshBase<ListView> refreshView) {
					String label = DateUtils.formatDateTime(CasehandoverListActivity.this, System.currentTimeMillis(),
							DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

					// 更下下拉面板
					refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

					// 执行更新任务,结束后刷新界面
					new GetCasehandoverListTask().executeOnExecutor(MyApplication.executorService);
				}
			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

					MaintainSimpleInfo itemEntity = (MaintainSimpleInfo) arg0.getItemAtPosition(arg2);

					CaseHandoverUserFragment fragment = new CaseHandoverUserFragment(itemEntity);
					fragment.show(getChildFragmentManager(), "");

				}
			});

			ListView actualListView = listView.getRefreshableView();

			registerForContextMenu(actualListView);

			adapter = new CasehandoverAdapter(taskList, getActivity());
			actualListView.setAdapter(adapter);

			listView.setRefreshing(false);

			return view;
		}

		public void notifyDataSetChanged() {
			adapter.notifyDataSetChanged();
		}

		public void onRefreshComplete() {
			listView.onRefreshComplete();
		}

		/** ListView的适配器，决定每一列的视图样式 */
		class CasehandoverAdapter extends BaseAdapter {
			private final ArrayList<MaintainSimpleInfo> data;
			private final LayoutInflater inflater;

			public CasehandoverAdapter(ArrayList<MaintainSimpleInfo> data, Context context) {
				this.data = data;
				this.inflater = LayoutInflater.from(context);
			}

			@Override
			public int getCount() {
				return data.size();
			}

			@Override
			public Object getItem(int position) {
				return data.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder viewHolder = null;
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.maintenance_list_item, null);
					viewHolder = new ViewHolder();

					// viewHolder.maintenanceListItemIndex = (TextView)
					// convertView.findViewById(R.id.maintenanceListItemIndex);
					// viewHolder.maintenanceListItemCaseName = (TextView)
					// convertView
					// .findViewById(R.id.maintenanceListItemCaseName);
					// viewHolder.maintenanceListItemCaseNo = (TextView)
					// convertView.findViewById(R.id.maintenanceListItemCaseNo);
					// viewHolder.maintenanceListItemFlow = (TextView)
					// convertView.findViewById(R.id.maintenanceListItemFlow);
					// viewHolder.maintenanceListItemPre = (TextView)
					// convertView.findViewById(R.id.maintenanceListItemPre);

					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}

				MaintainSimpleInfo table = data.get(position);

				viewHolder.maintenanceListItemIndex.setText(String.valueOf(position + 1) + ".");
				viewHolder.maintenanceListItemCaseName.setText(table.CaseName);
				viewHolder.maintenanceListItemCaseNo.setText(table.CaseNo);
				viewHolder.maintenanceListItemFlow.setText(table.FlowName + "_" + table.ActiveName);

				if (table.PreStepUnderTakenManName.length() > 0) {
					viewHolder.maintenanceListItemPre.setText(table.PreStepUnderTakenManName + ":" + table.Opinion);
				}

				return convertView;
			}

			class ViewHolder {
				public TextView maintenanceListItemIndex;
				public TextView maintenanceListItemCaseName;
				public TextView maintenanceListItemCaseNo;
				public TextView maintenanceListItemFlow;
				public TextView maintenanceListItemPre;
			}
		}
	}
}
