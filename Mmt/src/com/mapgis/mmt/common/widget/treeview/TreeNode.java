package com.mapgis.mmt.common.widget.treeview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/3/5.
 * 多级展开列表的节点数据
 */

public class TreeNode implements Comparable<TreeNode>{
    /**
     * 当前节点所对应的原始数据对象
     */
    private Object objs;
    private int id;
    private int pId;
    private TreeNode parent;
    private List<TreeNode> childrenNodes = new ArrayList<>();

    private boolean isExpand = false;
    private boolean isHideExpand = false;
    private boolean expandable = true;
    private int checkStatus = 0;
    private boolean checkable = true;
    private boolean isHideChecked = false;

    public TreeNode(int id, int pId, Object objs) {
        this.objs = objs;
        this.id = id;
        this.pId = pId;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public Object getObjs() {
        return objs;
    }

    public int getId() {
        return id;
    }

    public int getpId() {
        return pId;
    }

    public boolean isExpand() {
        return isExpand;
    }

    /**
     * 当父节点收起，其子节点也收起
     */
    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
//        if (!isExpand) {
//            for (TreeNode node : childrenNodes) {
//                node.setExpand(false);
//            }
//        }
    }

    public int getLevel() {
        return parent == null ? 0 : parent.getLevel() + 1;
    }

    public List<TreeNode> getChildrenNodes() {
        return childrenNodes;
    }

    public void setChildrenNodes(List<TreeNode> childrenNodes) {
        this.childrenNodes = childrenNodes;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    /**
     * 判断是否是根节点
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判断是否是叶子节点
     */
    public boolean isLeaf() {
        return childrenNodes.size() == 0;
    }


    /**
     * 判断父节点是否展开
     *
     * @return
     */
    public boolean isParentExpand() {
        if (parent == null)
            return false;
        return parent.isExpand();
    }


    public boolean isHideChecked() {
        return isHideChecked;
    }

    public void setHideChecked(boolean isHideChecked) {
        this.isHideChecked = isHideChecked;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public boolean isHideExpand() {
        return isHideExpand;
    }

    public void setHideExpand(boolean hideExpand) {
        isHideExpand = hideExpand;
    }

    /**
     * 获取当前节点的状态
     *
     * @param isReCal true需要重新计算，false不需要重新计算
     * @return
     */
    public int getCheckStatus(boolean isReCal) {
        // 影藏选中框的话就直接返回-1啦
        if (isHideChecked) {
            return -1;
        }
        int status = -1;
        if (!isReCal) {
            status = getCheckStatus();
        } else {
            List<TreeNode> childrenNodes = parent.getChildrenNodes();
            int selectCount = 0;
            for (TreeNode node : childrenNodes) {
                switch (node.getCheckStatus()) {
                    case ThreeStatusCheckBox.CHECK_NONE:
                        // 子条目中只要有没有选中的就要设置为半选
                        break;
                    case ThreeStatusCheckBox.CHECK_PART:
                        selectCount += 1;
                        break;
                    case ThreeStatusCheckBox.CHECK_ALL:
                        selectCount += 2;
                        break;
                }
            }
            if (selectCount == 0) {
                status = ThreeStatusCheckBox.CHECK_NONE;
            } else if (selectCount == 2 * childrenNodes.size()) {
                status = ThreeStatusCheckBox.CHECK_ALL;
            } else {
                status = ThreeStatusCheckBox.CHECK_PART;
            }
        }
        this.checkStatus = status;
        return status;
    }

    public int getChildrenCount() {
        if (childrenNodes == null) {
            return 0;
        } else {
            return childrenNodes.size();
        }
    }


    @Override
    public int compareTo(TreeNode o) {
        int result = this.pId - o.pId;
        if (result == 0){
            result = this.id - o.id;
        }
        return result;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "id=" + id +
                ", pId=" + pId +
                ", objs=" + objs +
                ", isExpand=" + isExpand +
                ", checkStatus=" + checkStatus +
                ", level=" + getLevel() +
                '}' +"\n";
    }
}
