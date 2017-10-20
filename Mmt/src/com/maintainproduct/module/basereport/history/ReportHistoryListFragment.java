package com.maintainproduct.module.basereport.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.maintainproduct.entity.BaseReportEntity;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.R;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportHistoryListFragment extends ListFragment {
	private final MyAdapter adapter;
	private final List<BaseReportEntity> entities = new ArrayList<BaseReportEntity>();

	private final String title;// 事件类型,即功能模块的alias值

	private boolean isOnBottom = false;// 是否滑动到最后底部

	private boolean isQueryAll = false;

	private int page = 0;// 加载的第几页
	private int startIndex = 0;// 从第几个数据开始获取
	private final int showCount = 10;// 每次查询的个数

	private int flag;// 查询的单位
	private String startTime;// 自定义开始时间
	private String endTime;// 自定义结束时间

	private final FinalBitmap fb;

	public ReportHistoryListFragment(Context context, String title) {
		this.title = title;
		this.adapter = new MyAdapter(context, this.entities);
		refreash(ReportHistoryActivity.All, null, null);

		fb = FinalBitmap.create(context);
		fb.configBitmapMaxHeight(DimenTool.dip2px(context, 80));
		fb.configBitmapMaxWidth(DimenTool.dip2px(context, 60));
		fb.configBitmapLoadThreadSize(2);
		fb.configDiskCachePath(MyApplication.getInstance().getMediaPathString());
	}

	/** 根据条件查询数据 */
	public void refreash(int flag, String startTime, String endTime) {
		this.flag = flag;
		this.startTime = startTime + " 00:00:00";
		this.endTime = endTime + " 23:59:59";

		this.page = 0;
		this.startIndex = 0;

		new AsyncTask<Object, Void, Void>() {// 对查询过程进行异步操作，防止界面卡主
			@Override
			protected Void doInBackground(Object... params) {
				entities.clear();
				entities.addAll(DatabaseHelper.getInstance().query(
						BaseReportEntity.class,
						createSql(Integer.valueOf(params[0].toString()), params[1] == null ? null : params[1].toString(),
								params[2] == null ? null : params[2].toString())));
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				adapter.notifyDataSetChanged();
			}

        }.executeOnExecutor(MyApplication.executorService,flag, startTime, endTime);

	}

	/** 根据条件查询数据，在原有数据的基础上添加 */
	public void addData(int flag, String startTime, String endTime) {
		new AsyncTask<Object, Void, Boolean>() {// 对查询过程进行异步操作，防止界面卡主
			@Override
			protected Boolean doInBackground(Object... params) {
				int preSize = entities.size();
				entities.addAll(DatabaseHelper.getInstance().query(
						BaseReportEntity.class,
						createSql(Integer.valueOf(params[0].toString()), params[1] == null ? null : params[1].toString(),
								params[2] == null ? null : params[2].toString())));
				return preSize == entities.size();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {// 若查询后的数据长度与查询之前相同，则数据库无更多数据，给出提示
					((BaseActivity) getActivity()).showToast("未查询到更多数据");
					isQueryAll = true;
				} else {
					((BaseActivity) getActivity()).showToast("加载完毕");
					adapter.notifyDataSetChanged();
				}
			}
        }.executeOnExecutor(MyApplication.executorService,flag, startTime, endTime);
	}

	/** 根据参数创建条件语句 */
	private String createSql(int flag, String startTime, String endTime) {
		String where = "EventType='" + title + "' and UserId=" + MyApplication.getInstance().getUserId();
		switch (this.flag) {
		case ReportHistoryActivity.Year:// 本年
			where = where + " and Time>date('now','localtime','start of year')";
			break;
		case ReportHistoryActivity.Month:// 本月
			where = where + " and Time>date('now','localtime','start of month')";
			break;
		case ReportHistoryActivity.Week:// 从当天所处的周一开始
			where = where + " and Time>date('now','localtime','weekday 1','-7 day','start of day')";
			break;
		case ReportHistoryActivity.Today:// 本日
			where = where + " and Time>date('now','localtime','start of day')";
			break;
		case ReportHistoryActivity.Any:// 任意
			where = where + " and Time>'" + this.startTime + "' and Time<'" + this.endTime + "'";
			break;
		case ReportHistoryActivity.All:// 所有
			break;
		}

		where = where + " order by Time desc limit " + startIndex + "," + showCount;// 按时间降序排列

		return where;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setListAdapter(adapter);
		getListView().setSelector(R.drawable.item_focus_bg);
		getListView().setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					fb.pauseWork(true);
				} else {
					fb.pauseWork(false);
				}

				if (!isQueryAll && isOnBottom && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 拉倒底部后自动加载
					page++;
					startIndex = page * showCount;
					addData(flag, startTime, endTime);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				isOnBottom = totalItemCount == firstVisibleItem + visibleItemCount;
			}
		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		BaseReportEntity entity = (BaseReportEntity) l.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), ReportHistoryDetailActivity.class);
		intent.putExtra("BaseReportEntity", entity);
		startActivityForResult(intent, 100);
		MyApplication.getInstance().startActivityAnimation(getActivity());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();

		fb.pauseWork(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		fb.pauseWork(false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		fb.clearCache();
		fb.exitTasksEarly(false);
	}

	private class MyAdapter extends BaseAdapter {
		private final LayoutInflater inflater;
		private final List<BaseReportEntity> entities;

		public MyAdapter(Context context, List<BaseReportEntity> entities) {
			this.inflater = LayoutInflater.from(context);
			this.entities = entities;
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
			ViewHolder viewHolder = null;

			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.basereport_history_item, parent, false);
				viewHolder.baseReportHistoryImage = (ImageView) convertView.findViewById(R.id.baseReportHistoryImage);
				viewHolder.baseReportHistoryTime = (TextView) convertView.findViewById(R.id.baseReportHistoryTime);
				viewHolder.baseReportHistoryOverview = (TextView) convertView.findViewById(R.id.baseReportHistoryOverview);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			BaseReportEntity entity = (BaseReportEntity) getItem(position);

			// 显示图片信息
			if (BaseClassUtil.isNullOrEmptyString(entity.FilePath) || !entity.FilePath.contains(".jpg")) {
				viewHolder.baseReportHistoryImage.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(),
						R.drawable.no_picture));
			} else {
				// 将图片文件筛选出来
				List<String> picPaths = entity.getPicPath();

				if (new File(picPaths.get(0)).exists()) {// 若文件存在则显示文件
					fb.display(viewHolder.baseReportHistoryImage, picPaths.get(0));
				}
			}

			viewHolder.baseReportHistoryTime.setText(entity.Time);
			viewHolder.baseReportHistoryOverview.setText(entity.showOverview(3));

			return convertView;
		}

	}

	class ViewHolder {
		public ImageView baseReportHistoryImage;
		public TextView baseReportHistoryTime;
		public TextView baseReportHistoryOverview;
	}

}
