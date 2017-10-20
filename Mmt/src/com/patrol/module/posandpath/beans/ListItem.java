package com.patrol.module.posandpath.beans;

/**
 * User: zhoukang
 * Date: 2016-03-15
 * Time: 15:32
 * <p/>
 * trunk:
 * PullToRefreshListView中的条目
 */
public class ListItem {
    // 姓名
    private String name;
    // 标记checkBox是否被选中
    private boolean isChecked;

    public ListItem() {
    }

    public ListItem(String name, boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    /**
     * 默认checkbox不选中
     * @param name
     */
    public ListItem(String name) {
        this.name = name;
        this.isChecked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean Checked) {
        this.isChecked = Checked;
    }
}
