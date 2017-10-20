package com.mapgis.mmt.common.widget.treeview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Comclay on 2017/3/5.
 * 多级列表工具类
 */

public class TreeNodeUtil {

    /**
     * 根据所有节点获取可见节点
     */
    public static List<TreeNode> filterVisibleNode(List<TreeNode> allNodes) {
        List<TreeNode> visibleNodes = new ArrayList<>();
        for (TreeNode node : allNodes) {
            // 如果为根节点，或者上层目录为展开状态
            if (node.isRoot() || node.isParentExpand()) {
                visibleNodes.add(node);
            }
        }
        return visibleNodes;
    }

    public static void setNodeChecked(TreeNode node, int checkStatus) {
        // 自己设置是否选择
        node.setCheckStatus(checkStatus);
        /**
         * 非叶子节点,子节点处理
         */
        setChildrenNodeStatus(node);
        /** 父节点处理 */
        setParentStatus(node);
    }

    /**
     * 设置节点的选中状态
     *
     * @param node
     */
    public static void setCheckedStatus(TreeNode node, int checkStatus) {
        if (node.getCheckStatus() == checkStatus) {
            return;
        }
        node.setCheckStatus(checkStatus);
        // 当前条目的子条目状态完全保持一致
        setChildrenNodeStatus(node);
        // 这里需要重新计算父节点的状态
        setParentStatus(node);
    }

    public static TreeNode setParentStatus(TreeNode treeNode) {
        TreeNode parent = treeNode.getParent();
        if (parent == null || parent.isHideChecked()) {
            return null;
        }
        int preStatus = parent.getCheckStatus();
        int lastStatus = getCheckStatus(parent, true);
        if (preStatus != lastStatus) {
            TreeNode tempNode = setParentStatus(parent);
            return tempNode != null ? tempNode : treeNode;
        } else {
            return null;
        }
    }

    /**
     * 获取当前节点的状态
     *
     * @param isReCal true需要重新计算，false不需要重新计算
     * @return
     */
    public static int getCheckStatus(TreeNode node, boolean isReCal) {
        // 影藏选中框的话就直接返回-1啦
        if (node.isHideChecked()) {
            return -1;
        }
        int status = 0;
        if (!isReCal) {
            status = node.getCheckStatus();
        } else {
            List<TreeNode> childrenNodes = node.getChildrenNodes();
            int selectCount = 0;
            for (TreeNode childNode : childrenNodes) {
                switch (childNode.getCheckStatus()) {
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
        node.setCheckStatus(status);
        return status;
    }

    /**
     * 设置所有的子条目都为childrenNodesStatus状态
     * <p>
     * //@return 最后一个改变状态的id
     */
    public static void setChildrenNodeStatus(TreeNode treeNode) {
        int childrenNodeStatus = treeNode.getCheckStatus();
        List<TreeNode> childrenNodes = treeNode.getChildrenNodes();
        if (childrenNodes != null && childrenNodes.size() > 0) {
            for (TreeNode node : childrenNodes) {
                if (node.isHideChecked() || node.getCheckStatus() == childrenNodeStatus) {
                    continue;
                }
                node.setCheckStatus(childrenNodeStatus);
                setChildrenNodeStatus(node);
            }
        }
    }

    /**
     * 获取所有的根节点
     */
    public static List<TreeNode> getRootNodes(List<TreeNode> nodes) {
        List<TreeNode> rootNodes = new ArrayList<>();
        for (TreeNode node : nodes) {
            if (node.isRoot()) {
                rootNodes.add(node);
            }
        }

        return rootNodes;
    }

    /**
     * 合并一个节点
     *
     * @param node
     * @return 共隐藏的节点数目
     */
    public static int collapseNode(TreeNode node) {
        int count = 0;
        if (node.isExpand() && !node.isLeaf()){
            node.setExpand(false);
            count += node.getChildrenCount();
            for (TreeNode childNode : node.getChildrenNodes()) {
                if (!childNode.isLeaf() && childNode.isExpand()) {
                    count += collapseNode(childNode);
                }
            }
        }
        return count;
    }

    /**
     * 展开某一个节点
     *
     * @param node
     */
    public static void expandNode(TreeNode node) {
        if (node.isExpand()){
            return;
        }
        node.setExpand(true);
        TreeNode parent = node.getParent();
        if (parent != null && !parent.isExpand()) {
            expandNode(parent);
        }
    }

    /**
     * 按一定规则排序节点
     *
     * @param nodeList
     * @return
     */
    public static List<TreeNode> sortNodeList(List<TreeNode> nodeList) {
        List<TreeNode> list = new ArrayList<>();
        if (nodeList != null && nodeList.size() != 0) {
            for (TreeNode node : nodeList) {
                list.add(node);
                list.addAll(sortNodeList(node.getChildrenNodes()));
            }
        }
        return list;
    }

    public static List<TreeNode> formatNodeList(List<TreeNode> nodeList) {
        Collections.sort(nodeList);

        for (TreeNode node : nodeList) {
            List<TreeNode> childrenNodes = node.getChildrenNodes();
            for (TreeNode childNode : nodeList) {
                if (node.getId() == childNode.getpId()) {
                    childrenNodes.add(childNode);
                    childNode.setParent(node);
                }
            }
        }

        //        nodeList.clear();
//        nodeList.addAll(treeNodes);
        return sortNodeList(getRootNodes(nodeList));
    }
}
