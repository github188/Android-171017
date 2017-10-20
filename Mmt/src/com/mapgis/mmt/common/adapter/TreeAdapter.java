package com.mapgis.mmt.common.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.entity.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 树数据源构造器
 * 
 * @author LongShao
 * 
 */
public class TreeAdapter extends BaseAdapter {

	private final Context con;
	private final LayoutInflater lif;
	private final List<Node> allsCache = new ArrayList<Node>();
	private final List<Node> alls = new ArrayList<Node>();
	private final TreeAdapter oThis = this;
	private boolean hasCheckBox = true;// 是否拥有复选框
	private int expandedIcon = -1;
	private int collapsedIcon = -1;

	/**
	 * TreeAdapter构造函数
	 * 
	 * @param context
	 * @param rootNode
	 *            根节点
	 */
	public TreeAdapter(Context context, Node rootNode) {
		this.con = context;
		this.lif = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addNode(rootNode);
	}

	private void addNode(Node node) {
		alls.add(node);
		allsCache.add(node);
		if (node.isLeaf()) {
			return;
		}
		for (int i = 0; i < node.getChildren().size(); i++) {
			addNode(node.getChildren().get(i));
		}
	}

	// 复选框联动
	private void checkNode(Node node, boolean isChecked) {
		node.setChecked(isChecked);
		for (int i = 0; i < node.getChildren().size(); i++) {
			checkNode(node.getChildren().get(i), isChecked);
		}

		// 瓦片数据同时只能显示一个
		if (node.getParent() != null && node.getParent().getValue().equals("000000") && node.getChildren().size() == 0
				&& isChecked) {
			for (Node node2 : getAll()) {
				if (node2.getParent() != null && node2.getParent().getValue().equals("000000") && node2.getChildren().size() == 0
						&& !node2.equals(node)) {
					node2.setChecked(false);
				}
			}
		}
	}

	/**
	 * 获得选中节点
	 * 
	 * @return
	 */
	public List<Node> getSeletedNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (n.isChecked()) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * 获得选中叶子节点
	 * 
	 * @return
	 */
	public List<Node> getSeletedLeafNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (n.isChecked() && n.isLeaf()) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * 获得未选中节点
	 * 
	 * @return
	 */
	public List<Node> getUnSeletedNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (!n.isChecked()) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/***
	 * 取所有节点
	 * 
	 * @return
	 */
	public List<Node> getAll() {
		return this.allsCache;
	}

