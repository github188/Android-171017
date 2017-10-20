package com.mapgis.mmt.common.widget.treeview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/3/5.
 * 多技树形选择列表的适配器
 */

public abstract class MultiTreeAdapter<K extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<MultiTreeAdapter.TreeViewHolder<K>> {
    private static final String TAG = "MultiTreeAdapter";
    protected LayoutInflater inflater;
    // 可见的nodeList
    protected List<TreeNode> nodeList;
    // 所有的nodeList
    protected List<TreeNode> allNodeList;

    // 默认缩进20
    private int indent = DimenTool.dip2px(MyApplication.getInstance().getApplicationContext(), 16);

    public MultiTreeAdapter(LayoutInflater inflater, List<TreeNode> allNodeList) {
        this.inflater = inflater;
        this.allNodeList = allNodeList;
        List<TreeNode> nodeList = TreeNodeUtil.formatNodeList(allNodeList);
        this.nodeList = TreeNodeUtil.filterVisibleNode(nodeList);
        Log.i(TAG, "所有可见的数据有: \n" + this.nodeList.toString());
    }

    protected TreeNode getNode(int position){
        return nodeList.get(position);
    }

    @Override
    public TreeViewHolder<K> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tree_view, parent, false);
        TreeViewHolder<K> treeViewHolder = new TreeViewHolder<>(view);
        K kHolder = onCreateContentHolder(parent, viewType);
        treeViewHolder.setContentHolder(kHolder);

        setViewStyle(view, viewType);
        return treeViewHolder;
    }

    protected void setViewStyle(View itemView, int viewType) {
    }

    public abstract K onCreateContentHolder(ViewGroup parent, int viewType);

    boolean isFlag = false;

    /*@Override
    public void onViewAttachedToWindow(TreeViewHolder<K> holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = holder.contentHolder.itemView.getHeight();
        holder.itemView.setLayoutParams(layoutParams);
    }*/

    @Override
    public void onBindViewHolder(final TreeViewHolder<K> holder, final int position) {
        if (position != 0) {
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.topMargin = DimenTool.dip2px(MyApplication.getInstance().getApplicationContext(), 1);
            holder.itemView.setLayoutParams(layoutParams);
        }
        final TreeNode treeNode = nodeList.get(position);
        holder.itemView.setPadding(indent * treeNode.getLevel()
                , holder.itemView.getPaddingTop()
                , holder.itemView.getPaddingRight()
                , holder.itemView.getPaddingBottom());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!treeNode.isHideExpand() && treeNode.getChildrenCount() == 0) {
                    // 此处留一个
                    delayExpandNode(treeNode, holder);
                    return;
                }
                expandNode(treeNode, holder);
            }
        });

        if (treeNode.isHideChecked()) {
            // 隐藏选中框
            holder.setCheckBoxVisibility(false);
        } else {
            holder.setCheckBoxVisibility(true);
            holder.threeStatusCheckBox.setStatus(treeNode.getCheckStatus());
            if (treeNode.isCheckable()) {
                holder.threeStatusCheckBox.setCheckable(true);
                holder.threeStatusCheckBox.setOnCheckedChangeListener(new ThreeStatusCheckBox.OnCheckedChangeListener() {
                    @Override
                    public void onChenkedChange(int status) {
                        isFlag = true;
                        TreeNodeUtil.setCheckedStatus(treeNode, status);
                        notifyDataSetChanged();
                        isFlag = false;
                        if (mOnCheckChangedListener != null){
                            mOnCheckChangedListener.onCheckeChanged(holder,position,status);
                        }
                    }
                });
            } else {
                holder.threeStatusCheckBox.setCheckable(false);
            }
        }
        if (treeNode.isHideExpand()) {
            // 隐藏展开图标
            holder.setExpandIconVisibility(false);
        } else {
            holder.setExpandIconVisibility(true);
            holder.setExpandIcon(treeNode.isExpand());
            if (treeNode.isExpandable()) {
                if (treeNode.isLeaf()) {
                    holder.ibExpandIcon.setOnClickListener(null);
                } else {
                    holder.ibExpandIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            expandNode(treeNode, holder);
                        }
                    });
                }
            } else {
                holder.ibExpandIcon.setOnClickListener(null);
            }

        }

        onBindContentHolder(holder.contentHolder, position);
    }

    /**
     * 延时展开节点
     * 当该节点的子节点为空的时候，可以让用户在子类中实现延时展开某个节点
     *
     * @param treeNode 节点
     * @param holder   TreeViewHolder
     */
    protected void delayExpandNode(TreeNode treeNode, TreeViewHolder<K> holder) {

    }

    ;

    protected void expandNode(TreeNode treeNode, TreeViewHolder<K> holder) {
        //                    if (isFlag) return;
        boolean expand = treeNode.isExpand();
        holder.setExpandIcon(!expand);
        int adapterPosition = nodeList.indexOf(treeNode);
        if (!expand) {
            // 展开状态
            TreeNodeUtil.expandNode(treeNode);
            nodeList.addAll(adapterPosition + 1, treeNode.getChildrenNodes());
            notifyItemRangeInserted(adapterPosition + 1, treeNode.getChildrenCount());
        } else {
            // 合并状态
            int count = TreeNodeUtil.collapseNode(treeNode);
            notifyItemChanged(adapterPosition);
            List<TreeNode> clearList = new ArrayList<>();
            for (int i = 1; i < count + 1; i++) {
                clearList.add(nodeList.get(adapterPosition + i));
            }
            nodeList.removeAll(clearList);
            notifyItemRangeRemoved(adapterPosition + 1, count);
        }
    }

    public void getView(int position) {

    }

    public abstract void onBindContentHolder(K holder, int position);

    @Override
    public int getItemCount() {
        return nodeList == null ? 0 : nodeList.size();
    }

    public static class TreeViewHolder<T extends RecyclerView.ViewHolder> extends RecyclerView.ViewHolder {
        private ThreeStatusCheckBox threeStatusCheckBox;
        private ViewGroup contentLayout;
        //        private RadioButton rbExpandIcon;
        private ImageButton ibExpandIcon;
        private T contentHolder;

        TreeViewHolder(View itemView) {
            super(itemView);
            this.threeStatusCheckBox = (ThreeStatusCheckBox) itemView.findViewById(
                    R.id.threeStatusCheckBox);
            this.contentLayout = (ViewGroup) itemView.findViewById(R.id.layout_content);
//            this.rbExpandIcon = (RadioButton) itemView.findViewById(R.id.rb_expand);
            this.ibExpandIcon = (ImageButton) itemView.findViewById(R.id.ib_expand);
        }

        public T getContentHolder() {
            return contentHolder;
        }

        /**
         * 设置checkbox的可见性
         */
        void setCheckBoxVisibility(boolean isShow) {
            if (isShow) {
                this.threeStatusCheckBox.setVisibility(View.VISIBLE);
            } else {
                this.threeStatusCheckBox.setVisibility(View.GONE);
            }
        }

        void setExpandIcon(boolean isExpand) {
//            this.rbExpandIcon.setSelected(isExpand);
            if (isExpand) {
                this.ibExpandIcon.setImageResource(R.drawable.arrow_down);
            } else {
                this.ibExpandIcon.setImageResource(R.drawable.arrow_up);
            }
        }

        void setExpandIconVisibility(boolean isShow) {
            if (isShow) {
                this.ibExpandIcon.setVisibility(View.VISIBLE);
            } else {
                this.ibExpandIcon.setVisibility(View.GONE);
            }
        }

        void setContent(View view) {
            this.contentLayout.addView(view);
        }

        void setContentHolder(T contentHolder) {
            this.contentHolder = contentHolder;
            this.contentLayout.addView(contentHolder.itemView);
        }
    }

    private OnCheckeChangedListener mOnCheckChangedListener;

    public void setOnCheckChangedListener(OnCheckeChangedListener listener) {
        this.mOnCheckChangedListener = listener;
    }

    public interface OnCheckeChangedListener {
        void onCheckeChanged(TreeViewHolder viewHolder, int position, int status);
    }
}
