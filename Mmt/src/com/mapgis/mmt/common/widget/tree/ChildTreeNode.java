package com.mapgis.mmt.common.widget.tree;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Comclay on 2016/12/21.
 * 树节点
 */

public class ChildTreeNode<T extends Parcelable> extends TreeNode<T> implements Parcelable{

    public ChildTreeNode() {
    }

    public ChildTreeNode(T obj) {
        this.object = obj;
    }

    public void setSelected() {
        setSelected(!isSelected);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected ChildTreeNode(Parcel in) {
        super(in);
    }

    public  static final Creator<ChildTreeNode> CREATOR = new Creator<ChildTreeNode>() {
        @Override
        public ChildTreeNode createFromParcel(Parcel source) {
            return new ChildTreeNode(source);
        }

        @Override
        public ChildTreeNode[] newArray(int size) {
            return new ChildTreeNode[size];
        }
    };
}