	/***
	 * 根据value取值,取到为第一个满足的项
	 * 
	 * @param value
	 * @return
	 */
	public Node getNode(String value) {
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (n.getValue() == value) {
				return n;
			}
		}
		return null;
	}

	// 控制节点的展开和折叠
	private void filterNode() {
		alls.clear();
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (!n.isParentCollapsed() || n.isRoot()) {
				alls.add(n);
			}
		}
	}

	/**
	 * 设置是否拥有复选框
	 * 
	 * @param hasCheckBox
	 */
	public void setCheckBox(boolean hasCheckBox) {
		this.hasCheckBox = hasCheckBox;
	}

	/**
	 * 设置展开和折叠状态图标
	 * 
	 * @param expandedIcon
	 *            展开时图标
	 * @param collapsedIcon
	 *            折叠时图标
	 */
	public void setExpandedCollapsedIcon(int expandedIcon, int collapsedIcon) {
		this.expandedIcon = expandedIcon;
		this.collapsedIcon = collapsedIcon;
	}

	/**
	 * 设置展开级别
	 * 
	 * @param level
	 */
	public void setExpandLevel(int level) {
		alls.clear();
		for (int i = 0; i < allsCache.size(); i++) {
			Node n = allsCache.get(i);
			if (n.getLevel() <= level) {
				if (n.getLevel() < level) {// 上层都设置展开状态
					n.setExpanded(true);
				} else {// 最后一层都设置折叠状态
					n.setExpanded(false);
				}
				alls.add(n);
			}
		}
		this.notifyDataSetChanged();
	}

	/**
	 * 控制节点的展开和收缩
	 * 
	 * @param position
	 */
	public void ExpandOrCollapse(int position) {
		Node n = alls.get(position);
		if (n != null) {
			if (!n.isLeaf()) {
				n.setExpanded(!n.isExpanded());
				filterNode();
				this.notifyDataSetChanged();
			} else {
				checkNode(n, !n.isChecked());
				oThis.notifyDataSetChanged();
			}
		}
	}

	@Override
	public int getCount() {
		return alls.size();
	}

	@Override
	public Object getItem(int position) {
		return alls.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		if (view == null) {
			view = this.lif.inflate(R.layout.mutil_tree_item, null);
			holder = new ViewHolder();
			holder.mutilTreeItemSelect = (CheckBox) view.findViewById(R.id.mutilTreeItemSelect);

			// 复选框单击事件
			holder.mutilTreeItemSelect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO
					Node n = (Node) v.getTag();
					checkNode(n, ((CheckBox) v).isChecked());
					oThis.notifyDataSetChanged();
				}

			});
			holder.mutilTreeItemIcon = (ImageView) view.findViewById(R.id.mutilTreeItemIcon);
			holder.mutilTreeItemText = (TextView) view.findViewById(R.id.mutilTreeItemText);
			holder.mutilTreeItemExEc = (ImageView) view.findViewById(R.id.mutilTreeItemExEc);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// 得到当前节点
		Node n = alls.get(position);

		if (n != null) {
			holder.mutilTreeItemSelect.setTag(n);
			holder.mutilTreeItemSelect.setChecked(n.isChecked());

			// 是否显示复选框
			if (n.hasCheckBox() && hasCheckBox) {
				holder.mutilTreeItemSelect.setVisibility(View.VISIBLE);
			} else {
				holder.mutilTreeItemSelect.setVisibility(View.GONE);
			}

			// 是否显示图标
			if (n.getIcon() == -1) {
				holder.mutilTreeItemIcon.setVisibility(View.GONE);
			} else {
				holder.mutilTreeItemIcon.setVisibility(View.VISIBLE);
				holder.mutilTreeItemIcon.setImageResource(n.getIcon());
			}

			// 显示文本
			String value = n.getText().toString();
			if (value.contains("<font")) {
				value = value.substring(value.indexOf(">") + 1, value.lastIndexOf("<"));
			}
			holder.mutilTreeItemText.setText(value);

			if (n.isLeaf()) {
				// 是叶节点 不显示展开和折叠状态图标
				holder.mutilTreeItemExEc.setVisibility(View.GONE);
				holder.mutilTreeItemText.setTextSize(14f);
				holder.mutilTreeItemText.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			} else {
				// 单击时控制子节点展开和折叠,状态图标改变
				holder.mutilTreeItemExEc.setVisibility(View.VISIBLE);
				holder.mutilTreeItemText.setTextSize(18f);
				holder.mutilTreeItemText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				if (n.isExpanded()) {
					if (expandedIcon != -1) {
						holder.mutilTreeItemExEc.setImageResource(expandedIcon);
					}
				} else {
					if (collapsedIcon != -1) {
						holder.mutilTreeItemExEc.setImageResource(collapsedIcon);
					}
				}

			}

			// 控制缩进
			view.setPadding(35 * n.getLevel(), 3, 3, 3);

		}

		return view;
	}

	/**
	 * 
	 * 列表项控件集合
	 * 
	 */
	public class ViewHolder {
		public CheckBox mutilTreeItemSelect;// 选中与否
		public ImageView mutilTreeItemIcon;// 图标
		public TextView mutilTreeItemText;// 文本〉〉〉
		public ImageView mutilTreeItemExEc;// 展开或折叠标记">"或"v"

		public CheckBox getChbSelect() {
			return mutilTreeItemSelect;
		}

		public void setChbSelect(CheckBox chbSelect) {
			this.mutilTreeItemSelect = chbSelect;
		}

		public ImageView getIvIcon() {
			return mutilTreeItemIcon;
		}

		public void setIvIcon(ImageView ivIcon) {
			this.mutilTreeItemIcon = ivIcon;
		}

		public TextView getTvText() {
			return mutilTreeItemText;
		}

		public void setTvText(TextView tvText) {
			this.mutilTreeItemText = tvText;
		}

		public ImageView getIvExEc() {
			return mutilTreeItemExEc;
		}

		public void setIvExEc(ImageView ivExEc) {
			this.mutilTreeItemExEc = ivExEc;
		}

	}
}
