package com.mapgis.mmt.common.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.entity.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class TreeViewAdapter extends BaseExpandableListAdapter {
	public static final int ItemHeight = 40;// 每项的高度
	public static final int PaddingLeft = 36;// 每项的高度
	private int myPaddingLeft = 0;// 如果是由SuperTreeView调用，则作为子项需要往右移
	public List<List<Boolean>> isCheckedList;

	static public class TreeNode {
		public Object parent;
		public List<Object> childs = new ArrayList<Object>();
	}

	List<TreeNode> treeNodes = new ArrayList<TreeNode>();
	Context parentContext;

	public TreeViewAdapter(Context view, int myPaddingLeft, List<List<Boolean>> isCheckedList) {
		this.parentContext = view;
		this.myPaddingLeft = myPaddingLeft;
		this.isCheckedList = isCheckedList;
	}

	public TreeViewAdapter(Context view, int myPaddingLeft) {
		this.parentContext = view;
		this.myPaddingLeft = myPaddingLeft;
	}

	public List<TreeNode> GetTreeNode() {
		return treeNodes;
	}

	public void UpdateTreeNode(List<TreeNode> nodes) {
		treeNodes = nodes;
	}

	public void RemoveAll() {
		treeNodes.clear();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return treeNodes.get(groupPosition).childs.get(childPosition);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return treeNodes.get(groupPosition).childs.size();
	}

	public TextView getTextView(Context context) {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		textView.setBackgroundResource(R.color.default_light_dark);
		textView.setTextAppearance(context, R.style.default_text_large);
		textView.setMinHeight(DimenTool.dip2px(context, 40));
		return textView;
	}

	static public CheckBox getCheckBox(Context context) {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		CheckBox textView = new CheckBox(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		return textView;
	}

	private int drawableId = R.drawable.user_unselected;

	/**
	 * 
	 * @param context
	 * @param text
	 *            TextView要显示的信息
	 * @param groupPosition
	 *            父节点位置
	 * @param childPosition
	 *            子节点位置
	 * @return 线性布局View，模拟的一个CheckBox
	 */
	public LinearLayout getImageCheckBox(Context context, String text, final int groupPosition, final int childPosition) {
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		layout.setLayoutParams(params);
		layout.setBackgroundResource(R.color.white);

		LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		final ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(cParams);
		if (isCheckedList != null) {
			if (isCheckedList.get(groupPosition).get(childPosition)) {
				drawableId = R.drawable.user_selected;
			} else {
				drawableId = R.drawable.user_unselected;
			}
		}
		imageView.setBackgroundResource(drawableId);

		layout.addView(imageView);

		TextView textView = new TextView(context);
		textView.setLayoutParams(cParams);
		textView.setText(text);
		textView.setTextAppearance(context, R.style.default_text_medium_1);

		layout.addView(textView);

		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (drawableId == R.drawable.user_selected) {
					drawableId = R.drawable.user_unselected;
					isCheckedList.get(groupPosition).set(childPosition, false);
				} else {
					drawableId = R.drawable.user_selected;
					isCheckedList.get(groupPosition).set(childPosition, true);
				}
				imageView.setBackgroundResource(drawableId);
			}
		});

		return layout;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		LinearLayout v = getImageCheckBox(this.parentContext, "  "
				+ ((UserInfo) getChild(groupPosition, childPosition)).getTruename().toString(), groupPosition, childPosition);
		v.setPadding(myPaddingLeft + PaddingLeft, 15, 0, 15);
		return v;

	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		TextView textView = getTextView(this.parentContext);
		textView.setText(getGroup(groupPosition).toString());
		textView.setPadding(myPaddingLeft + 60, 0, 0, 0);
		return textView;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return treeNodes.get(groupPosition).parent;
	}

	@Override
	public int getGroupCount() {
		return treeNodes.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public List<UserInfo> getChooseUsers() {
		List<UserInfo> usersInfos = new ArrayList<UserInfo>();
		for (int i = 0; i < isCheckedList.size(); i++) {
			for (int j = 0; j < isCheckedList.get(i).size(); j++) {
				if (isCheckedList.get(i).get(j)) {
					usersInfos.add(((UserInfo) getChild(i, j)));
				}
			}
		}
		return usersInfos;
	}
}