package com.mapgis.mmt.common.widget.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.adapter.TreeAdapter;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.entity.Node;

import java.util.List;

public class ListTreeDialogFragment extends DialogFragment {

	private Context context;
	private ListView listView;
	private Node treeRootNode;
	private int defaultExpandLevel;
	private final String title;
	private boolean isShowCheckBox = false;
	private OnListTreeItemClickListener listTreeItemClickListener;
	private OnRightButtonClickListener onRightButtonClickListener;

	public ListTreeDialogFragment(Context context, String title, Node treeRootNode, int defaultExpandLevel) {
		this(context, title, treeRootNode, defaultExpandLevel, false);
	}

	public ListTreeDialogFragment(Context context, String title, Node treeRootNode, int defaultExpandLevel, boolean isShowCheckBox) {
		this.context = context;
		this.title = title;
		this.treeRootNode = treeRootNode;
		this.defaultExpandLevel = defaultExpandLevel;
		this.isShowCheckBox = isShowCheckBox;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.listdialog, container, false);

		((TextView) v.findViewById(R.id.listDialogTitle)).setText(title);

		if (isShowCheckBox) {
			v.findViewById(R.id.layoutOkCancel).setVisibility(View.VISIBLE);

			v.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					List<Node> nodes = ((TreeAdapter) listView.getAdapter()).getSeletedLeafNodes();

					v.setTag(nodes);

					if (onRightButtonClickListener != null) {
						onRightButtonClickListener.onRightButtonClick(v);
					}

					dismiss();
				}
			});

			v.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		} else {
			v.findViewById(R.id.layoutOkCancel).setVisibility(View.GONE);
		}

		listView = (ListView) v.findViewById(R.id.listDialog);

		TreeAdapter ta = new TreeAdapter(context, treeRootNode);

		ta.setCheckBox(isShowCheckBox);// 设置整个树是否显示复选框
		ta.setExpandedCollapsedIcon(R.drawable.tree_ex, R.drawable.tree_ec);// 设置展开和折叠时图标
		ta.setExpandLevel(defaultExpandLevel);// 设置默认展开级别

		listView.setAdapter(ta);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Node clickNode = (Node) ((TreeAdapter.ViewHolder) view.getTag()).mutilTreeItemSelect.getTag();

				if (clickNode.isLeaf() && !isShowCheckBox) {
					listTreeItemClickListener.onListItemClick(position, clickNode.getParent().isRoot() ? "" : clickNode.getParent()
							.getText(), clickNode.getText());
					dismiss();
				} else {
					// 根据点击的项是否 是 叶子节点 来重置 ListView 显示的数据源 ，
					// 写在点击事件处理的后面，避免处理的是更新之后的数据源
					((TreeAdapter) parent.getAdapter()).ExpandOrCollapse(position);
				}
			}
		});

		getDialog().requestWindowFeature(STYLE_NO_TITLE);

		return v;
	}

	public void setListTreeItemClickListener(OnListTreeItemClickListener listTreeItemClickListener) {
		this.listTreeItemClickListener = listTreeItemClickListener;
	}

	public void setOnRightButtonClickListener(OnRightButtonClickListener onRightButtonClickListener) {
		this.onRightButtonClickListener = onRightButtonClickListener;
	}

	public interface OnListTreeItemClickListener {
		void onListItemClick(int arg2, String parentNodeText, String currentNodeText);
	}
}
