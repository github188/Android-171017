package com.mapgis.mmt.module.flowreport.history;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment;
import com.mapgis.mmt.common.widget.DateSelectDialogFragment.OnDateSelectPositiveClick;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.flowreport.FlowReportDetailActivity;
import com.mapgis.mmt.module.flowreport.FlowReportTaskParameters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class EventViewFragment extends Fragment {

	private EventViewAdapter adapter;
	private TextView dateSelectTextView;

	private Button dateSelectButton;
	private Button dateSelectSelfButton;

	// private long startTime;
	// private long endTime;
	private long deadlineTime;
	private long currentTime;

	List<FlowReportTaskParameters> parametersList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.flow_reporter_event_view, null);

		currentTime = System.currentTimeMillis();
		// 删除15天以前的信息
		deadlineTime = currentTime - 15 * 24 * 60 * 60 * 1000;
		// long deadlineTime = System.currentTimeMillis();
		List<FlowReportTaskParameters> deadParameters = getdeadlineParameters(deadlineTime);

		DatabaseHelper.getInstance().delete(FlowReportTaskParameters.class, "time < " + deadlineTime);

		if (deadParameters != null & deadParameters.size() > 0) {
			deletePic(deadParameters);
		}

		dateSelectTextView = (TextView) view.findViewById(R.id.dateSelectText);
		dateSelectButton = (Button) view.findViewById(R.id.dateSelectButton);
		dateSelectSelfButton = (Button) view.findViewById(R.id.dateSelectSelfButton);

		dateSelectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DateSelectDialogFragment dateSelectDialogFragment = new DateSelectDialogFragment();
				dateSelectDialogFragment.show(getActivity().getSupportFragmentManager(), "2");
				dateSelectDialogFragment.setOnDateSelectPositiveClick(new OnDateSelectPositiveClick() {

					@Override
					public void setOnDateSelectPositiveClickListener(View view, String startDate, String endDate,
							long startTimeLong, long endTimeLong) {
						parametersList.clear();
						if (StringToDate(startDate) == -1 && StringToDate(endDate) == -1) {
							getParameters();
						} else {
							getParameters(StringToDate(startDate), StringToDate(endDate) + 86399999);
							String result = startDate + " 至 " + endDate;
							// startTime = startTimeLong;
							// endTime = endTimeLong;
							dateSelectTextView.setText(result);
							dateSelectButton.setText("自定义过滤条件：" + result);
						}
						dateSelectSelfButton.setText("默认过滤条件");
						adapter.notifyDataSetChanged();
					}
				});
			}
		});

		dateSelectSelfButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ListDialogFragment fragment = new ListDialogFragment("选择时间段", new String[] { "本日", "本周", "自定义", "全部" });
				fragment.show(getActivity().getSupportFragmentManager(), "1");
				fragment.setListItemClickListener(new OnListItemClickListener() {
					@Override
					public void onListItemClick(int arg2, String value) {
						switch (arg2) {
						case 0:
							getParameters(currentTime - 86400000, currentTime);
							break;
						case 1:
							getParameters(currentTime - 7 * 86400000, currentTime);
							break;
						case 2:
							dateSelectButton.performClick();
							break;
						case 3:
							getParameters();
							break;
						}
						adapter.notifyDataSetChanged();
					}
				});
			}
		});

		getParameters(currentTime - 86400000, currentTime);

		adapter = new EventViewAdapter(this);

		if (parametersList != null) {

			((ListView) view.findViewById(R.id.eventViewList)).setAdapter(adapter);

		}

		((ListView) view.findViewById(R.id.eventViewList)).setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(getActivity(), FlowReportDetailActivity.class);
				intent.putExtra("event", parametersList.get(arg2).getCaseDesc());
				intent.putExtra("position", parametersList.get(arg2).getPosition());
				intent.putExtra("address", parametersList.get(arg2).getRoadName());
				intent.putExtra("description", parametersList.get(arg2).getContent());
				intent.putExtra("advice", parametersList.get(arg2).getEventSituation());
				intent.putExtra("media", parametersList.get(arg2).getMediaString());
				intent.putExtra("recorder", parametersList.get(arg2).getRecordString());
				startActivity(intent);
				MyApplication.getInstance().startActivityAnimation(getActivity());
			}
		});

		return view;
	}

	/**
	 * 获取图片路径
	 */
	public ArrayList<String> getMediaPath(String path) {
		ArrayList<String> list = new ArrayList<String>();
		String[] paths = path.split(",");
		for (int i = 0; i < paths.length; i++) {
			list.add(paths[i]);
		}
		return list;
	}

	/**
	 * 获取区间段数据库数据
	 */
	private void getParameters(long startDate, long endDate) {
		parametersList = DatabaseHelper.getInstance().query(FlowReportTaskParameters.class,
				"time >= " + startDate + " and time <= " + endDate + " and userId = " + MyApplication.getInstance().getUserId());
	}

	/**
	 * 获取数据库数据
	 */
	private void getParameters() {
		parametersList = DatabaseHelper.getInstance().query(FlowReportTaskParameters.class,
				"userId = " + MyApplication.getInstance().getUserId());
	}

	/**
	 * 获取15天前数据库数据
	 */
	private List<FlowReportTaskParameters> getdeadlineParameters(long deadlineTime) {
		return DatabaseHelper.getInstance().query(FlowReportTaskParameters.class, "time < " + deadlineTime);
	}

	/**
	 * 删除15天前的照片
	 */

	private void deletePic(List<FlowReportTaskParameters> deadParameters) {

		if (deadParameters == null || deadParameters.size() == 0) {
			return;
		}

		for (FlowReportTaskParameters parameters : deadParameters) {
			List<String> media = BaseClassUtil.StringToList(parameters.getMediaString(), ",");

			for (int i = 0; i < media.size(); i++) {

				if (BaseClassUtil.isNullOrEmptyString(media.get(i))) {
					continue;
				}

				if (new File(media.get(i)).exists()) {
					FileUtil.deleteFile(new File(media.get(i)));
				}
			}
		}
	}

	public Button getDateSelectSelfButton() {
		return dateSelectSelfButton;
	}

	/**
	 * 将String转换为long
	 */
	private long StringToDate(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Long time = null;
		try {
			Date date = sdf.parse(str);
			time = date.getTime();
			return time;
		} catch (ParseException e) {
			return -1;
		}
	}

	// 横向拍照得到的照片旋转90°显示
	Bitmap getMyBitmap(Bitmap bitmap) {

		if (bitmap == null) {
			return null;
		}

		if (bitmap.getWidth() > bitmap.getHeight()) {
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		}
		return bitmap;
	}
}