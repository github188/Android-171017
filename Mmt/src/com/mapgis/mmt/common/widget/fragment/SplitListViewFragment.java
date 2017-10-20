package com.mapgis.mmt.common.widget.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;

import java.util.ArrayList;
import java.util.List;

public class SplitListViewFragment extends DialogFragment {
	private ListView splitLeftListView;
	private ListView splitRightListView;
	private TextView splitLeftListTitle;

	/** 左边初始数据集合 */
	private final List<String> leftList;
	/** 左边初始数据转换后的符合显示要求的集合,主要增加一个标识来判定是否被选中 */
	private final List<SplitItem> splitItems = new ArrayList<SplitListViewFragment.SplitItem>();

	/** 左边被选中后，右边显示的数据集合 */
	private final List<String> rightList = new ArrayList<String>();

	/** 左边被选中的数据的值 */
	private String leftValue;
	/** 左边被选中的数据位置 */
	private int leftPos;

	private SplitListViewAdapter leftAdapter;
	private ArrayAdapter<String> rightAdapter;

	/** 右边全部的数据集合 */
	private final List<List<String>> allRightList;

	/** Fragment的标题 */
	private final String title;

	/** Fragment的左侧副标题 */
	private final String splitLeftStr;
	/** Fragment的右侧副标题 */
	private final String splitRightStr;

	/** Fragment的左侧副标题的TextView */
	private TextView splitLeftText;
	/** Fragment的右侧副标题的TextView */
	private TextView splitRightText;

	private int leftWeight = 1;
	private int rightWeight = 1;

	private SplitListViewPositiveClick splitListViewPositiveClick;

	private Boolean rightListItemSingleLine = false;

	public void setSplitListViewPositiveClick(SplitListViewPositiveClick splitListViewPositiveClick) {
		this.splitListViewPositiveClick = splitListViewPositiveClick;
	}

	public void setRightListItemSingleLine(Boolean rightListItemSingleLine) {
		this.rightListItemSingleLine = rightListItemSingleLine;
	}

	/**
	 * 
	 * @param title
	 *            标题信息
	 * @param splitLeftStr
	 *            左侧副标题
	 * @param splitRightStr
	 *            右侧副标题
	 * @param leftList
	 *            左侧字符信息列表,与右侧侧是一对多的关系
	 * @param allRightList
	 *            右侧全部字符信息列表,与左侧是多对一的关系
	 */
	public SplitListViewFragment(String title, String splitLeftStr, String splitRightStr, List<String> leftList,
			List<List<String>> allRightList) {
		this.title = title;
		this.leftList = leftList;
		this.allRightList = allRightList;
		this.splitLeftStr = splitLeftStr;
		this.splitRightStr = splitRightStr;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.split_list_view, container, false);

		splitLeftListTitle = (TextView) view.findViewById(R.id.splitListTitle);

		splitLeftText = (TextView) view.findViewById(R.id.splitLeftText);
		((LinearLayout.LayoutParams) splitLeftText.getLayoutParams()).weight = leftWeight;

		splitRightText = (TextView) view.findViewById(R.id.splitRightText);
		((LinearLayout.LayoutParams) splitRightText.getLayoutParams()).weight = rightWeight;

		splitLeftListView = (ListView) view.findViewById(R.id.splitLeftListView);
		((LinearLayout.LayoutParams) splitLeftListView.getLayoutParams()).weight = leftWeight;

		splitRightListView = (ListView) view.findViewById(R.id.splitRightListView);
		((LinearLayout.LayoutParams) splitRightListView.getLayoutParams()).weight = rightWeight;

		leftAdapter = new SplitListViewAdapter(splitItems);

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		this.getDialog().setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dismiss();

					return true;
				}

				return false;
			}
		});

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		splitItems.clear();
		rightList.clear();

		// 右边ListView初始数据
		if (allRightList.size() > 0) {
			rightList.addAll(allRightList.get(0));
		}

		// 设置标题栏
		splitLeftListTitle.setText(title);

		// 设置附标题栏
		splitLeftText.setText(splitLeftStr);
		splitRightText.setText(splitRightStr);

		if (!rightListItemSingleLine) {
			rightAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_split_item, rightList);
		} else {
			rightAdapter = new ArrayAdapter<String>(getActivity(), R.layout.simple_list_split_item_single_line, rightList);
		}
		splitRightListView.setAdapter(rightAdapter);

		// 初始化左边的数据模型列表
		for (String string : leftList) {
			SplitItem splitItem = new SplitItem();
			splitItem.value = string;
			splitItem.isChoiced = false;

			splitItems.add(splitItem);
		}

		// 若有数据，默认显示第一个数据
		if (splitItems.size() > 0) {
			splitItems.get(0).isChoiced = true;
			leftValue = splitItems.get(0).value;
		}

		splitLeftListView.setAdapter(leftAdapter);
		splitLeftListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				rightList.clear();
				rightList.addAll(allRightList.get(arg2));

				for (int i = 0; i < splitItems.size(); i++) {
					splitItems.get(i).isChoiced = false;
					if (i == arg2) {
						splitItems.get(i).isChoiced = true;
						leftValue = splitItems.get(i).value;
						leftPos = i;
					}
				}

				rightAdapter.notifyDataSetChanged();
				leftAdapter.notifyDataSetChanged();

			}
		});

		splitRightListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int rightPos, long arg3) {
				String vaule = arg0.getItemAtPosition(rightPos).toString();
				if (splitListViewPositiveClick != null) {
					splitListViewPositiveClick.onSplitListViewPositiveClick(leftValue, vaule, leftPos, rightPos);
				}

				dismiss();
			}
		});
	}

	public void setLeftLayoutWeight(int weight) {
		leftWeight = weight;
	}

	public void setRightLayoutWeight(int weight) {
		rightWeight = weight;

	}

	class SplitListViewAdapter extends BaseAdapter {
		private final List<SplitItem> splitItems;

		public SplitListViewAdapter(List<SplitItem> splitItems) {
			this.splitItems = splitItems;
		}

		@Override
		public int getCount() {
			return splitItems.size();
		}

		@Override
		public Object getItem(int position) {
			return splitItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getActivity().getLayoutInflater().inflate(R.layout.simple_list_split_item, null);
			TextView textView = (TextView) convertView.findViewById(R.id.splitListViewItemText);
			textView.setText(splitItems.get(position).value);

			convertView.setBackgroundResource(splitItems.get(position).isChoiced ? R.color.default_light_blue : R.color.white);

			return convertView;
		}
	}

	class SplitItem {
		public boolean isChoiced;
		public String value;
	}

	public interface SplitListViewPositiveClick {
		/**
		 * 选中事件监听
		 * 
		 * @param leftListValue
		 *            左边List选中的值
		 * @param rightListValue
		 *            右边List选中的值
		 * @param leftPos
		 *            左边List选中的位置
		 * @param rightPos
		 *            右边List选中的位置
		 */
		void onSplitListViewPositiveClick(String leftListValue, String rightListValue, int leftPos, int rightPos);
	}

}
