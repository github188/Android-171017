package com.mapgis.mmt.module.gis.toolbar.accident;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentIdentify;
import com.mapgis.mmt.module.gis.toolbar.accident.entity.AccidentResult;

import java.util.List;

/**
 * 查询结果显示对话框。用于显示查询的图层以及查询到的结果的数量，并且可以勾选图层显示。
 * 
 */
public class AccidentCheckDialogFragment extends DialogFragment {
	private ListView accidentListView;
	private MyAdapter adapter;

	/** 爆管分析查询结果 */
	private final AccidentResult accidentResult;

	private OnOkClickListener onOkClickListener;

	/**
	 * 查询结果显示对话框
	 * 
	 * @param accidentResult
	 *            爆管分析结果
	 */
	public AccidentCheckDialogFragment(AccidentResult accidentResult) {
		this.accidentResult = accidentResult;
	}

	public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
		this.onOkClickListener = onOkClickListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accident_list_view, container, false);
		accidentListView = (ListView) view.findViewById(R.id.accidentListView);
		adapter = new MyAdapter(getActivity(), accidentResult);
		accidentListView.setAdapter(adapter);

		accidentListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((View) view.getTag()).performClick();
			}
		});

		view.findViewById(R.id.btn_cancel).setVisibility(View.GONE);

		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOkClickListener.onClick();
			}
		});

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		return view;
	}

	/** 监听器，监听确认按钮点击事件 */
	public interface OnOkClickListener {
		void onClick();
	}

	class MyAdapter extends BaseAdapter {
		private final AccidentResult accidentResult;
		private final Context context;

		private final List<String> dataList;

		public MyAdapter(Context context, AccidentResult accidentResult) {
			this.accidentResult = accidentResult;
			this.context = context;

			dataList = accidentResult.getResultTypesWithCount();
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final AccidentIdentify identify = accidentResult.getAccidentIdentifyByIndex(position);

			int drawableId = R.drawable.user_selected;

			// 若该该图层查询的有结果，并且是需要显示的，则采用勾选中的图片
			if (identify != null && identify.totalRcdNum > 0 && identify.isAnnotationShow) {
				drawableId = R.drawable.user_selected;
			} else {// 否则采用未勾选的图片
				drawableId = R.drawable.user_unselected;
			}

			// 横向线性布局，有一个ImageView和一个TextView组合而成
			LinearLayout layout = new LinearLayout(context);
			layout.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setMinimumHeight((int) getActivity().getResources().getDimension(R.dimen.singline_height));

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(DimenTool.dip2px(getActivity(), getActivity().getResources().getDimension(R.dimen.margin_left)), 0,
					0, 0);
			params.gravity = Gravity.CENTER_VERTICAL;

			final ImageView imageView = new ImageView(getActivity());
			imageView.setLayoutParams(params);
			imageView.setImageResource(drawableId);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (identify != null) {
						identify.isAnnotationShow = !identify.isAnnotationShow;
						imageView.setImageResource(identify.isAnnotationShow ? R.drawable.user_selected : R.drawable.user_unselected);
					}
				}
			});

			layout.addView(imageView);

			TextView textView = new TextView(getActivity());
			textView.setLayoutParams(params);
			textView.setText(getItem(position).toString());
			textView.setTextAppearance(getActivity(), R.style.default_text_medium_1);

			layout.addView(textView);

			// 将ImageVIew缓存起来，方便调用它的点击事件
			layout.setTag(imageView);

			return layout;
		}
	}
}
