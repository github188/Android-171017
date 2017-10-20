package com.mapgis.mmt.common.widget.tree;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Comclay on 2016/12/21.
 * 树节点
 */

public class TreeNode<T extends Parcelable> implements Parcelable {
    // 节点是否被选中
    protected boolean isSelected = false;

    protected T object;

    protected OnSelectStateChangeListener listener;

//    protected ArrayList<OnSelectStateChangeListener> listenerList = new ArrayList<>();

    public TreeNode() {
    }

    public TreeNode(T object) {
        this.object = object;
    }

    public TreeNode(boolean isSelected, T object) {
        this.isSelected = isSelected;
        this.object = object;
    }

    public static final Creator<TreeNode> CREATOR = new Creator<TreeNode>() {
        @Override
        public TreeNode createFromParcel(Parcel in) {
            return new TreeNode(in);
        }

        @Override
        public TreeNode[] newArray(int size) {
            return new TreeNode[size];
        }
    };

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        if (selected != isSelected) {
            isSelected = selected;
            performListener();
        }
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public void setOnSelecStateChangeListener(OnSelectStateChangeListener listener) {
        this.listener = listener;
//        listenerList.add(listener);
    }

    public void performListener() {
        /*for (OnSelectStateChangeListener listener : listenerList) {
            if (listener == null) continue;
            listener.onSelectedStateChangeListener(isSelected);
        }*/
        if (listener != null){
            listener.onSelectedStateChangeListener(isSelected);
        }
    }

    interface OnSelectStateChangeListener {
        void onSelectedStateChangeListener(boolean isSelected);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.object, flags);
    }

    protected TreeNode(Parcel in) {
        this.isSelected = in.readByte() != 0;
        String dataName = in.readString();
        try {

            this.object = in.readParcelable(Class.forName(dataName).getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode<?> treeNode = (TreeNode<?>) o;

        if (isSelected != treeNode.isSelected) return false;
        if (object != null ? !object.equals(treeNode.object) : treeNode.object != null)
            return false;
        return listener != null ? listener.equals(treeNode.listener) : treeNode.listener == null;
    }

    @Override
    public int hashCode() {
        int result = (isSelected ? 1 : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (listener != null ? listener.hashCode() : 0);
        return result;
    }
}
