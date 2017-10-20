package com.mapgis.mmt.common.widget.tree;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mapgis.mmt.R;

import java.util.List;

/**
 * Created by Comclay on 2016/12/21.
 * 可选的树布局适配器
 */

public abstract class TreeSelectableAdapter<T extends Parcelable> extends BaseExpandableListAdapter {
    protected Context mContext;
    protected List<ParentTreeNode<T>> mNodeList;
    private int[] mCount;

    public TreeSelectableAdapter(Context context, List<ParentTreeNode<T>> mNodeList) {
        this.mContext = context;
        this.mNodeList = mNodeList;

        initCount(mNodeList);
    }

    private void initCount(List<ParentTreeNode<T>> mNodeList) {
        mCount = new int[getGroupCount()];
        ParentTreeNode parentTreeNode;
        for (int i = 0; i < mCount.length; i++) {
            parentTreeNode = mNodeList.get(i);
            mCount[i] = mNodeList.get(i).getSelectedCount();
//            parentTreeNode.setSelectCount(mCount[i]);
            if (mCount[i] == 0) {
                parentTreeNode.setSelectedState(ParentTreeNode.State.SELECT_NONE.getState());
            } else if (mCount[i] == parentTreeNode.getChildNodeSize()) {
                parentTreeNode.setSelectedState(ParentTreeNode.State.SELECT_ALL.getState());
            } else {
                parentTreeNode.setSelectedState(ParentTreeNode.State.SELECT_PART.getState());
            }
        }
    }

    @Override
    public int getGroupCount() {
        return mNodeList == null ? 0 : mNodeList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mNodeList.get(groupPosition).getChildNodeSize();
    }

    @Override
    public ParentTreeNode<T> getGroup(int groupPosition) {
        return mNodeList.get(groupPosition);
    }

    @Override
    public ChildTreeNode getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getChild(childPosition);
    }

    /**
     * 父布局
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_selectable_group_view, null);
            groupHolder = new GroupHolder((LinearLayout) convertView
                    , getGroupItemView(groupPosition, isExpanded, (ViewGroup) convertView));
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        groupHolder.setPatrentNode(getGroup(groupPosition));
        initGroupValue(groupHolder, groupPosition, isExpanded);
        // 组布局
        return convertView;
    }

    /**
     * 自定义的布局
     */
    protected abstract View getGroupItemView(int groupPosition, boolean isExpanded, ViewGroup parentView);

    /**
     * 需要重写该方法来实现自定义的界面初始化
     */
    protected abstract void initGroupView(GroupHolder groupHolder, int groupPosition);

    /**
     * 初始化GroupItem，子类需继承该方法
     */
    protected void initGroupValue(final GroupHolder groupHolder, final int groupPosition, final boolean isExpanded) {
        final ParentTreeNode parentTreeNode = mNodeList.get(groupPosition);
        // 父类所做的操作
        groupHolder.toggleSelectImg(parentTreeNode.getSelectedState());

        // 扩展状态
        if (isExpanded) {
            groupHolder.ivExpandIcon.setImageResource(R.drawable.list_open);
        } else {
            // 添加旋转动画
            groupHolder.ivExpandIcon.setImageResource(R.drawable.list_close);
        }

        groupHolder.ivSelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentTreeNode.setSelected();
            }
        });

        /**
         * 直接设置全选和全部选时的监听
         */
        parentTreeNode.setOnSelecStateChangeListener(new TreeNode.OnSelectStateChangeListener() {
            @Override
            public void onSelectedStateChangeListener(boolean isSelected) {
                // 初始化
                if (isSelected) {
                    mCount[groupPosition] = parentTreeNode.getChildNodeSize();
//                    parentTreeNode.setSelectCount(parentTreeNode.getChildNodeSize());
                } else {
                    mCount[groupPosition] = 0;
//                    parentTreeNode.setSelectCount(0);
                }

                if (!parentTreeNode.equals(groupHolder.getPatrentNode())){
                    return;
                }

                groupHolder.toggleSelectImg(parentTreeNode.getSelectedState());
            }
        });

        // 设置三种不同状态的监听，这个一般有其自条目触发，因此不用通知自条目更新
        parentTreeNode.setOnParentSelecStateChangeListener(new ParentTreeNode.OnParentSelectStateChangeListener() {
            @Override
            public void onParentSelectedStateChangeListener(boolean isSelected, int state) {
                if (!parentTreeNode.equals(groupHolder.getPatrentNode())){
                    return;
                }
                groupHolder.toggleSelectImg(parentTreeNode.getSelectedState());
            }
        });

        // 子类所做的操作
        initGroupView(groupHolder, groupPosition);
    }

    /**
     * 子布局
     */
    @Override
    public View getChildView(int groupPosition, int childPosition
            , boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_selectable_child_view, null);
            childHolder = new ChildHolder((LinearLayout) convertView
                    , getChildItemView(groupPosition, childPosition, (ViewGroup) convertView));
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }
        childHolder.setChildNode(getChild(groupPosition,childPosition));
        initChildValue(childHolder, groupPosition, childPosition);
        // 组布局
        return convertView;
    }

    protected void initChildValue(final ChildHolder childHolder, final int groupPosition, final int childPosition) {
        final ChildTreeNode childTreeNode = getChild(groupPosition, childPosition);

        childTreeNode.setOnSelecStateChangeListener(new TreeNode.OnSelectStateChangeListener() {
            @Override
            public void onSelectedStateChangeListener(boolean isSelected) {
                if (childTreeNode.equals(childHolder.getChildNode())){
                    childHolder.toggleSelectImg(isSelected);
                }

                if (TreeSelectableAdapter.this.mOnChildSelectChangedListener != null) {
                    mOnChildSelectChangedListener.onChildSelectChanged(childTreeNode, isSelected);
                }
                // 由ParentTreeNode调用notifyStateChanged()方法触发的ChildTreeNode的监听不执行余下操作
                ParentTreeNode<T> group = getGroup(groupPosition);
                if (group.isNotify()) {
                    return;
                }

                if (isSelected) {
                    mCount[groupPosition]++;
//                    group.setSelectCount(group.getSelectedCount() + 1);
                } else {
                    mCount[groupPosition]--;
//                    group.setSelectCount(group.getSelectedCount() - 1);
                }


                if (mCount[groupPosition] == 0) {
                    group.setSelectedState(ParentTreeNode.STATE_SELECT_NONE);
                } else if (mCount[groupPosition] == group.getChildNodeSize()) {
                    group.setSelectedState(ParentTreeNode.STATE_SELECT_ALL);
                } else {
                    group.setSelectedState(ParentTreeNode.STATE_SELECT_PART);
                }
            }
        });

        childHolder.ivSelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                childTreeNode.setSelected();
            }
        });

