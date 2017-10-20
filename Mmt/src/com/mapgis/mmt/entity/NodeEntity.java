package com.mapgis.mmt.entity;


/**
 * 人员机构列表数据模型
 */
public class NodeEntity {
    public boolean Checked;
    public NodeEntity[] ChildNodes;
    public boolean ShowCheckBox;
    public boolean Expanded;
    public String ImageToolTip;
    public String ImageUrl;
    public String NavigateUrl;
    public int SelectAction;
    public String Target;
    public String Text;
    public String ToolTip;
    public String Value;

    /**
     * 将人员机构数据模型转换成成适合适配器显示的数据模型
     */
    public Node toNode(Node parentNode) {
        Node node = new Node(Text, Value);
        node.setChecked(Checked);
        //Text : "<font color=red>调度判断分派<font>"
        if (parentNode != null&&(Text!=null&&!Text.contains("color=red"))) {
            node.setParent(parentNode);
            parentNode.add(node);
        }else{
            node.setText(node.getText().length() == 0 ? "人员机构列表" : node.getText());
        }

        if (ChildNodes != null && ChildNodes.length > 0) {
            for (NodeEntity nodeEntity : ChildNodes) {
                nodeEntity.toNode(node);
            }
        }

        return node;

    }
}