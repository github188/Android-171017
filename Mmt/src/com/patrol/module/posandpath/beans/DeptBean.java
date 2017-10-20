package com.patrol.module.posandpath.beans;

/**
 * User: zhoukang
 * Date: 2016-03-15
 * Time: 15:43
 * <p/>
 * trunk:
 * 描述部门信息的类
 */
public class DeptBean {
    // 部门Id
    public String DeptID;
    // 部门名称
    public String DeptName;

    public DeptBean(String deptID, String deptName) {
        DeptID = deptID;
        DeptName = deptName;
    }
}