//        childHolder.ivSelectIcon.performClick();

        // 父类所做的操作
        childHolder.toggleSelectImg(childTreeNode.isSelected());

        // 子类所做的操作
        initChildView(childHolder, groupPosition, childPosition);
    }

    protected abstract View getChildItemView(int groupPosition, int childPosition, ViewGroup parentView);

    protected abstract void initChildView(ChildHolder childHolder, int groupPosition, int childPosition);

    /**
     * 子布局可以被选中
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition * 1000;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getGroupId(groupPosition) + childPosition + 1;
    }

    protected static class GroupHolder {
        private ParentTreeNode patrentNode;
        public LinearLayout groupView;
        public ImageView ivExpandIcon;
        public ImageView ivSelectIcon;
        public View view;

        public GroupHolder(LinearLayout groupView, View view) {
            this.groupView = groupView;
            this.view = view;
            ivExpandIcon = (ImageView) groupView.findViewById(R.id.iv_expand_icon);
            ivSelectIcon = (ImageView) groupView.findViewById(R.id.iv_select_checkbox);

            // 将自定义的view添加到父布局中
            if (view.getRootView() == null) {
                groupView.addView(view);
            }
        }

        public ParentTreeNode getPatrentNode() {
            return patrentNode;
        }

        public void setPatrentNode(ParentTreeNode patrentNode) {
            this.patrentNode = patrentNode;
        }

        public void toggleSelectImg(int state) {
            int drawableId;
            switch (state) {
                case ParentTreeNode.STATE_SELECT_PART:      // 选择部分，所对应的isSelected属性和全部选中时一样都为true
                    drawableId = R.drawable.checkbox_part;
                    break;
                case ParentTreeNode.STATE_SELECT_ALL:       // 全部选中
                    drawableId = R.drawable.checkbox_all;
                    break;
                case ParentTreeNode.STATE_SELECT_NONE:      // 没有选中
                default:
                    drawableId = R.drawable.checkbox_none;
            }
            this.ivSelectIcon.setImageResource(drawableId);
        }
    }

    protected static class ChildHolder {
        private ChildTreeNode childNode;
        public LinearLayout groupView;
        public ImageView ivSelectIcon;
        public View view;

        public ChildHolder(LinearLayout groupView, View view) {
            this.groupView = groupView;
            this.view = view;
            ivSelectIcon = (ImageView) groupView.findViewById(R.id.iv_select_checkbox);

            // 将自定义的view添加到父布局中
            if (view.getRootView() == null) {
                groupView.addView(view);
            }
        }

        public ChildTreeNode getChildNode() {
            return childNode;
        }

        public void setChildNode(ChildTreeNode childNode) {
            this.childNode = childNode;
        }

        public void toggleSelectImg(boolean selected) {
            int drawableId;
            if (selected) {
                drawableId = R.drawable.checkbox_all;
            } else {
                drawableId = R.drawable.checkbox_none;
            }
            this.ivSelectIcon.setImageResource(drawableId);
        }
    }

    /**
     * 全部选中
     */
    public void selectAll() {
        for (ParentTreeNode<T> node :
                mNodeList) {
            node.setSelected(true);
        }
    }

    /**
     * 全不选
     */
    public void selectNone() {
        for (ParentTreeNode<T> node : mNodeList) {
            node.setSelected(false);
        }
    }

    private OnChildSelectChangedListener mOnChildSelectChangedListener;

    public void setOnChildSelectChangedListener(OnChildSelectChangedListener listener) {
        this.mOnChildSelectChangedListener = listener;
    }

    public interface OnChildSelectChangedListener {
        void onChildSelectChanged(ChildTreeNode childNode, boolean select);
    }
}
