package com.mapgis.mmt.module.taskcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class TaskControlActivity extends BaseActivity {
	private final List<TaskControlEntity> entities = new ArrayList<TaskControlEntity>();
	private TaskControlFragment taskControlFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getBaseTextView().setText("任务监控");

		getBaseRightImageView().setVisibility(View.VISIBLE);
		getBaseRightImageView().setImageResource(R.drawable.delete_white);
		getBaseRightImageView().setOnClickListener(deleteListener);

		taskControlFragment = new TaskControlFragment();
		addFragment(taskControlFragment);
	}

	private final OnClickListener deleteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TextView textView = new TextView(TaskControlActivity.this);
			textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			textView.setText("确定要删除所有任务监控记录吗？");
			textView.setTextAppearance(TaskControlActivity.this, R.style.default_text_blue);

			final OkCancelDialogFragment okCancelDlgFrgt = new OkCancelDialogFragment("删除记录", textView);
			okCancelDlgFrgt.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
				@Override
				public void onLeftButtonClick(View view) {
					okCancelDlgFrgt.dismiss();
				}
			});

			okCancelDlgFrgt.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
				@Override
				public void onRightButtonClick(View view) {
					TaskControlDBHelper.getIntance().deleteAllControlData();
					taskControlFragment.refresh();
				}
			});

			okCancelDlgFrgt.show(getSupportFragmentManager(), "");
		}
	};

	class TaskControlFragment extends ListFragment {
		private ArrayAdapter<TaskControlEntity> adapter;

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			getListView().setCacheColorHint(0);
			getListView().setSelector(R.drawable.item_focus_bg);

			adapter = new ArrayAdapter<TaskControlEntity>(getActivity(), R.layout.simple_list_item_1, R.id.text1, entities);

			setListAdapter(adapter);

			entities.addAll(TaskControlDBHelper.getIntance().queryControlData(MyApplication.getInstance().getUserId()));
			adapter.notifyDataSetChanged();
		}

		protected void refresh() {
			entities.clear();
			entities.addAll(TaskControlDBHelper.getIntance().queryControlData(MyApplication.getInstance().getUserId()));
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Intent intent = new Intent(getActivity(), TaskControlDetailActivity.class);
			intent.putExtra("showData", entities.get(position).getShowData());
			startActivity(intent);
		}

	}
}
