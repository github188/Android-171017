package com.mapgis.mmt.common.widget.tree;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2016/12/21.
 * 父节点
 */
public class ParentTreeNode<T extends Parcelable> extends TreeNode<T> implements Parcelable {
    public static final int STATE_SELECT_NONE = 0;
    public static final int STATE_SELECT_PART = 1;
    public static final int STATE_SELECT_ALL = 2;

    public enum State {
        SELECT_NONE(0), SELECT_PART(1), SELECT_ALL(2);

        private int state;

        State(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }

    private int mSelectState = 0;
    /**
     * 节点列表，里面可以包裹ParentTreeNode本身
     */
    private List<ChildTreeNode> mNodeList;
    private int selectCount = 0;
    private OnParentSelectStateChangeListener parentListener;
    private boolean isNotify = false;

    public ParentTreeNode() {
    }

    public ParentTreeNode(T obj) {
        super(obj);
    }

    public ParentTreeNode(T obj, ArrayList<ChildTreeNode> list) {
        super(obj);
        this.mNodeList = list;
    }

    public int getChildNodeSize() {
        return mNodeList == null ? 0 : mNodeList.size();
    }

    public ChildTreeNode getChild(int childPosition) {
        return mNodeList.get(childPosition);
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public List<ChildTreeNode> getNodeList() {
        return mNodeList;
    }

    public void setNodeList(List<ChildTreeNode> mNodeList) {
        this.mNodeList = mNodeList;
    }

    public int getSelectCount() {
        return selectCount;
    }

    public void setSelectCount(int selectCount) {
        if (this.selectCount == selectCount) return;
        this.selectCount = selectCount;

        if (selectCount == 0) {
            this.setSelectedState(ParentTreeNode.STATE_SELECT_NONE);
        } else if (selectCount == this.getChildNodeSize()) {
            this.setSelectedState(ParentTreeNode.STATE_SELECT_ALL);
        } else {
            this.setSelectedState(ParentTreeNode.STATE_SELECT_PART);
        }
    }

    /**
     * 获取选中状态，全选，部分选，不选
     */
    public int getSelectedState() {
        return mSelectState;
    }

    public void setSelectedState(int state) {
        if (mSelectState == state) {
            return;
        }
        this.mSelectState = state;
        if (state == STATE_SELECT_ALL || state == STATE_SELECT_PART) {
            isSelected = true;
        } else if (state == STATE_SELECT_NONE) {
            isSelected = false;
        }
        if (listener != null) {
            parentListener.onParentSelectedStateChangeListener(isSelected, state);
        }
    }

    public void setSelected() {
        setSelected(!isSelected);
    }

    @Override
    public void setSelected(boolean selected) {
        int state = 0;
        if (selected) {      // 全选
            /*if (mSelectState == STATE_SELECT_PART){
                mSelectState = STATE_SELECT_ALL;
                super.performListener();
                notifyStateChanged();
                return;
            }*/
            state = STATE_SELECT_ALL;
        } else {              // 全不选
            state = STATE_SELECT_NONE;
        }
        if (selected != isSelected || state != mSelectState) {
            isSelected = selected;
            mSelectState = state;
            performListener();
        }

        notifyStateChanged();
    }

    public void addChildNode(ChildTreeNode childTreeNode) {
        if (mNodeList == null) {
            mNodeList = new ArrayList<>();
        }
        mNodeList.add(childTreeNode);
    }

    public void notifyStateChanged() {
        isNotify = true;
        for (ChildTreeNode node : mNodeList) {
            node.setSelected(isSelected);
        }
        isNotify = false;
    }

    public int getSelectedCount() {
        int count = 0;
        for (ChildTreeNode node : mNodeList) {
            if (node.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public void setOnParentSelecStateChangeListener(OnParentSelectStateChangeListener listener) {
        this.parentListener = listener;
    }

    interface OnParentSelectStateChangeListener {
        void onParentSelectedStateChangeListener(boolean isSelected, int state);
    }

    class DefaultOnSelectStateChangeListener implements OnSelectStateChangeListener {

        @Override
        public void onSelectedStateChangeListener(boolean isSelected) {

        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mSelectState);
        dest.writeTypedList(this.mNodeList);
        dest.writeInt(this.selectCount);
        dest.writeByte(this.isNotify ? (byte) 1 : (byte) 0);
    }

    protected ParentTreeNode(Parcel in) {
        super(in);
        this.mSelectState = in.readInt();
        this.mNodeList = in.createTypedArrayList(ChildTreeNode.CREATOR);
        this.selectCount = in.readInt();
        this.isNotify = in.readByte() != 0;
    }

    public static final Creator<ParentTreeNode> CREATOR = new Creator<ParentTreeNode>() {
        @Override
        public ParentTreeNode createFromParcel(Parcel source) {
            return new ParentTreeNode(source);
        }

        @Override
        public ParentTreeNode[] newArray(int size) {
            return new ParentTreeNode[size];
        }
    };
}
